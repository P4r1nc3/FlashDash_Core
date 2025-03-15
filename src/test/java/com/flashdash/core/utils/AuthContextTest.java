package com.flashdash.core.utils;

import com.flashdash.core.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthContextTest {

    @Autowired
    private AuthContext authContext;

    @MockitoBean
    private SecurityContext securityContext;

    @MockitoBean
    private Authentication authentication;

    @MockitoBean
    private User mockUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldReturnUserFrnWhenUserIsAuthenticated() {
        // Arrange
        String expectedUserFrn = "user-123";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(mockUser.getUserFrn()).thenReturn(expectedUserFrn);

        // Act
        String userFrn = authContext.getUserFrn();

        // Assert
        assertThat(userFrn).isEqualTo(expectedUserFrn);
    }

    @Test
    void shouldReturnNullWhenNoAuthenticationPresent() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        String userFrn = authContext.getUserFrn();

        // Assert
        assertThat(userFrn).isNull();
    }

    @Test
    void shouldReturnNullWhenAuthenticationPrincipalIsNotUser() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("InvalidPrincipal");

        // Act
        String userFrn = authContext.getUserFrn();

        // Assert
        assertThat(userFrn).isNull();
    }
}
