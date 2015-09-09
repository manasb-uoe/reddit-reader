package com.enthusiast94.reddit_reader.app.network;

import com.enthusiast94.reddit_reader.app.App;
import com.enthusiast94.reddit_reader.app.R;
import com.enthusiast94.reddit_reader.app.models.User;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.apache.http.Header;
import org.json.JSONObject;

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

    public static void vote(String itemFullName, int voteDir, final Callback<Void> callback) {
        if (itemFullName == null) throw new IllegalArgumentException("'itemId' cannot be null");

        RequestParams params = new RequestParams();
        params.put("id", itemFullName);
        params.put("dir", voteDir);

        if (AuthManager.isUserAuthenticated()) {
            getAsyncHttpClient().post(AUTH_API_BASE + "/api/vote", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (callback != null) callback.onSuccess(null);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if (callback != null) callback.onFailure(errorResponse.toString());
                }
            });
        } else {
            if (callback != null)
                callback.onFailure(App.getAppContext().getResources().getString(R.string.error_not_authorized));
        }
    }
}
