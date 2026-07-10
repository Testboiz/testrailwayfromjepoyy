package com.indivaragroup.jdt17wms.services;

import com.indivaragroup.jdt17wms.repositories.GoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class GoalsProjectionServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private GoalsProjectionService goalsProjectionService;

    @Test
    void serviceShouldBeInitialized() {
        assertNotNull(goalsProjectionService);
    }
}
