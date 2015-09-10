package com.enthusiast94.reddit_reader.app.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by manas on 10-09-2015.
 */
public abstract class EndlessRecyclerScrollListener extends RecyclerView.OnScrollListener {

    private static final String TAG = EndlessRecyclerScrollListener.class.getSimpleName();

    private LinearLayoutManager linearLayoutManager;
    private int previousTotal; // The total number of items in the data set after the last load.
    private boolean isLoading; // True if we are still waiting for the last set of data to load.
    private int visibleItemThreshold; // The minimum amount of items to have below your current scroll position before loading more.
    private long lastLoadTime; // Time when onLoadMore was last called
    private int loadInterval; // Time minimum time that needs to pass between two consecutive calls to onLoadMore.

    public EndlessRecyclerScrollListener(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;

        previousTotal = 0;
        isLoading = false;
        visibleItemThreshold = 3;
        loadInterval = 2000;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int totalItemCount = linearLayoutManager.getItemCount();
        int lastVisibleItemPos = linearLayoutManager.findLastVisibleItemPosition();

        if (isLoading) {
            if (totalItemCount > previousTotal) {
                isLoading = false;
                previousTotal = totalItemCount;
            }
        }

        if (!isLoading && ((System.currentTimeMillis() - lastLoadTime) > loadInterval) &&(lastVisibleItemPos > (totalItemCount - visibleItemThreshold))) {
            // end has been reached
            onLoadMore();
            isLoading = true;
            lastLoadTime = System.currentTimeMillis();
        }
    }

    public abstract void onLoadMore();
}
