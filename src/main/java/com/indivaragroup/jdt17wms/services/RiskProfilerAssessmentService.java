package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.constants.AppConstants;
import com.indivaragroup.jdt17wms.dto.request.Answer;
import com.indivaragroup.jdt17wms.dto.request.RiskProfilerDTO;
import com.indivaragroup.jdt17wms.dto.response.OptionDTO;
import com.indivaragroup.jdt17wms.dto.response.QuestionnaireDTO;
import com.indivaragroup.jdt17wms.dto.response.RiskProfilerResponseDTO;
import com.indivaragroup.jdt17wms.dto.utils.QuestionnaireDataDTO;
import com.indivaragroup.jdt17wms.exceptions.BadRequestException;
import com.indivaragroup.jdt17wms.exceptions.NotFoundException;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RiskProfilerAssessmentService {

  private final QuestionnaireDataDTO questionnaireDataDTO;
    private final UserRepository userRepository;

    public RiskProfilerAssessmentService(
                                         QuestionnaireDataDTO questionnaireDataDTO,
                                         UserRepository userRepository) {
      this.questionnaireDataDTO = questionnaireDataDTO;
        this.userRepository = userRepository;
    }

    public List<QuestionnaireDTO> getQuestionnaire() {
        if (questionnaireDataDTO.getData() == null) {
            return List.of();
        }
        return questionnaireDataDTO.getData().stream()
                .map(item -> {
                    List<OptionDTO> responseOptions = List.of();
                    if (item.getOptions() != null) {
                        responseOptions = item.getOptions().stream()
                                .map(opt -> OptionDTO.builder()
                                        .label(opt.getLabel())
                                        .score(opt.getScore())
                                        .build())
                          .toList();
                    }
                    return QuestionnaireDTO.builder()
                            .question(item.getQuestion())
                            .options(responseOptions)
                            .build();
                })
                .toList();
    }

    public RiskProfilerResponseDTO updateProfilerAssessment(RiskProfilerDTO riskProfilerDTO) {
        if (riskProfilerDTO == null || riskProfilerDTO.getAnswers() == null) {
            throw new BadRequestException("Invalid JSON Body");
        }

        int expectedSize = questionnaireDataDTO.getData() != null ? questionnaireDataDTO.getData().size() : AppConstants.DEFAULT_QUESTIONNAIRE_SIZE;
        if (riskProfilerDTO.getAnswers().size() != expectedSize) {
            throw new BadRequestException("Invalid answers count");
        }

        for (com.indivaragroup.jdt17wms.dto.request.Answer answer : riskProfilerDTO.getAnswers()) {
            if (answer.getScore() == null || answer.getScore() < AppConstants.MIN_ANSWER_SCORE || answer.getScore() > AppConstants.MAX_ANSWER_SCORE) {
                throw new BadRequestException("Invalid JSON Body");
            }
        }

        int rawScore = riskProfilerDTO.getAnswers().stream()
                .mapToInt(Answer::getScore)
                .sum();

        String riskProfile;
        if (rawScore <= AppConstants.RISK_AVERSE_THRESHOLD) {
            riskProfile = "risk_averse";
        } else if (rawScore <= AppConstants.RISK_MODERATE_THRESHOLD) {
            riskProfile = "moderate";
        } else {
            riskProfile = "risk_taker";
        }

        int outputScore = rawScore * AppConstants.RISK_SCALING_FACTOR;

        User user = userRepository.findById(AppConstants.USER_ID)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setRiskProfile(riskProfile);
        user.setQuestionnaireCompleted(true);
        user.setUpdatedAt(Instant.now());
        user = userRepository.save(user);

        return RiskProfilerResponseDTO.builder()
                .id(user.getId())
                .riskProfile(user.getRiskProfile())
                .questionnaireCompleted(user.getQuestionnaireCompleted())
                .updatedAt(user.getUpdatedAt())
                .score(outputScore)
                .build();
    }
}
