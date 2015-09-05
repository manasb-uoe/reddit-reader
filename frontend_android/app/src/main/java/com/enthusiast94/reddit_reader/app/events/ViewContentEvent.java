package com.enthusiast94.reddit_reader.app.events;

/**
 * Created by manas on 05-09-2015.
 */
public class ViewContentEvent {

    private String title;
    private String url;

    public ViewContentEvent(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
