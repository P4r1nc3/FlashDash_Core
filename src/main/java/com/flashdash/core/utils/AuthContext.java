package com.flashdash.core.utils;

import com.flashdash.core.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthContext {

    private final HttpServletRequest request;

    public AuthContext(HttpServletRequest request) {
        this.request = request;
    }

    public String getUserFrn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            User user = (User) authentication.getPrincipal();
            return user.getUserFrn();
        }

        return null;
    }

    public String getBearerToken() {
        String authHeader = request.getHeader("Authorization");
        return authHeader.substring(7);
    }
}
