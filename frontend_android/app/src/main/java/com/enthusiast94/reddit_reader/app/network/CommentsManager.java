package com.enthusiast94.reddit_reader.app.network;

import com.enthusiast94.reddit_reader.app.App;
import com.enthusiast94.reddit_reader.app.R;
import com.enthusiast94.reddit_reader.app.models.Comment;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manas on 07-09-2015.
 */
public class CommentsManager {

    private static final String UNAUTH_API_BASE = "http://www.reddit.com";

    public static void getComments(String subreddit, String postId, String sort, final Callback<List<Comment>> callback) {
        sort = sort.toLowerCase();

        String commentsUrl = UNAUTH_API_BASE + "/r/" + subreddit + "/comments/" + postId + "/.json?sort=" + sort;

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(commentsUrl, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                List<Comment> comments = new ArrayList<Comment>();

                try {
                    JSONArray children = jsonArray.getJSONObject(1).getJSONObject("data").getJSONArray("children");
                    for (int i=0; i<children.length(); i++) {
                        parseComments(comments, children.getJSONObject(i), 0);
                    }
                } catch (JSONException e) {
                    if (callback != null) callback.onFailure(App.getAppContext().getString(R.string.error_parsing));
                }

                if (callback != null) callback.onSuccess(comments);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (callback != null) callback.onFailure(String.valueOf(statusCode));
            }
        });
    }

    private static void parseComments(List<Comment> comments, JSONObject thread, int level) throws JSONException {
        if (thread.getString("kind").equals("t1")) {
            JSONObject commentData = thread.getJSONObject("data");

            Comment comment = new Comment();
            comment.setFullName(commentData.getString("name"));
            comment.setBody(commentData.getString("body"));
            comment.setLikes(!commentData.isNull("likes") ? commentData.getBoolean("likes") : null);
            comment.setAuthor(commentData.getString("author"));
            comment.setScore(commentData.getInt("score"));
            comment.setCreated(commentData.getString("created_utc"));
            comment.setLevel(level);

            comments.add(comment);

            if (!commentData.isNull("replies")) {
                level++;
                JSONArray children =
                        commentData.getJSONObject("replies").getJSONObject("data").getJSONArray("children");
                for (int i=0; i<children.length(); i++) {
                    parseComments(comments, children.getJSONObject(i), level);
                }
            }
        }
    }
}
