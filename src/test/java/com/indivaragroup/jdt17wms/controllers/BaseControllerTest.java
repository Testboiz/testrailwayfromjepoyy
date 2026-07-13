package com.indivaragroup.jdt17wms.controllers;

import com.indivaragroup.jdt17wms.repositories.UserRepository;
import com.indivaragroup.jdt17wms.services.JwtService;
import org.springframework.boot.test.mock.mockito.MockBean;

public abstract class BaseControllerTest {

    @MockBean
    protected JwtService jwtService;

    @MockBean
    protected UserRepository userRepository;
}
