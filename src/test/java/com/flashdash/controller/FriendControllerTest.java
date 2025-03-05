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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FriendControllerTest {

    @Autowired
    private FriendController friendController;

    @MockitoBean
    private FriendService friendService;

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
    void testGetFriendsSuccessful() {
        // Arrange
        UserResponse friendResponse = new UserResponse(friendUser);

        List<UserResponse> friends = List.of(friendResponse);
        when(friendService.getFriends(userFrn)).thenReturn(friends);

        // Act
        ResponseEntity<List<UserResponse>> responseEntity = friendController.getFriends();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(friends, responseEntity.getBody());
    }

    @Test
    void testSendFriendInvitationSuccessful() {
        // Arrange
        String recipientFrn = friendUser.getUserFrn();
        doNothing().when(friendService).sendFriendInvitation(userFrn, recipientFrn);

        // Act
        ResponseEntity<Void> responseEntity = friendController.sendFriendInvitation(recipientFrn);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(friendService, times(1)).sendFriendInvitation(userFrn, recipientFrn);
    }

    @Test
    void testGetReceivedInvitationsSuccessful() {
        // Arrange
        FriendInvitation invitation = TestUtils.createFriendInvitation(friendUser, testUser);
        FriendInvitationResponse invitationResponse = new FriendInvitationResponse(
                invitation.getInvitationFrn(),
                invitation.getSentByFrn(),
                invitation.getSentToFrn(),
                invitation.getStatus(),
                invitation.getCreatedAt()
        );

        List<FriendInvitationResponse> invitations = List.of(invitationResponse);
        when(friendService.getReceivedFriendInvitations(userFrn)).thenReturn(invitations);

        // Act
        ResponseEntity<List<FriendInvitationResponse>> responseEntity = friendController.getReceivedInvitations();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(invitations, responseEntity.getBody());
    }

    @Test
    void testRespondToInvitationSuccessful() {
        // Arrange
        String invitationFrn = "frn:flashdash:invitation:123";
        String status = "ACCEPTED";
        doNothing().when(friendService).respondToFriendInvitation(invitationFrn, userFrn, status);

        // Act
        ResponseEntity<Void> responseEntity = friendController.respondToInvitation(invitationFrn, status);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(friendService, times(1)).respondToFriendInvitation(invitationFrn, userFrn, status);
    }

    @Test
    void testDeleteFriendSuccessful() {
        // Arrange
        String friendFrn = friendUser.getUserFrn();
        doNothing().when(friendService).deleteFriend(userFrn, friendFrn);

        // Act
        ResponseEntity<Void> responseEntity = friendController.deleteFriend(friendFrn);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(friendService, times(1)).deleteFriend(userFrn, friendFrn);
    }

    @Test
    void shouldThrowExceptionWhenSendingInvitationToSelf() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E403003, "You cannot send an invitation to yourself."))
                .when(friendService).sendFriendInvitation(userFrn, userFrn);

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> friendController.sendFriendInvitation(userFrn)
        );
        assertEquals(ErrorCode.E403003, exception.getErrorCode());
        assertEquals("You cannot send an invitation to yourself.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserIsAlreadyAFriend() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E409003, "You are already friends with this user."))
                .when(friendService).sendFriendInvitation(userFrn, friendUser.getUserFrn());

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> friendController.sendFriendInvitation(friendUser.getUserFrn())
        );
        assertEquals(ErrorCode.E409003, exception.getErrorCode());
        assertEquals("You are already friends with this user.", exception.getMessage());
    }

    @Test
    void testDeleteFriendNotFound() {
        // Arrange
        String friendFrn = friendUser.getUserFrn();
        doThrow(new FlashDashException(ErrorCode.E404005, "Friend not found: " + friendFrn))
                .when(friendService).deleteFriend(userFrn, friendFrn);

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> friendController.deleteFriend(friendFrn)
        );
        assertEquals(ErrorCode.E404005, exception.getErrorCode());
        assertEquals("Friend not found: " + friendFrn, exception.getMessage());
    }
}
