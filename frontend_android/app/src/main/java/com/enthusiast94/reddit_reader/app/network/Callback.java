package com.enthusiast94.reddit_reader.app.network;

/**
 * Created by manas on 03-09-2015.
 */
public interface Callback<T> {
    void onSuccess(T data);
    void onFailure(String message);
}
