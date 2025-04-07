package com.flashdash.core.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
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
