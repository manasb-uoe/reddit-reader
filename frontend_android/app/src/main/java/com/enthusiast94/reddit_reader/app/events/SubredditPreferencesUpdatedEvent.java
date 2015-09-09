package com.enthusiast94.reddit_reader.app.events;

import com.enthusiast94.reddit_reader.app.models.Subreddit;

import java.util.List;

/**
 * Created by manas on 05-09-2015.
 */
public class SubredditPreferencesUpdatedEvent {

    private List<Subreddit> subreddits;

    public SubredditPreferencesUpdatedEvent(List<Subreddit> subreddits) {
        this.subreddits = subreddits;
    }

    public List<Subreddit> getSubreddits() {
        return subreddits;
    }
}
