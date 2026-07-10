package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.models.User;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserManagementService userManagementService;

    @Test
    void serviceShouldBeInitialized() {
        assertNotNull(userManagementService);
    }

    @Test
    void getAllUsers_shouldReturnPaginatedUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = new User();
        Page<User> expectedPage = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<User> actualPage = userManagementService.getAllUsers(pageable);

        assertNotNull(actualPage);
        assertEquals(1, actualPage.getTotalElements());
        assertEquals(user, actualPage.getContent().get(0));
    }
}

