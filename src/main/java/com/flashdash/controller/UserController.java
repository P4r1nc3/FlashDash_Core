package com.flashdash.controller;

import com.flashdash.model.User;
import com.flashdash.service.UserService;
import com.flashdash.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.ChangePasswordRequest;
import com.p4r1nc3.flashdash.core.model.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final EntityToResponseMapper entityToResponseMapper;

    public UserController(UserService userService, EntityToResponseMapper entityToResponseMapper) {
        this.userService = userService;
        this.entityToResponseMapper = entityToResponseMapper;
    }

    @GetMapping
    public ResponseEntity<UserResponse> getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userService.getCurrentUser(email);
        return ResponseEntity.ok(entityToResponseMapper.toUserResponse(user));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        userService.changePassword(email, request);
        return ResponseEntity.ok().build();
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user;
    }
}
