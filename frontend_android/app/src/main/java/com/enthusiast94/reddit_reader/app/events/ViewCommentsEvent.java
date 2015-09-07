package com.enthusiast94.reddit_reader.app.events;

import com.enthusiast94.reddit_reader.app.models.Post;

/**
 * Created by manas on 07-09-2015.
 */
public class ViewCommentsEvent {

    private Post selectedPost;

    public ViewCommentsEvent(Post selectedPost) {
        this.selectedPost = selectedPost;
    }

    public Post getSelectedPost() {
        return selectedPost;
    }
}
