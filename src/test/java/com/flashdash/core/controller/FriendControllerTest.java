package com.flashdash.core.controller;

import com.flashdash.core.TestUtils;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.User;
import com.flashdash.core.service.FriendService;
import com.flashdash.core.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.UserResponse;
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

@SpringBootTest
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
    private String friendFrn;

    @BeforeEach
    void setUp() {
        testUser = TestUtils.createUser();
        friendUser = TestUtils.createUser();
        userFrn = testUser.getUserFrn();
        friendFrn = friendUser.getUserFrn();

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
        UserResponse friendResponse = new UserResponse();
        friendResponse.setUserFrn(friendFrn);
        friendResponse.setFirstName(friendUser.getFirstName());
        friendResponse.setLastName(friendUser.getLastName());

        List<User> friends = List.of(friendUser);
        List<UserResponse> expectedResponses = List.of(friendResponse);

        when(friendService.getFriends(userFrn)).thenReturn(friends);
        when(entityToResponseMapper.mapToUserResponse(friends)).thenReturn(expectedResponses);

        // Act
        ResponseEntity<List<UserResponse>> responseEntity = friendController.getFriends();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponses, responseEntity.getBody());
    }

    @Test
    void shouldGetFriendSuccessfully() {
        // Arrange
        UserResponse friendResponse = new UserResponse();
        friendResponse.setUserFrn(friendFrn);
        friendResponse.setFirstName(friendUser.getFirstName());
        friendResponse.setLastName(friendUser.getLastName());

        when(friendService.getFriend(userFrn, friendFrn)).thenReturn(friendUser);
        when(entityToResponseMapper.mapToUserResponse(friendUser)).thenReturn(friendResponse);

        // Act
        ResponseEntity<UserResponse> responseEntity = friendController.getFriend(friendFrn);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(friendResponse, responseEntity.getBody());
    }

    @Test
    void shouldThrowExceptionWhenFriendNotFound() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E404005, "Friend not found: " + friendFrn))
                .when(friendService).getFriend(userFrn, friendFrn);

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> friendController.getFriend(friendFrn)
        );
        assertEquals(ErrorCode.E404005, exception.getErrorCode());
        assertEquals("Friend not found: " + friendFrn, exception.getMessage());
    }

    @Test
    void shouldDeleteFriendSuccessfully() {
        // Arrange
        doNothing().when(friendService).deleteFriend(userFrn, friendFrn);

        // Act
        ResponseEntity<Void> responseEntity = friendController.deleteFriend(friendFrn);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(friendService, times(1)).deleteFriend(userFrn, friendFrn);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentFriend() {
        // Arrange
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
