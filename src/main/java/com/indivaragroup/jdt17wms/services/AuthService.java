package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
