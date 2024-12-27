package com.flashdash.controller;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.dto.response.FriendInvitationResponse;
import com.flashdash.dto.response.UserResponse;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.FriendInvitation;
import com.flashdash.model.User;
import com.flashdash.service.FriendService;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FriendControllerTest {

    @Autowired
    private FriendController friendController;

    @MockBean
    private FriendService friendService;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    private User testUser;
    private User friendUser;
    private String userEmail;

    @BeforeEach
    void setUp() {
        testUser = TestUtils.createUser();
        friendUser = TestUtils.createFriendUser();
        userEmail = testUser.getUsername();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userEmail);

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
    @Order(1)
    void testGetFriendsSuccessful() {
        // Arrange
        UserResponse friendResponse = new UserResponse(
                friendUser.getFirstName(),
                friendUser.getLastName(),
                friendUser.getUsername(),
                friendUser.isDailyNotifications(),
                friendUser.getCreatedAt(),
                friendUser.getUpdatedAt()
        );

        List<UserResponse> friends = List.of(friendResponse);
        when(friendService.getFriends(userEmail)).thenReturn(friends);

        // Act
        ResponseEntity<List<UserResponse>> responseEntity = friendController.getFriends();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(friends, responseEntity.getBody());
    }

    @Test
    @Order(2)
    void testGetFriendsUserNotFound() {
        // Arrange
        when(friendService.getFriends(userEmail))
                .thenThrow(new FlashDashException(ErrorCode.E404001, "User not found"));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> friendController.getFriends()
        );
        assertEquals(ErrorCode.E404001, exception.getErrorCode());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @Order(3)
    void testSendFriendInvitationSuccessful() {
        // Arrange
        String recipientEmail = friendUser.getUsername();
        doNothing().when(friendService).sendFriendInvitation(userEmail, recipientEmail);

        // Act
        ResponseEntity<Void> responseEntity = friendController.sendFriendInvitation(recipientEmail);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(friendService, times(1)).sendFriendInvitation(userEmail, recipientEmail);
    }

    @Test
    @Order(4)
    void testGetReceivedInvitationsSuccessful() {
        // Arrange
        FriendInvitation invitation = TestUtils.createFriendInvitation(friendUser, testUser);
        FriendInvitationResponse invitationResponse = new FriendInvitationResponse(
                invitation.getId(),
                invitation.getSentBy().getFirstName(),
                invitation.getSentBy().getLastName(),
                invitation.getSentTo().getFirstName(),
                invitation.getSentTo().getLastName(),
                invitation.getStatus().name(),
                invitation.getCreatedAt()
        );

        List<FriendInvitationResponse> invitations = List.of(invitationResponse);
        when(friendService.getReceivedFriendInvitations(userEmail)).thenReturn(invitations);

        // Act
        ResponseEntity<List<FriendInvitationResponse>> responseEntity = friendController.getReceivedInvitations();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(invitations, responseEntity.getBody());
    }

    @Test
    @Order(5)
    void testGetSentInvitationsSuccessful() {
        // Arrange
        FriendInvitation invitation = TestUtils.createFriendInvitation(testUser, friendUser);
        FriendInvitationResponse invitationResponse = new FriendInvitationResponse(
                invitation.getId(),
                invitation.getSentBy().getFirstName(),
                invitation.getSentBy().getLastName(),
                invitation.getSentTo().getFirstName(),
                invitation.getSentTo().getLastName(),
                invitation.getStatus().name(),
                invitation.getCreatedAt()
        );

        List<FriendInvitationResponse> invitations = List.of(invitationResponse);
        when(friendService.getSentFriendInvitations(userEmail)).thenReturn(invitations);

        // Act
        ResponseEntity<List<FriendInvitationResponse>> responseEntity = friendController.getSentInvitations();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(invitations, responseEntity.getBody());
    }

    @Test
    @Order(6)
    void testRespondToInvitationSuccessful() {
        // Arrange
        Long invitationId = 1L;
        FriendInvitation.InvitationStatus status = FriendInvitation.InvitationStatus.ACCEPTED;
        doNothing().when(friendService).respondToFriendInvitation(invitationId, userEmail, status);

        // Act
        ResponseEntity<Void> responseEntity = friendController.respondToInvitation(invitationId, status);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(friendService, times(1)).respondToFriendInvitation(invitationId, userEmail, status);
    }

    @Test
    @Order(7)
    void testRespondToInvitationUnauthorized() {
        // Arrange
        Long invitationId = 1L;
        FriendInvitation.InvitationStatus status = FriendInvitation.InvitationStatus.ACCEPTED;
        doThrow(new FlashDashException(ErrorCode.E403001, "Unauthorized to respond to this invitation."))
                .when(friendService).respondToFriendInvitation(invitationId, userEmail, status);

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> friendController.respondToInvitation(invitationId, status)
        );
        assertEquals(ErrorCode.E403001, exception.getErrorCode());
        assertEquals("Unauthorized to respond to this invitation.", exception.getMessage());
    }

    @Test
    @Order(8)
    void testSendFriendInvitationDuplicate() {
        // Arrange
        String recipientEmail = friendUser.getUsername();
        doThrow(new FlashDashException(ErrorCode.E409002, "Friend invitation already sent."))
                .when(friendService).sendFriendInvitation(userEmail, recipientEmail);

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> friendController.sendFriendInvitation(recipientEmail)
        );
        assertEquals(ErrorCode.E409002, exception.getErrorCode());
        assertEquals("Friend invitation already sent.", exception.getMessage());
    }

    @Test
    @Order(9)
    void shouldThrowExceptionWhenSendingInvitationToSelf() {
        // Arrange
        String recipientEmail = userEmail;
        doThrow(new FlashDashException(ErrorCode.E403003, "You cannot send an invitation to yourself."))
                .when(friendService).sendFriendInvitation(userEmail, recipientEmail);

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> friendController.sendFriendInvitation(recipientEmail)
        );
        assertEquals(ErrorCode.E403003, exception.getErrorCode());
        assertEquals("You cannot send an invitation to yourself.", exception.getMessage());
    }
}
