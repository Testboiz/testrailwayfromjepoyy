package com.indivaragroup.jdt17wms.repositories;

import com.indivaragroup.jdt17wms.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.indivaragroup.jdt17wms.dto.utils.UserSecurityProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  List<User> findByRiskProfile(String riskProfile);
  boolean existsByEmail(String email);
  Optional<User> findByEmail(String email);

  @Query("SELECT u.id as id, u.name as name, u.email as email, u.role as role, (SELECT COUNT(o) FROM User o WHERE o.createdAt < u.createdAt) as priorCount FROM User u WHERE u.email = :email")
  Optional<UserSecurityProjection> findUserSecurityProjectionByEmail(@Param("email") String email);
}
