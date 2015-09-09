package com.enthusiast94.reddit_reader.app.network;

import com.enthusiast94.reddit_reader.app.App;
import com.enthusiast94.reddit_reader.app.R;
import com.enthusiast94.reddit_reader.app.models.User;
import com.enthusiast94.reddit_reader.app.utils.Helpers;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by manas on 09-09-2015.
 */
public class AuthManager extends RedditManager {

    private static final String STATE = "reddit_reader_android";
    private static final String USER_PREFS_KEY = "user_prefs_key";
    public static final String REDIRECT_URI = "http://localhost:4000/oauth2-callback";

    public static String getAuthUrl() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("client_id", "WXV8JYusTqO8NQ");
        params.put("response_type", "token");
        params.put("state", STATE);
        params.put("redirect_uri", REDIRECT_URI);
        params.put("scope", "identity,mysubreddits,read,vote");

        return UNAUTH_API_BASE + "/api/v1/authorize.compact?" + Helpers.stringifyParams(params);
    }

    public static void auth(final String accessToken, String state, final String expiresIn, final Callback<User> callback) {
        if (accessToken == null || state == null || expiresIn == null)
            throw new IllegalArgumentException("\"'accessToken', 'state' and 'expiresIn' cannot be null\"");

        if (!state.equals(STATE)) throw new IllegalArgumentException("Response state does not match request state");

        AsyncHttpClient client = getAsyncHttpClient();
        client.addHeader("Authorization", "bearer " + accessToken);
        client.get(AUTH_API_BASE + "/api/v1/me.json", new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    User user = new User(response.getString("name"), accessToken, Long.valueOf(expiresIn));

                    // clear any existing preferences
                    Helpers.clearPrefs(App.getAppContext());

                    Gson gson = new Gson();
                    Helpers.writeToPrefs(App.getAppContext(), USER_PREFS_KEY, gson.toJson(user));

                    if (callback != null) callback.onSuccess(user);
                } catch (JSONException e) {
                    if (callback != null) callback.onFailure(App.getAppContext().getString(R.string.error_parsing));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (callback != null) callback.onFailure(errorResponse.toString());
            }
        });
    }

    public static User getUser() {
        String userJson = Helpers.readFromPrefs(App.getAppContext(), USER_PREFS_KEY);

        if (userJson != null) {
            Gson gson = new Gson();
            return gson.fromJson(userJson, User.class);
        }

        return null;
    }

    public static void deauth() {
        Helpers.clearPrefs(App.getAppContext());
    }

    public static boolean isUserAuthenticated() {
        return Helpers.readFromPrefs(App.getAppContext(), USER_PREFS_KEY) != null;
    }
}
