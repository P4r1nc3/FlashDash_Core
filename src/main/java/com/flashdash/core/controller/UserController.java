package com.flashdash.core.controller;

import com.flashdash.core.model.User;
import com.flashdash.core.service.UserService;
import com.flashdash.core.service.api.NotificationService;
import com.flashdash.core.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.ChangePasswordRequest;
import com.p4r1nc3.flashdash.core.model.UserResponse;
import com.p4r1nc3.flashdash.notification.model.NotificationSubscriber;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final NotificationService notificationService;
    private final EntityToResponseMapper entityToResponseMapper;

    public UserController(UserService userService,
                          NotificationService notificationService,
                          EntityToResponseMapper entityToResponseMapper) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.entityToResponseMapper = entityToResponseMapper;
    }

    @GetMapping
    public ResponseEntity<UserResponse> getUser() {
        String userFrn = getAuthenticatedUser();
        User user = userService.getCurrentUser(userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToUserResponse(user));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser() {
        String userFrn = getAuthenticatedUser();
        userService.deleteUser(userFrn);
        return ResponseEntity.noContent().build();
    }

    @PutMapping()
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        userService.changePassword(email, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/notifications/enable")
    public ResponseEntity<Void> enableNotifications(@RequestParam(required = false) LocalTime notificationTime) {
        String userFrn = getAuthenticatedUser();
        notificationService.enableDailyNotifications(userFrn, notificationTime);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/notifications")
    public ResponseEntity<NotificationSubscriber> getNotificationsDetails() {
        String userFrn = getAuthenticatedUser();
        NotificationSubscriber notificationSubscriber = notificationService.getSubscriber(userFrn);
        return ResponseEntity.ok(notificationSubscriber);
    }

    @PutMapping("/notifications/disable")
    public ResponseEntity<Void> disableNotifications() {
        String userFrn = getAuthenticatedUser();
        notificationService.disableDailyNotifications(userFrn);
        return ResponseEntity.ok().build();
    }

    private String getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getUserFrn();
    }
}
