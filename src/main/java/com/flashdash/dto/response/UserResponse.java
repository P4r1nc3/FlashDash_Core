package com.flashdash.dto.response;

import java.time.LocalDateTime;

public class UserResponse {
    private String userFrn; // ✅ Dodane
    private String firstName;
    private String lastName;
    private String email;
    private boolean dailyNotifications;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserResponse() {}

    // 🔹 Nowy konstruktor tylko dla FRN
    public UserResponse(String userFrn) {
        this.userFrn = userFrn;
    }

    // 🔹 Istniejący konstruktor dla pełnych danych użytkownika
    public UserResponse(String firstName, String lastName, String email, boolean dailyNotifications, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dailyNotifications = dailyNotifications;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getUserFrn() {
        return userFrn;
    }

    public void setUserFrn(String userFrn) {
        this.userFrn = userFrn;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isDailyNotifications() {
        return dailyNotifications;
    }

    public void setDailyNotifications(boolean dailyNotifications) {
        this.dailyNotifications = dailyNotifications;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
