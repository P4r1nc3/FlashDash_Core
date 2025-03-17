package com.flashdash.core.controller;

import com.flashdash.core.TestUtils;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.FriendInvitation;
import com.flashdash.core.model.User;
import com.flashdash.core.service.InvitationService;
import com.flashdash.core.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.FriendInvitationResponseReceived;
import com.p4r1nc3.flashdash.core.model.FriendInvitationResponseSent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InvitationControllerTest {

    @Autowired
    private InvitationController invitationController;

    @MockitoBean
    private InvitationService invitationService;

    @MockitoBean
    private EntityToResponseMapper entityToResponseMapper;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    private User testUser;
    private User friendUser;
    private String userFrn;

    @BeforeEach
    void setUp() {
        testUser = TestUtils.createUser();
        friendUser = TestUtils.createUser();
        userFrn = testUser.getUserFrn();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    @Test
    void shouldSendFriendInvitationSuccessfully() {
        // Arrange
        String recipientEmail = friendUser.getEmail();
        doNothing().when(invitationService).sendFriendInvitation(userFrn, recipientEmail);

        // Act
        ResponseEntity<Void> responseEntity = invitationController.sendFriendInvitation(recipientEmail);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(invitationService, times(1)).sendFriendInvitation(userFrn, recipientEmail);
    }

    @Test
    void shouldGetReceivedFriendInvitationsSuccessfully() {
        // Arrange
        List<FriendInvitation> invitations = List.of(TestUtils.createFriendInvitation(friendUser, testUser));
        List<FriendInvitationResponseReceived> expectedResponses = List.of(new FriendInvitationResponseReceived());

        when(invitationService.getReceivedFriendInvitations(userFrn)).thenReturn(invitations);
        when(entityToResponseMapper.mapToReceivedResponse(invitations)).thenReturn(expectedResponses);

        // Act
        ResponseEntity<List<FriendInvitationResponseReceived>> responseEntity = invitationController.getReceivedInvitations();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponses, responseEntity.getBody());
    }

    @Test
    void shouldGetSentFriendInvitationsSuccessfully() {
        // Arrange
        List<FriendInvitation> invitations = List.of(TestUtils.createFriendInvitation(testUser, friendUser));
        List<FriendInvitationResponseSent> expectedResponses = List.of(new FriendInvitationResponseSent());

        when(invitationService.getSentFriendInvitations(userFrn)).thenReturn(invitations);
        when(entityToResponseMapper.mapToSentResponse(invitations)).thenReturn(expectedResponses);

        // Act
        ResponseEntity<List<FriendInvitationResponseSent>> responseEntity = invitationController.getSentInvitations();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponses, responseEntity.getBody());
    }

    @Test
    void shouldRespondToInvitationSuccessfully() {
        // Arrange
        String invitationFrn = "frn:flashdash:invitation:123";
        String status = "ACCEPTED";

        doNothing().when(invitationService).respondToFriendInvitation(invitationFrn, userFrn, status);

        // Act
        ResponseEntity<Void> responseEntity = invitationController.respondToInvitation(invitationFrn, status);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(invitationService, times(1)).respondToFriendInvitation(invitationFrn, userFrn, status);
    }

    @Test
    void shouldThrowExceptionWhenSendingInvitationToSelf() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E403003, "You cannot send an invitation to yourself."))
                .when(invitationService).sendFriendInvitation(userFrn, testUser.getEmail());

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> invitationController.sendFriendInvitation(testUser.getEmail())
        );
        assertEquals(ErrorCode.E403003, exception.getErrorCode());
        assertEquals("You cannot send an invitation to yourself.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserIsAlreadyAFriend() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E409003, "You are already friends with this user."))
                .when(invitationService).sendFriendInvitation(userFrn, friendUser.getEmail());

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> invitationController.sendFriendInvitation(friendUser.getEmail())
        );
        assertEquals(ErrorCode.E409003, exception.getErrorCode());
        assertEquals("You are already friends with this user.", exception.getMessage());
    }
}
