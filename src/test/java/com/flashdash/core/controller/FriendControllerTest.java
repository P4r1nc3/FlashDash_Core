package com.flashdash.core.controller;

import com.flashdash.core.FlashDashCoreApplication;
import com.flashdash.core.TestUtils;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.FriendInvitation;
import com.flashdash.core.model.User;
import com.flashdash.core.service.FriendService;
import com.flashdash.core.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.FriendInvitationResponseReceived;
import com.p4r1nc3.flashdash.core.model.FriendInvitationResponseSent;
import com.p4r1nc3.flashdash.core.model.FriendResponse;
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

@SpringBootTest(classes = FlashDashCoreApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FriendControllerTest {

    @Autowired
    private FriendController friendController;

    @MockitoBean
    private FriendService friendService;

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
    void shouldGetFriendsSuccessfully() {
        // Arrange
        FriendResponse friendResponse = new FriendResponse();
        friendResponse.setUserFrn(friendUser.getUserFrn());
        friendResponse.setFirstName(friendUser.getFirstName());
        friendResponse.setLastName(friendUser.getLastName());

        List<User> friends = List.of(friendUser);
        List<FriendResponse> expectedResponses = List.of(friendResponse);

        when(friendService.getFriends(userFrn)).thenReturn(friends);
        when(entityToResponseMapper.mapToFriendResponse(friends)).thenReturn(expectedResponses);

        // Act
        ResponseEntity<List<FriendResponse>> responseEntity = friendController.getFriends();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponses, responseEntity.getBody());
    }

    @Test
    void shouldSendFriendInvitationSuccessfully() {
        // Arrange
        String recipientEmail = friendUser.getEmail();
        doNothing().when(friendService).sendFriendInvitation(userFrn, recipientEmail);

        // Act
        ResponseEntity<Void> responseEntity = friendController.sendFriendInvitation(recipientEmail);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(friendService, times(1)).sendFriendInvitation(userFrn, recipientEmail);
    }

    @Test
    void shouldGetReceivedFriendInvitationsSuccessfully() {
        // Arrange
        List<FriendInvitation> invitations = List.of(TestUtils.createFriendInvitation(friendUser, testUser));
        List<FriendInvitationResponseReceived> expectedResponses = List.of(new FriendInvitationResponseReceived());

        when(friendService.getReceivedFriendInvitations(userFrn)).thenReturn(invitations);
        when(entityToResponseMapper.mapToReceivedResponse(invitations)).thenReturn(expectedResponses);

        // Act
        ResponseEntity<List<FriendInvitationResponseReceived>> responseEntity = friendController.getReceivedInvitations();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponses, responseEntity.getBody());
    }

    @Test
    void shouldGetSentFriendInvitationsSuccessfully() {
        // Arrange
        List<FriendInvitation> invitations = List.of(TestUtils.createFriendInvitation(testUser, friendUser));
        List<FriendInvitationResponseSent> expectedResponses = List.of(new FriendInvitationResponseSent());

        when(friendService.getSentFriendInvitations(userFrn)).thenReturn(invitations);
        when(entityToResponseMapper.mapToSentResponse(invitations)).thenReturn(expectedResponses);

        // Act
        ResponseEntity<List<FriendInvitationResponseSent>> responseEntity = friendController.getSentInvitations();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponses, responseEntity.getBody());
    }

    @Test
    void shouldRespondToInvitationSuccessfully() {
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
    void shouldDeleteFriendSuccessfully() {
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
                .when(friendService).sendFriendInvitation(userFrn, testUser.getEmail());

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> friendController.sendFriendInvitation(testUser.getEmail())
        );
        assertEquals(ErrorCode.E403003, exception.getErrorCode());
        assertEquals("You cannot send an invitation to yourself.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserIsAlreadyAFriend() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E409003, "You are already friends with this user."))
                .when(friendService).sendFriendInvitation(userFrn, friendUser.getEmail());

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> friendController.sendFriendInvitation(friendUser.getEmail())
        );
        assertEquals(ErrorCode.E409003, exception.getErrorCode());
        assertEquals("You are already friends with this user.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentFriend() {
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

