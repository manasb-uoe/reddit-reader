package com.enthusiast94.reddit_reader.app.network;

import com.enthusiast94.reddit_reader.app.models.User;
import com.loopj.android.http.AsyncHttpClient;

/**
 * Created by manas on 08-09-2015.
 */
public class RedditManager {

    protected static final String UNAUTH_API_BASE = "https://www.reddit.com";
    protected static final String AUTH_API_BASE = "https://oauth.reddit.com";
    protected static final String USER_AGENT = "android:com.enthusiast94.reddit_reader:v1.0.0 (by /u/enthusiast94)";

    protected static AsyncHttpClient getAsyncHttpClient() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setUserAgent(USER_AGENT);

        // add authorization header if user is authenticated
        User user = AuthManager.getUser();
        if (user != null) {
            client.addHeader("Authorization", "bearer " + user.getAccessToken());
        }

        return client;
    }
}
