package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.constants.AppConstants;
import com.indivaragroup.jdt17wms.dto.response.GoalProjectionDTO;
import com.indivaragroup.jdt17wms.dto.response.GoalProjectionDTO.TimeSeriesPointDTO;
import com.indivaragroup.jdt17wms.exceptions.MissingRiskProfileException;
import com.indivaragroup.jdt17wms.exceptions.NotFoundException;
import com.indivaragroup.jdt17wms.models.FinancialProfile;
import com.indivaragroup.jdt17wms.models.Goal;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.models.Asset;
import com.indivaragroup.jdt17wms.models.Product;
import com.indivaragroup.jdt17wms.repositories.FinancialProfileRepository;
import com.indivaragroup.jdt17wms.repositories.GoalRepository;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import com.indivaragroup.jdt17wms.repositories.AssetRepository;
import com.indivaragroup.jdt17wms.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoalsProjectionService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final FinancialProfileRepository financialProfileRepository;
    private final AssetRepository assetRepository;
    private final ProductRepository productRepository;
    private final Clock clock;

    public GoalsProjectionService(GoalRepository goalRepository,
                                  UserRepository userRepository,
                                  FinancialProfileRepository financialProfileRepository,
                                  AssetRepository assetRepository,
                                  ProductRepository productRepository,
                                  Clock clock) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.financialProfileRepository = financialProfileRepository;
        this.assetRepository = assetRepository;
        this.productRepository = productRepository;
        this.clock = clock;
    }

    public List<GoalProjectionDTO> getProjectionsForUser() {
        User user = userRepository.findById(AppConstants.USER_ID)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getQuestionnaireCompleted() == null || !user.getQuestionnaireCompleted()) {
            throw new MissingRiskProfileException("Risk Profiler Assessment Required");
        }

        BigDecimal defaultReturn = financialProfileRepository.findByUserId(user.getId())
                .map(FinancialProfile::getDefaultReturn)
                .orElse(BigDecimal.valueOf(7.50));

        double annualRate = defaultReturn.doubleValue();
        double defaultMonthlyRate = 0.0; // Savings do not grow like assets do, so rate is 0.0

        List<Goal> goals = goalRepository.findAllByUserId(user.getId());

        return goals.stream().map(goal -> {
            List<Asset> assets = assetRepository.findAllByGoalId(goal.getId());

            double target = goal.getTargetAmount() != null ? goal.getTargetAmount().doubleValue() : 0.0;
            double totalContribution = goal.getMonthlyContribution() != null ? goal.getMonthlyContribution().doubleValue() : 0.0;

            LocalDate projectedDate;
            BigDecimal recommendedContribution;
            List<TimeSeriesPointDTO> timeSeries = new ArrayList<>();

            if (assets.isEmpty()) {
                // Scenario A: No assets tied to the goal. Savings do not grow (0% return rate).
                double balance = goal.getCurrentAmount() != null ? goal.getCurrentAmount().doubleValue() : 0.0;

                // 1. Calculate projected date
                int monthsToTarget = 0;
                double simBalance = balance;
                if (simBalance < target) {
                    if (totalContribution > 0) {
                        while (simBalance < target && monthsToTarget < 12000) {
                            simBalance = simBalance * (1 + defaultMonthlyRate) + totalContribution;
                            monthsToTarget++;
                        }
                    } else {
                        monthsToTarget = 12000;
                    }
                }
                projectedDate = LocalDate.now(clock).plusMonths(monthsToTarget);

                // 2. Calculate recommended contribution
                double maxMonths = AppConstants.GOAL_MAX_MONTHS.getOrDefault(
                        goal.getType() != null ? goal.getType().toLowerCase() : "custom", 60
                );
                double monthsToUse = maxMonths;
                if (goal.getTargetDate() != null) {
                    long actualMonths = ChronoUnit.MONTHS.between(LocalDate.now(clock), goal.getTargetDate());
                    if (actualMonths > 0 && actualMonths < maxMonths) {
                        monthsToUse = actualMonths;
                    }
                }
                double recContributionVal = (target - balance) / monthsToUse;
                recommendedContribution = BigDecimal.valueOf(Math.max(0.0, recContributionVal))
                        .setScale(2, RoundingMode.HALF_UP);

                // 3. Calculate 60-month time-series
                double runningBalance = balance;
                for (int m = 1; m <= 60; m++) {
                    runningBalance = runningBalance * (1 + defaultMonthlyRate) + totalContribution;
                    timeSeries.add(TimeSeriesPointDTO.builder()
                            .month(m)
                            .value(BigDecimal.valueOf(runningBalance).setScale(2, RoundingMode.HALF_UP))
                            .build());
                }

            } else {
                // Scenario B: Assets are tied. Savings are evenly distributed to all assets.
              int kValue = assets.size();
                double[] balances = new double[kValue];
                double[] rates = new double[kValue];
                double contributionPerAsset = totalContribution / kValue;

                double initialSum = 0.0;
                for (int j = 0; j < kValue; j++) {
                    Asset asset = assets.get(j);
                    balances[j] = asset.getCurrentValue() != null ? asset.getCurrentValue().doubleValue() : 0.0;
                    initialSum += balances[j];

                    Product product = productRepository.findById(asset.getProductId()).orElse(null);
                    double assetAnnualReturn = (product != null && product.getAnnualReturn() != null)
                            ? product.getAnnualReturn().doubleValue() : annualRate;
                    rates[j] = assetAnnualReturn / 100.0 / 12.0;
                }

                // 1. Calculate projected date
                int monthsToTarget = 0;
                double[] simBalances = balances.clone();
                double sum = initialSum;
                if (sum < target) {
                    boolean canGrow = contributionPerAsset > 0;
                    for (int j = 0; j < kValue; j++) {
                      if (rates[j] > 0 && balances[j] > 0) {
                        canGrow = true;
                        break;
                      }
                    }
                    if (canGrow) {
                        while (sum < target && monthsToTarget < 12000) {
                            sum = 0.0;
                            for (int j = 0; j < kValue; j++) {
                                simBalances[j] = simBalances[j] * (1 + rates[j]) + contributionPerAsset;
                                sum += simBalances[j];
                            }
                            monthsToTarget++;
                        }
                    } else {
                        monthsToTarget = 12000;
                    }
                }
                projectedDate = LocalDate.now(clock).plusMonths(monthsToTarget);

                // 2. Calculate recommended contribution
                double maxMonths = AppConstants.GOAL_MAX_MONTHS.getOrDefault(
                        goal.getType() != null ? goal.getType().toLowerCase() : "custom", 60
                );
                double monthsToUse = maxMonths;
                if (goal.getTargetDate() != null) {
                    long actualMonths = ChronoUnit.MONTHS.between(LocalDate.now(clock), goal.getTargetDate());
                    if (actualMonths > 0 && actualMonths < maxMonths) {
                        monthsToUse = actualMonths;
                    }
                }
                double num = target;
                double sumS = 0.0;
                for (int j = 0; j < kValue; j++) {
                    double fValueFactor = Math.pow(1 + rates[j], monthsToUse);
                    num -= balances[j] * fValueFactor;

                    double sFactor;
                    if (rates[j] > 0) {
                        sFactor = (fValueFactor - 1) / rates[j];
                    } else {
                        sFactor = monthsToUse;
                    }
                    sumS += sFactor;
                }
                double denom = sumS / kValue;
                double recContributionVal = denom > 0 ? num / denom : 0.0;
                recommendedContribution = BigDecimal.valueOf(Math.max(0.0, recContributionVal))
                        .setScale(2, RoundingMode.HALF_UP);

                // 3. Calculate 60-month time-series
                double[] runBalances = balances.clone();
                for (int m = 1; m <= 60; m++) {
                    double stepSum = 0.0;
                    for (int j = 0; j < kValue; j++) {
                        runBalances[j] = runBalances[j] * (1 + rates[j]) + contributionPerAsset;
                        stepSum += runBalances[j];
                    }
                    timeSeries.add(TimeSeriesPointDTO.builder()
                            .month(m)
                            .value(BigDecimal.valueOf(stepSum).setScale(2, RoundingMode.HALF_UP))
                            .build());
                }
            }

            return GoalProjectionDTO.builder()
                    .id(goal.getId())
                    .name(goal.getName())
                    .type(goal.getType())
                    .targetAmount(goal.getTargetAmount())
                    .targetDate(goal.getTargetDate())
                    .isPriority(goal.getIsPriority())
                    .notes(goal.getNotes())
                    .status(goal.getStatus())
                    .projectedDate(projectedDate)
                    .recommendedContribution(recommendedContribution)
                    .timeSeries(timeSeries)
                    .build();
        }).toList();
    }
}
