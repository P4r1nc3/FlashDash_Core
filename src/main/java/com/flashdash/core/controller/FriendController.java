package com.flashdash.core.controller;

import com.flashdash.core.model.User;
import com.flashdash.core.service.FriendService;
import com.flashdash.core.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friends")
public class FriendController {
    private final FriendService friendService;
    private final EntityToResponseMapper entityToResponseMapper;

    public FriendController(FriendService friendService, EntityToResponseMapper entityToResponseMapper) {
        this.friendService = friendService;
        this.entityToResponseMapper = entityToResponseMapper;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getFriends() {
        String userFrn = getAuthenticatedUser();
        List<User> users = friendService.getFriends(userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToUserResponse(users));
    }

    @GetMapping("/{friendFrn}")
    public ResponseEntity<UserResponse> getFriend(@PathVariable String friendFrn) {
        String userFrn = getAuthenticatedUser();
        User user = friendService.getFriend(userFrn, friendFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToUserResponse(user));
    }

    @DeleteMapping("/{friendFrn}")
    public ResponseEntity<Void> deleteFriend(@PathVariable String friendFrn) {
        String userFrn = getAuthenticatedUser();
        friendService.deleteFriend(userFrn, friendFrn);
        return ResponseEntity.noContent().build();
    }

    private String getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getUserFrn();
    }
}
