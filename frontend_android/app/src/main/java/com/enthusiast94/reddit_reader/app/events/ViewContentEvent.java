package com.enthusiast94.reddit_reader.app.events;

/**
 * Created by manas on 05-09-2015.
 */
public class ViewContentEvent {

    private String contentTitle;
    private String url;

    public ViewContentEvent(String contentTitle, String url) {
        this.contentTitle = contentTitle;
        this.url = url;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public String getUrl() {
        return url;
    }
}
