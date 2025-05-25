package com.flashdash.core.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @Column(name = "user_frn", nullable = false, length = 256)
    private String userFrn;

    @Column(name = "username", nullable = false, length = 256)
    private String username;

    @Column(name = "first_name", nullable = false, length = 256)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 256)
    private String lastName;

    @Column(name = "email", unique = true, nullable = false, length = 256)
    private String email;

    @Column(name = "password", nullable = false, length = 256)
    private String password;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "activation_token", unique = true, nullable = false, length = 256)
    private String activationToken;

    @Column(name = "study_time", nullable = false, length = 256)
    private Duration studyTime;

    @Column(name = "games_played", nullable = false, length = 256)
    private int gamesPlayed;

    @Column(name = "points", nullable = false, length = 256)
    private int points;

    @Column(name = "strike", nullable = false, length = 256)
    private int strike;

    @Column(name = "friends_frn", columnDefinition = "JSON", nullable = false)
    private String friendsFrn = "[]";

    public String getUserFrn() {
        return userFrn;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getActivationToken() {
        return activationToken;
    }

    public Duration getStudyTime() {
        return studyTime;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getPoints() {
        return points;
    }

    public int getStrike() {
        return strike;
    }

    public String getFriendsFrn() {
        return friendsFrn;
    }

    public void setUserFrn(String userFrn) {
        this.userFrn = userFrn;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setActivationToken(String activationToken) {
        this.activationToken = activationToken;
    }

    public void setStudyTime(Duration studyTime) {
        this.studyTime = studyTime;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setStrike(int strike) {
        this.strike = strike;
    }

    public void setFriendsFrn(String friendsFrn) {
        this.friendsFrn = friendsFrn;
    }

    public List<String> getFriendsFrnList() {
        try {
            if (friendsFrn == null || friendsFrn.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(friendsFrn, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setFriendsFrnList(List<String> friends) {
        try {
            this.friendsFrn = objectMapper.writeValueAsString(friends);
        } catch (JsonProcessingException e) {
            this.friendsFrn = "[]";
        }
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
