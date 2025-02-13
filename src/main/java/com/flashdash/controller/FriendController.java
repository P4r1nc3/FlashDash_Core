package com.flashdash.controller;

import com.flashdash.dto.response.FriendInvitationResponse;
import com.flashdash.dto.response.UserResponse;
import com.flashdash.model.FriendInvitation;
import com.flashdash.service.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friends")
public class FriendController {
    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getFriends() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        List<UserResponse> friends = friendService.getFriends(userEmail);
        return ResponseEntity.ok(friends);
    }

    @PostMapping("/invite")
    public ResponseEntity<Void> sendFriendInvitation(@RequestParam String recipientEmail) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String senderEmail = authentication.getName();

        friendService.sendFriendInvitation(senderEmail, recipientEmail);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/invitations/received")
    public ResponseEntity<List<FriendInvitationResponse>> getReceivedInvitations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String recipientEmail = authentication.getName();

        List<FriendInvitationResponse> invitations = friendService.getReceivedFriendInvitations(recipientEmail);
        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/invitations/sent")
    public ResponseEntity<List<FriendInvitationResponse>> getSentInvitations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String senderEmail = authentication.getName();

        List<FriendInvitationResponse> invitations = friendService.getSentFriendInvitations(senderEmail);
        return ResponseEntity.ok(invitations);
    }

    @PutMapping("/invitations/{invitationId}")
    public ResponseEntity<Void> respondToInvitation(@PathVariable Long invitationId,
                                                    @RequestParam FriendInvitation.InvitationStatus status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        friendService.respondToFriendInvitation(invitationId, userEmail, status);
        return ResponseEntity.ok().build();
    }
}
