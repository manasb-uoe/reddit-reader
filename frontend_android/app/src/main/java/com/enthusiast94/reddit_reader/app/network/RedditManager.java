package com.enthusiast94.reddit_reader.app.network;

import com.loopj.android.http.AsyncHttpClient;

/**
 * Created by manas on 08-09-2015.
 */
public class RedditManager {

    protected static final String UNAUTH_API_BASE = "https://www.reddit.com";
    protected static final String AUTH_API_BASE = "https://oauth.reddit.com";
    protected static final String USER_AGENT = "android:com.enthusiast94.reddit_reader:v1.0.0 (by /u/enthusiast94)";

    protected static AsyncHttpClient getAsyncHttpClient() {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setUserAgent(USER_AGENT);

        return asyncHttpClient;
    }
}
