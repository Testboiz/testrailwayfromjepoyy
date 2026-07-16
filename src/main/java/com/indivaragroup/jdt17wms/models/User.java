package com.indivaragroup.jdt17wms.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.indivaragroup.jdt17wms.models.enums.UserRole;
import com.indivaragroup.jdt17wms.models.enums.UserRoleConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mst_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role", nullable = false, columnDefinition = "user_role")
    @Convert(converter = UserRoleConverter.class)
    @ColumnTransformer(write = "?::user_role")
    private UserRole role;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "risk_profile")
    private String riskProfile;

    @Column(name = "questionnaire_completed",nullable = false)
    private Boolean questionnaireCompleted;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
