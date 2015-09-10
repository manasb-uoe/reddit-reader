package com.enthusiast94.reddit_reader.app.events;

/**
 * Created by manas on 10-09-2015.
 */
public class ShareContentEvent {

    private String content;
    private String mimeType;

    public ShareContentEvent(String content, String mimeType) {
        this.content = content;
        this.mimeType = mimeType;
    }

    public String getContent() {
        return content;
    }

    public String getMimeType() {
        return mimeType;
    }
}
