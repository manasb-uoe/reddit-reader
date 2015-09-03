package com.enthusiast94.reddit_reader.app.models;

/**
 * Created by manas on 03-09-2015.
 */
public class Subreddit {

    private String id;
    private String displayName;

    public Subreddit(String id, String name) {
        this.id = id;
        this.displayName = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return displayName;
    }

    public void setName(String name) {
        this.displayName = name;
    }
}
