package com.enthusiast94.reddit_reader.app.network;

import com.enthusiast94.reddit_reader.app.App;
import com.enthusiast94.reddit_reader.app.R;
import com.enthusiast94.reddit_reader.app.models.User;
import com.enthusiast94.reddit_reader.app.utils.Helpers;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
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
    private static final String CLIENT_ID = "Li4KDbjjUs7_Bw";
    private static final String CLIENT_SECRET = "qnS-1cfzgIMDiQWIFamOJv_1a1s";

    public static String getAuthUrl() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("client_id", CLIENT_ID);
        params.put("response_type", "code");
        params.put("state", STATE);
        params.put("redirect_uri", REDIRECT_URI);
        params.put("duration", "permanent"); // in order to receive refresh token
        params.put("scope", "identity,mysubreddits,read,vote");


        return UNAUTH_API_BASE + "/api/v1/authorize.compact?" + Helpers.stringifyParams(params);
    }

    public static void auth(String code, String state, final Callback<User> callback) {
        if (code == null || state == null)
            throw new IllegalArgumentException("'code' and 'state' cannot be null.");

        if (!state.equals(STATE))
            throw new IllegalArgumentException("Response state does not match request state");

        // retrieve access token, refresh token and expires in
        RequestParams params = new RequestParams();
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", REDIRECT_URI);

        AsyncHttpClient client = getAsyncHttpClient(false);
        client.setBasicAuth(CLIENT_ID, CLIENT_SECRET);
        client.post(UNAUTH_API_BASE + "/api/v1/access_token", params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.isNull("error")) {
                        final String accessToken = response.getString("access_token");
                        final String refreshToken = response.getString("refresh_token");
                        final long expiresIn = response.getLong("expires_in");

                        // retrieve username
                        AsyncHttpClient client2 = getAsyncHttpClient(false);
                        client2.addHeader("Authorization", "bearer " + accessToken);
                        client2.get(AUTH_API_BASE + "/api/v1/me.json", new JsonHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    User user = new User(response.getString("name"), accessToken, refreshToken,
                                    expiresIn, System.currentTimeMillis() / 1000);

                                    // clear any existing preferences
                                    Helpers.clearPrefs(App.getAppContext());

                                    Gson gson = new Gson();
                                    Helpers.writeToPrefs(App.getAppContext(), USER_PREFS_KEY, gson.toJson(user));

                                    if (callback != null) callback.onSuccess(user);
                                } catch (JSONException e) {
                                    if (callback != null)
                                        callback.onFailure(App.getAppContext().getString(R.string.error_parsing));
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                if (callback != null) callback.onFailure(errorResponse.toString());
                            }
                        });
                    } else {
                        if (callback != null) callback.onFailure(response.getString("error"));
                    }
                } catch (JSONException e) {
                    if (callback != null)
                        callback.onFailure(App.getAppContext().getResources().getString(R.string.error_parsing));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (callback != null) callback.onFailure(errorResponse.toString());
            }
        });
    }

    public static void refreshAccessToken(final Callback<Void> callback) {
        final User user = getUser();

        if (user == null)
            throw new IllegalStateException("User needs to be authenticated in order to refresh in their access token.");

        RequestParams params = new RequestParams();
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", user.getRefreshToken());

        AsyncHttpClient client = getAsyncHttpClient(false);
        client.setBasicAuth(CLIENT_ID, CLIENT_SECRET);
        client.post(UNAUTH_API_BASE + "/api/v1/access_token", params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.isNull("error")) {
                        final String accessToken = response.getString("access_token");
                        final long expiresIn = response.getLong("expires_in");

                        // update user instance with new data
                        user.setAccessToken(accessToken);
                        user.setExpiresIn(expiresIn);
                        user.setUpdatedAt(System.currentTimeMillis() / 1000);

                        Gson gson = new Gson();
                        Helpers.writeToPrefs(App.getAppContext(), USER_PREFS_KEY, gson.toJson(user));

                        if (callback != null) callback.onSuccess(null);
                    } else {
                        if (callback != null) callback.onFailure(response.getString("error"));
                    }
                } catch (JSONException e) {
                    if (callback != null)
                        callback.onFailure(App.getAppContext().getResources().getString(R.string.error_parsing));
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
