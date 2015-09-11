package com.enthusiast94.reddit_reader.app.models;

/**
 * Created by manas on 09-09-2015.
 */
public class User {

    private String username;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private long updatedAt;

    public User(String username, String accessToken, String refreshToken, long expiresIn, long updatedAt) {
        this.username = username;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.updatedAt = updatedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean hasAccessTokenExpired() {
        return ((System.currentTimeMillis() / 1000) - this.getUpdatedAt()) >= this.getExpiresIn();
    }
}
