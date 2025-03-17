package com.flashdash.core.controller;

import com.flashdash.core.model.FriendInvitation;
import com.flashdash.core.model.User;
import com.flashdash.core.service.InvitationService;
import com.flashdash.core.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.FriendInvitationResponseReceived;
import com.p4r1nc3.flashdash.core.model.FriendInvitationResponseSent;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invitations")
public class InvitationController {
    private final InvitationService invitationService;
    private final EntityToResponseMapper entityToResponseMapper;

    public InvitationController(InvitationService invitationService, EntityToResponseMapper entityToResponseMapper) {
        this.invitationService = invitationService;
        this.entityToResponseMapper = entityToResponseMapper;
    }

    @PostMapping()
    public ResponseEntity<Void> sendFriendInvitation(@RequestParam String recipientEmail) {
        String userFrn = getAuthenticatedUser();
        invitationService.sendFriendInvitation(userFrn, recipientEmail);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/received")
    public ResponseEntity<List<FriendInvitationResponseReceived>> getReceivedInvitations() {
        String userFrn = getAuthenticatedUser();
        List<FriendInvitation> invitations = invitationService.getReceivedFriendInvitations(userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToReceivedResponse(invitations));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<FriendInvitationResponseSent>> getSentInvitations() {
        String userFrn = getAuthenticatedUser();
        List<FriendInvitation> invitations = invitationService.getSentFriendInvitations(userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToSentResponse(invitations));
    }

    @PutMapping("/{invitationFrn}")
    public ResponseEntity<Void> respondToInvitation(@PathVariable String invitationFrn, @RequestParam String status) {
        String userFrn = getAuthenticatedUser();
        invitationService.respondToFriendInvitation(invitationFrn, userFrn, status);
        return ResponseEntity.ok().build();
    }

    private String getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getUserFrn();
    }
}
