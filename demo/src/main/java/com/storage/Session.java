package com.storage;

import java.time.LocalDateTime;

public class Session {
    private String sessionId;
    private String username;
    private LocalDateTime expiryTime;

    public Session(String sessionId, String username, LocalDateTime expiryTime) {
        this.sessionId = sessionId;
        this.username = username;
        this.expiryTime = expiryTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
