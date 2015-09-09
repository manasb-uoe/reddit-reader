package com.enthusiast94.reddit_reader.app.events;

import com.enthusiast94.reddit_reader.app.models.User;

/**
 * Created by manas on 09-09-2015.
 */
public class AuthenticatedEvent {

    private User user;

    public AuthenticatedEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
