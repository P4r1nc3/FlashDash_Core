package com.flashdash.controller;

import com.flashdash.dto.response.UserResponse;
import com.flashdash.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService= userService;
    }

    @GetMapping
    public ResponseEntity<UserResponse> getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserResponse userResponse = userService.getCurrentUser(email);
        return ResponseEntity.ok(userResponse);
    }
}
