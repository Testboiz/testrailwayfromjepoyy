package com.indivaragroup.jdt17wms.repositories;

import com.indivaragroup.jdt17wms.models.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {
    List<Goal> findAllByUserId(UUID userId);
}
