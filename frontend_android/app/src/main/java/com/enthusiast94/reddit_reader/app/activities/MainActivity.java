package com.enthusiast94.reddit_reader.app.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.enthusiast94.reddit_reader.app.R;
import com.enthusiast94.reddit_reader.app.fragments.PostsFragment;
import com.enthusiast94.reddit_reader.app.models.Subreddit;
import com.enthusiast94.reddit_reader.app.network.Callback;
import com.enthusiast94.reddit_reader.app.network.SubredditsManager;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar appBar;
    private TabLayout subredditTabs;
    private PostsFragment postsFragment;
    private String subreddit;
    private String sort;
    private static final String SUBREDDIT_BUNDLE_KEY = "subreddit_key";
    private static final String SORT_BUNDLE_KEY = "sort_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Find views
         */

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        subredditTabs = (TabLayout) findViewById(R.id.subreddit_tabs);

        /**
         * Restore saved values from instance state. If not available, set default vaules.
         */

        if (savedInstanceState == null) {
            subreddit = getResources().getString(R.string.front_page);
            sort = getResources().getString(R.string.action_sort_hot);
        } else {
            subreddit = savedInstanceState.getString(SUBREDDIT_BUNDLE_KEY);
            sort = savedInstanceState.getString(SORT_BUNDLE_KEY);
        }

        /**
         * Setup AppBar
         */

        setSupportActionBar(toolbar);
        appBar = getSupportActionBar();
        updateAppBarTitles();

        /**
         * Setup tabs
         */

        SubredditsManager.getSubreddits(new Callback<List<Subreddit>>() {

            @Override
            public void onSuccess(List<Subreddit> data) {
                // add subreddit tabs
                for (Subreddit subreddit : data) {
                    subredditTabs.addTab(subredditTabs.newTab().setText(subreddit.getName()));
                }

                // select active tab
                for (int i = 0; i < subredditTabs.getTabCount(); i++) {
                    TabLayout.Tab tab = subredditTabs.getTabAt(i);
                    if (tab.getText().toString().equals(subreddit)) {
                        tab.select();
                        break;
                    }
                }

                // set selection listener
                subredditTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        subreddit = tab.getText().toString();
                        postsFragment.loadPosts(subreddit, sort);
                        updateAppBarTitles();
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });
            }

            @Override
            public void onFailure(String message) {

            }
        });

        /**
         * Add posts fragment dynamically if it doesn't already exist
         */

        postsFragment = (PostsFragment) getSupportFragmentManager().findFragmentByTag(PostsFragment.TAG);
        if (postsFragment == null) {
            postsFragment = PostsFragment.newInstance(subreddit, sort);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, postsFragment, PostsFragment.TAG)
                    .commit();
        }
    }

    private void updateAppBarTitles() {
        appBar.setTitle(subreddit);
        appBar.setSubtitle(sort);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SUBREDDIT_BUNDLE_KEY, subreddit);
        outState.putString(SORT_BUNDLE_KEY, sort);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort_hot || id == R.id.action_sort_new || id == R.id.action_sort_rising ||
                id ==R.id.action_sort_controversial || id ==R.id.action_sort_top) {
            sort = item.getTitle().toString();
            updateAppBarTitles();
            postsFragment.loadPosts(subreddit, sort);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
