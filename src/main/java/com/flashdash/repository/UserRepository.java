package com.flashdash.repository;

import com.flashdash.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByActivationToken(String activationToken);
    List<User> findByDailyNotificationsTrue();
    @Query(value = "SELECT JSON_UNQUOTE(JSON_EXTRACT(u.friends_frn, '$')) FROM users u WHERE u.user_frn = :userFrn", nativeQuery = true)
    List<String> findFriendsByUserFrn(String userFrn);
    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET friends_frn = JSON_ARRAY_APPEND(friends_frn, '$', :friendFrn) WHERE user_frn = :userFrn", nativeQuery = true)
    void addFriend(String userFrn, String friendFrn);
    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET friends_frn = JSON_REMOVE(friends_frn, JSON_UNQUOTE(JSON_SEARCH(friends_frn, 'one', :friendFrn))) WHERE user_frn = :userFrn", nativeQuery = true)
    void removeFriend(String userFrn, String friendFrn);
    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET friends_frn = '[]' WHERE user_frn = :userFrn", nativeQuery = true)
    void removeAllFriends(String userFrn);
}
