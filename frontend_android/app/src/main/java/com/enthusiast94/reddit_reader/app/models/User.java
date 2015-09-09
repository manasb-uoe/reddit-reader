package com.enthusiast94.reddit_reader.app.models;

/**
 * Created by manas on 09-09-2015.
 */
public class User {

    private String username;
    private String accessToken;
    private long expiresIn;

    public User(String username, String accessToken, long expiresIn) {
        this.username = username;
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
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

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
