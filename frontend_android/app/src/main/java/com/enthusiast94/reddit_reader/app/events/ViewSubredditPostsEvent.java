package com.enthusiast94.reddit_reader.app.events;

/**
 * Created by manas on 10-09-2015.
 */
public class ViewSubredditPostsEvent {

    private String subreddit;
    private String sort;

    public ViewSubredditPostsEvent(String subreddit, String sort) {
        this.subreddit = subreddit;
        this.sort = sort;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public String getSort() {
        return sort;
    }
}
