package com.enthusiast94.reddit_reader.app.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.enthusiast94.reddit_reader.app.R;
import com.enthusiast94.reddit_reader.app.fragments.PostsFragment;


public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout sortTabs;
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
        sortTabs = (TabLayout) findViewById(R.id.sort_tabs);

        /**
         * Restore saved values from instance state
         */

        if (savedInstanceState != null) {
            subreddit = savedInstanceState.getString(SUBREDDIT_BUNDLE_KEY);
            sort = savedInstanceState.getString(SORT_BUNDLE_KEY);
        }

        /**
         * Setup AppBar
         */

        setSupportActionBar(toolbar);
        setAppBarTitle(subreddit);

        /**
         * Setup tabs
         */

        // add all sort tabs
        sortTabs.addTab(sortTabs.newTab().setText(R.string.sort_hot));
        sortTabs.addTab(sortTabs.newTab().setText(R.string.sort_new));
        sortTabs.addTab(sortTabs.newTab().setText(R.string.sort_rising));
        sortTabs.addTab(sortTabs.newTab().setText(R.string.sort_controversial));
        sortTabs.addTab(sortTabs.newTab().setText(R.string.sort_top));
        sortTabs.addTab(sortTabs.newTab().setText(R.string.sort_gilded));
        sortTabs.addTab(sortTabs.newTab().setText(R.string.sort_promoted));

        // select active tab
        for (int i=0; i<sortTabs.getTabCount(); i++) {
            TabLayout.Tab tab = sortTabs.getTabAt(i);
            if (tab.getText().toString().equals(sort)) {
                tab.select();
                break;
            }
        }

        // set selection listener
        sortTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                sort = tab.getText().toString();
                postsFragment.loadPosts(subreddit, sort.toLowerCase());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

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

    private void setAppBarTitle(String subreddit) {
        if (subreddit == null) {
            setTitle(R.string.front_page);
        } else {
            setTitle(subreddit);
        }
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
