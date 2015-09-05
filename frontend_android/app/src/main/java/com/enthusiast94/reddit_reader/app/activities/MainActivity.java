package com.enthusiast94.reddit_reader.app.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.enthusiast94.reddit_reader.app.R;
import com.enthusiast94.reddit_reader.app.events.ViewContentEvent;
import com.enthusiast94.reddit_reader.app.fragments.ContentViewerFragment;
import com.enthusiast94.reddit_reader.app.fragments.PostsFragment;
import com.enthusiast94.reddit_reader.app.models.Subreddit;
import com.enthusiast94.reddit_reader.app.network.Callback;
import com.enthusiast94.reddit_reader.app.network.SubredditsManager;
import de.greenrobot.event.EventBus;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar appBar;
    private TabLayout subredditTabs;
    private ViewPager viewPager;
    private SubredditPagerAdapter subredditPagerAdapter;
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
        viewPager = (ViewPager) findViewById(R.id.viewpager);

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
        updateAppBarTitlesWithPostInfo();

        /**
         * Setup tabs and viewpager
         */

        SubredditsManager.getSubreddits(new Callback<List<Subreddit>>() {

            @Override
            public void onSuccess(List<Subreddit> data) {
                // setup view pager
                subredditPagerAdapter = new SubredditPagerAdapter(data);
                viewPager.setAdapter(subredditPagerAdapter);
                viewPager.addOnPageChangeListener(subredditPagerAdapter);

                // bind view pager to tabs
                subredditTabs.setupWithViewPager(viewPager);

                // select active tab
                for (int i = 0; i < subredditTabs.getTabCount(); i++) {
                    TabLayout.Tab tab = subredditTabs.getTabAt(i);
                    if (tab.getText().toString().equals(subreddit)) {
                        tab.select();
                        break;
                    }
                }
            }

            @Override
            public void onFailure(String message) {
                // TODO display error message
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(ViewContentEvent event) {
        if (event.getUrl().contains("youtube.com")) {
            Intent viewIntent = new Intent();
            viewIntent.setAction(Intent.ACTION_VIEW);
            viewIntent.setData(Uri.parse(event.getUrl()));

            if (viewIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(viewIntent);
                return;
            }
        }

        ContentViewerFragment contentViewerFragment =
                ContentViewerFragment.newInstance(event.getContentTitle(), event.getUrl());
        FragmentTransaction fTransaction = getSupportFragmentManager().beginTransaction();
        fTransaction.add(android.R.id.content, contentViewerFragment);
        fTransaction.addToBackStack(null);
        fTransaction.commit();
    }

    private void updateAppBarTitlesWithPostInfo() {
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
            updateAppBarTitlesWithPostInfo();
            subredditPagerAdapter.getCurrentFragment().loadPosts(subreddit, sort);
            return true;
        } else if (id == R.id.action_go_to_subreddit) {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_go_tp_subreddit, null);
            final EditText subredditEditText = (EditText) dialogView.findViewById(R.id.subreddit_edittext);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.action_go_to_subreddit)
                    .setView(dialogView)
                    .setPositiveButton(R.string.action_go, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String subredditName = subredditEditText.getText().toString();
                            if (TextUtils.isEmpty(subredditName)) {
                                Toast.makeText(MainActivity.this, R.string.error_subreddit_name_required, Toast.LENGTH_SHORT)
                                        .show();
                            } else {
                                FragmentTransaction fTransaction = getSupportFragmentManager().beginTransaction();
                                fTransaction.add(android.R.id.content,
                                        PostsFragment.newInstance(subredditEditText.getText().toString(), sort, true));
                                fTransaction.addToBackStack(null);
                                fTransaction.commit();
                            }
                        }
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .create();
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private class SubredditPagerAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {

        private List<Subreddit> subreddits;

        public SubredditPagerAdapter(List<Subreddit> subreddits) {
            super(getSupportFragmentManager());

            this.subreddits = subreddits;
        }

        @Override
        public Fragment getItem(int position) {
            return PostsFragment.newInstance(subreddits.get(position).getName(), sort, false);
        }

        @Override
        public int getCount() {
            return subreddits.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return subreddits.get(position).getName();
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            subreddit = subreddits.get(position).getName();
            updateAppBarTitlesWithPostInfo();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        public PostsFragment getCurrentFragment() {
            return (PostsFragment) instantiateItem(viewPager, viewPager.getCurrentItem());
        }
    }
}
