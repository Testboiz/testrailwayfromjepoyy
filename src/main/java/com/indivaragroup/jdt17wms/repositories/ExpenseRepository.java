package com.indivaragroup.jdt17wms.repositories;

import com.indivaragroup.jdt17wms.models.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    Optional<Expense> findByFinancialProfileId(UUID financialProfileId);
}
