package com.flashdash.controller;

import com.flashdash.dto.response.UserResponse;
import com.flashdash.model.FriendInvitation;
import com.flashdash.model.User;
import com.flashdash.service.FriendService;
import com.flashdash.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.FriendInvitationResponseReceived;
import com.p4r1nc3.flashdash.core.model.FriendInvitationResponseSent;
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
        List<UserResponse> friends = friendService.getFriends(userFrn);
        return ResponseEntity.ok(friends);
    }

    @DeleteMapping("/{friendFrn}")
    public ResponseEntity<Void> deleteFriend(@PathVariable String friendFrn) {
        String userFrn = getAuthenticatedUser();
        friendService.deleteFriend(userFrn, friendFrn);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invite")
    public ResponseEntity<Void> sendFriendInvitation(@RequestParam String recipientEmail) {
        String userFrn = getAuthenticatedUser();
        friendService.sendFriendInvitation(userFrn, recipientEmail);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/invitations/received")
    public ResponseEntity<List<FriendInvitationResponseReceived>> getReceivedInvitations() {
        String userFrn = getAuthenticatedUser();
        List<FriendInvitation> invitations = friendService.getReceivedFriendInvitations(userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToReceivedResponse(invitations));
    }

    @GetMapping("/invitations/sent")
    public ResponseEntity<List<FriendInvitationResponseSent>> getSentInvitations() {
        String userFrn = getAuthenticatedUser();
        List<FriendInvitation> invitations = friendService.getSentFriendInvitations(userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToSentResponse(invitations));
    }

    @PutMapping("/invitations/{invitationFrn}")
    public ResponseEntity<Void> respondToInvitation(@PathVariable String invitationFrn, @RequestParam String status) {
        String userFrn = getAuthenticatedUser();
        friendService.respondToFriendInvitation(invitationFrn, userFrn, status);
        return ResponseEntity.ok().build();
    }

    private String getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getUserFrn();
    }
}
