package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.exceptions.NotFoundException;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;


@Service
@Validated
public class UserManagementService {

    private final UserRepository userRepository;

    public UserManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

  public User updateUserStatus(
    UUID id,
    @NotNull(message = "Status cannot be null")
    @Pattern(regexp = "^(active|disabled)$", message = "Invalid status value")
    String status
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No valid item with the ID"));
        user.setStatus(status);
        return userRepository.save(user);
    }
}
