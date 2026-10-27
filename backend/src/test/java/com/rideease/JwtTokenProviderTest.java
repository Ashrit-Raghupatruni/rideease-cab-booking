package com.rideease;

import com.rideease.config.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String testSecret = "A_VERY_SECURE_TEST_SECRET_KEY_FOR_UNIT_TESTING_PURPOSES_32_CHARS!";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000L);
    }

    @Test
    @DisplayName("Should generate valid token and extract subject claims")
    void testTokenGenerationAndExtraction() {
        String token = jwtTokenProvider.generateToken("testuser@example.com", "ROLE_USER", "user-12345");
        assertNotNull(token);

        String username = jwtTokenProvider.extractUsername(token);
        assertEquals("testuser@example.com", username);

        String userId = jwtTokenProvider.extractUserId(token);
        assertEquals("user-12345", userId);

        String role = jwtTokenProvider.extractRole(token);
        assertEquals("ROLE_USER", role);

        assertTrue(jwtTokenProvider.validateToken(token, "testuser@example.com"));
        assertFalse(jwtTokenProvider.validateToken(token, "otheruser@example.com"));
    }
}
