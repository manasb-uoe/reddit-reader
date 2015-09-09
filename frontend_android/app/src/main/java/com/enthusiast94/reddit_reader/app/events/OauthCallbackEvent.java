package com.enthusiast94.reddit_reader.app.events;

/**
 * Created by manas on 09-09-2015.
 */
public class OauthCallbackEvent {

    private String accessToken;
    private String expiresIn;
    private String state;
    private String error;

    public OauthCallbackEvent(String accessToken, String expiresIn, String state, String error) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.state = state;
        this.error = error;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public String getState() {
        return state;
    }

    public String getError() {
        return error;
    }
}

