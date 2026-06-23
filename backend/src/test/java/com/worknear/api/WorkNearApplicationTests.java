package com.worknear.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WorkNearApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring context (beans, security, JPA mappings) wires up correctly.
    }
}
