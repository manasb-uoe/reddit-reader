package com.enthusiast94.reddit_reader.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by manas on 03-09-2015.
 */
public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;
    }

    public static Context getAppContext() {
        return context;
    }
}
