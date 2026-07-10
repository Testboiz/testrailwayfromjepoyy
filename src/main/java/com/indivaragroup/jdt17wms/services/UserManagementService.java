package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserManagementService {

    private final UserRepository userRepository;

    public UserManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
