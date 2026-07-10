package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.exceptions.BadRequestException;
import com.indivaragroup.jdt17wms.exceptions.NotFoundException;
import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class UserManagementService {

    private final UserRepository userRepository;

    public UserManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User updateUserStatus(UUID id, String status) {
        if (status == null || (!status.equals("active") && !status.equals("disabled"))) {
            throw new BadRequestException("Invalid status value");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No valid item with the ID"));
        user.setStatus(status);
        return userRepository.save(user);
    }
}
