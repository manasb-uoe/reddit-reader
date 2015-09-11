package com.enthusiast94.reddit_reader.app.fragments;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.enthusiast94.reddit_reader.app.R;
import com.enthusiast94.reddit_reader.app.events.ShareContentEvent;
import com.enthusiast94.reddit_reader.app.events.ViewCommentsEvent;
import com.enthusiast94.reddit_reader.app.events.ViewContentEvent;
import com.enthusiast94.reddit_reader.app.events.ViewSubredditPostsEvent;
import com.enthusiast94.reddit_reader.app.models.Post;
import com.enthusiast94.reddit_reader.app.network.AuthManager;
import com.enthusiast94.reddit_reader.app.network.Callback;
import com.enthusiast94.reddit_reader.app.network.PostsManager;
import com.enthusiast94.reddit_reader.app.network.RedditManager;
import com.enthusiast94.reddit_reader.app.utils.Helpers;
import com.enthusiast94.reddit_reader.app.utils.OnItemSelectedListener;
import com.enthusiast94.reddit_reader.app.utils.TextViewLinkHandler;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manas on 03-09-2015.
 */
public class PostsFragment extends Fragment {

    public static final String TAG = PostsFragment.class.getSimpleName();
    private static final String SUBREDDIT_BUNDLE_KEY = "subreddit_key";
    private static final String SORT_BUNDLE_KEY = "sort_key";
    private static final String SHOULD_USE_TOOLBAR_BUNDLE_KEY = "should_use_toolbar_key";
    private static final String POSTS_BUNDLE_KEY = "posts_key";
    private static final String AFTER_BUNDLE_KEY = "after_key";
    private Toolbar toolbar;
    private RecyclerView postsRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PostsAdapter postsAdapter;
    private String subreddit;
    private String sort;
    private String after;
    private ArrayList<Post> posts;
    private boolean canLoadMorePosts = true;

    public static PostsFragment newInstance(String subreddit, String sort, boolean shouldUseToolbar) {
        Bundle bundle = new Bundle();
        bundle.putString(SUBREDDIT_BUNDLE_KEY, subreddit);
        bundle.putString(SORT_BUNDLE_KEY, sort);
        bundle.putBoolean(SHOULD_USE_TOOLBAR_BUNDLE_KEY, shouldUseToolbar);

        PostsFragment postsFragment = new PostsFragment();
        postsFragment.setArguments(bundle);

        return postsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);

        /**
         * Find views
         */

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        postsRecyclerView = (RecyclerView) view.findViewById(R.id.posts_recyclerview);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);

        /**
         * Retrieve info required to load posts
         */

        Bundle bundle = getArguments();
        subreddit = bundle.getString(SUBREDDIT_BUNDLE_KEY);
        final boolean shouldUseToolbar = bundle.getBoolean(SHOULD_USE_TOOLBAR_BUNDLE_KEY);

        if (savedInstanceState == null) {
            sort = bundle.getString(SORT_BUNDLE_KEY);
            after = null;
            posts = new ArrayList<Post>();
        } else {
            sort = savedInstanceState.getString(SORT_BUNDLE_KEY);
            after = savedInstanceState.getString(AFTER_BUNDLE_KEY);
            posts = savedInstanceState.getParcelableArrayList(POSTS_BUNDLE_KEY);
        }

        /**
         * Configure recycler view
         */

        linearLayoutManager = new LinearLayoutManager(getActivity());
        postsRecyclerView.setLayoutManager(linearLayoutManager);
        postsRecyclerView.getItemAnimator().setSupportsChangeAnimations(false);
        postsAdapter = new PostsAdapter();
        postsRecyclerView.setAdapter(postsAdapter);
        // enable endless scrolling of posts
        postsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemThreshold = 3;
                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisibleItemPos = linearLayoutManager.findLastVisibleItemPosition();

                if (!swipeRefreshLayout.isRefreshing() && (lastVisibleItemPos > (totalItemCount - visibleItemThreshold))
                        && canLoadMorePosts) {

                    loadPosts(true);
                }
            }
        });

        /**
         * Configure swipe refresh
         */

        swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                loadPosts(false);
            }
        });

        /**
         * Setup toolbar if it is to be used
         */

        if (shouldUseToolbar) {
            toolbar.setVisibility(View.VISIBLE);

            updateToolbarTitles();
            toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    getActivity().onBackPressed();
                }
            });
            toolbar.inflateMenu(R.menu.menu_posts_fragment);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.action_sort_hot || id == R.id.action_sort_new || id == R.id.action_sort_rising ||
                            id ==R.id.action_sort_controversial || id ==R.id.action_sort_top) {
                        sort = item.getTitle().toString();
                        updateToolbarTitles();
                        loadPosts(false);

                        return true;
                    }

                    return false;
                }
            });
        } else {
            toolbar.setVisibility(View.GONE);
        }

        /**
         * Load posts
         */

        if (savedInstanceState == null) {
            loadPosts(false);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SORT_BUNDLE_KEY, sort);
        outState.putString(AFTER_BUNDLE_KEY, after);
        outState.putParcelableArrayList(POSTS_BUNDLE_KEY, posts);
    }

    private void loadPosts(final boolean shouldAppend) {
        setRefreshIndicatorVisiblity(true);

        PostsManager.getPosts(subreddit, sort, shouldAppend ? this.after : null, new Callback<List<Post>>() {

            @Override
            public void onSuccess(List<Post> data) {
                // only proceed if fragment is still attached to its parent activity
                // this would prevent null pointer exception when adapter tries to use activity context
                if (getActivity() != null) {
                    setRefreshIndicatorVisiblity(false);

                    if (shouldAppend) {
                        postsAdapter.addPosts(data);
                    } else {
                        postsAdapter.setPosts(data);
                    }

                    if (data.size() > 0)
                        after = data.get(data.size()-1).getAfter();

                    canLoadMorePosts = (after != null);
                }
            }

            @Override
            public void onFailure(String message) {
                if (getActivity() != null) {
                    setRefreshIndicatorVisiblity(false);

                    if (message != null)
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * This method is called by the host activity when the user chooses a different sort value.
     */

    public void updateSort(String sort) {
        this.sort = sort;

        loadPosts(false);
    }

    private void setRefreshIndicatorVisiblity(final boolean visiblity) {
        swipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(visiblity);
            }
        });
    }

    private void updateToolbarTitles() {
        toolbar.setTitle(subreddit);
        toolbar.setSubtitle(sort);
    }

    private class PostsAdapter extends RecyclerView.Adapter<PostViewHolder> implements OnItemSelectedListener {

        private int previouslySelectedPosition;
        private int currentlySelectedPosition;

        public PostsAdapter()  {
            previouslySelectedPosition = -1;
            currentlySelectedPosition = -1;
        }

        @Override
        public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(getActivity()).inflate(R.layout.row_posts_recyclerview, viewGroup, false);
            return new PostViewHolder(getActivity(), itemView, this, false);
        }

        @Override
        public void onBindViewHolder(PostViewHolder postViewHolder, int i) {
            postViewHolder.bindItem(posts.get(i), currentlySelectedPosition);
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        @Override
        public void onItemSelected(int position) {
            currentlySelectedPosition = position;

            notifyItemChanged(currentlySelectedPosition);
            notifyItemChanged(previouslySelectedPosition);

            previouslySelectedPosition = currentlySelectedPosition;
        }

        public void addPosts(List<Post> posts) {
            PostsFragment.this.posts.addAll(posts);
            this.notifyDataSetChanged();
        }

        public void setPosts(List<Post> posts) {
            PostsFragment.this.posts = new ArrayList<Post>(posts);
            this.notifyDataSetChanged();
        }
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        private Context context;
        private OnItemSelectedListener onItemSelectedListener;
        private boolean shouldShowSelftext;
        private int upvoteColor;
        private int downvoteColor;
        private int primaryTextColor;
        private View rootLayout;
        private TextView scoreTextView;
        private TextView titleTextView;
        private TextView subredditTextView;
        private TextView numCommentsTextView;
        private TextView createdTextView;
        private ImageView thumbnailImageView;
        private View selftextContainer;
        private TextView selftextTextView;
        private View buttonsContainer;
        private Button viewButton;
        private Button commentsButton;
        private Button upvoteButton;
        private Button downvoteButton;
        private ImageButton moreOptionsButton;

        public PostViewHolder(Context context, View itemView, OnItemSelectedListener onItemSelectedListener, boolean shouldShowSelftext) {
            super(itemView);

            this.context = context;
            this.onItemSelectedListener = onItemSelectedListener;
            this.shouldShowSelftext = shouldShowSelftext;

            Resources res = context.getResources();

            upvoteColor = res.getColor(R.color.reddit_upvote);
            downvoteColor = res.getColor(R.color.reddit_downvote);
            primaryTextColor = res.getColor(R.color.primary_text_default_material_dark);

            rootLayout = itemView.findViewById(R.id.root_layout);
            scoreTextView = (TextView) itemView.findViewById(R.id.score_textview);
            titleTextView = (TextView) itemView.findViewById(R.id.title_textview);
            subredditTextView = (TextView) itemView.findViewById(R.id.subreddit_textview);
            numCommentsTextView = (TextView) itemView.findViewById(R.id.num_comments_textview);
            createdTextView = (TextView) itemView.findViewById(R.id.created_textview);
            thumbnailImageView = (ImageView) itemView.findViewById(R.id.thumbnail_imageview);
            selftextContainer = itemView.findViewById(R.id.self_text_container);
            selftextTextView = (TextView) itemView.findViewById(R.id.self_text_textview);
            buttonsContainer = itemView.findViewById(R.id.buttons_container);
            viewButton = (Button) buttonsContainer.findViewById(R.id.view_button);
            commentsButton = (Button) buttonsContainer.findViewById(R.id.comments_button);
            upvoteButton = (Button) buttonsContainer.findViewById(R.id.upvote_button);
            downvoteButton = (Button) buttonsContainer.findViewById(R.id.downvote_button);
            moreOptionsButton = (ImageButton) buttonsContainer.findViewById(R.id.more_options_button);
        }

        public void bindItem(final Post post, int currentlySelectedPosition) {
            scoreTextView.setText(String.valueOf(post.getScore()));
            titleTextView.setText(post.getTitle());
            subredditTextView.setText(post.getSubreddit());
            numCommentsTextView.setText(post.getNumComments() + " " + context.getResources().getString(R.string.label_comments));
            createdTextView.setText(post.getCreated());

            if (shouldShowSelftext && !post.getSelftext().equals("null")) {
                selftextContainer.setVisibility(View.VISIBLE);
                selftextTextView.setText(Helpers.trimTrailingWhitespace(Html.fromHtml(Html.fromHtml(post.getSelftext()).toString())));
                selftextTextView.setMovementMethod(new TextViewLinkHandler() {
                    @Override
                    public void onLinkClick(String url) {
                        EventBus.getDefault().post(new ViewContentEvent(null, url));
                    }
                });
            } else {
                selftextContainer.setVisibility(View.GONE);
            }

            if (post.getThumbnail() != null) {
                thumbnailImageView.setVisibility(View.VISIBLE);
                Glide.with(context).load(post.getThumbnail()).crossFade().into(thumbnailImageView);
            } else {
                thumbnailImageView.setVisibility(View.GONE);
            }

            if (getAdapterPosition() == currentlySelectedPosition) {
                rootLayout.setBackgroundResource(R.color.selected_item_background);
                buttonsContainer.setVisibility(View.VISIBLE);
            } else {
                buttonsContainer.setVisibility(View.GONE);
                rootLayout.setBackgroundResource(0);
            }

            if (AuthManager.isUserAuthenticated()) {
                setColorsAccordingToVote(post.getLikes());
            } else {
                upvoteButton.setEnabled(false);
                downvoteButton.setEnabled(false);
            }

            // set click listeners
            View.OnClickListener onClickListener = new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.root_layout:
                        case R.id.self_text_textview:
                            onItemSelectedListener.onItemSelected(getAdapterPosition());
                            break;
                        case R.id.view_button:
                            // if its a self post, open comments directly
                            if (post.isSelf()) {
                                EventBus.getDefault().post(new ViewCommentsEvent(post));
                            } else {
                                EventBus.getDefault().post(new ViewContentEvent(post.getTitle(), post.getUrl()));
                            }
                            break;
                        case R.id.comments_button:
                            EventBus.getDefault().post(new ViewCommentsEvent(post));
                            break;
                        case R.id.thumbnail_imageview:
                            EventBus.getDefault().post(new ViewContentEvent(post.getTitle(), post.getUrl()));
                            break;
                        case R.id.upvote_button:
                            if (post.getLikes() == -1 || post.getLikes() == 0) {
                                post.setLikes(true);
                                post.setScore(post.getScore() + 1);
                            } else {
                                post.setLikes(null);
                                post.setScore(post.getScore() - 1);
                            }
                            RedditManager.vote(post.getFullName(), post.getLikes(), null);
                            setColorsAccordingToVote(post.getLikes());
                            scoreTextView.setText(String.valueOf(post.getScore()));
                            break;
                        case R.id.downvote_button:
                            if (post.getLikes() == 1 || post.getLikes() == 0) {
                                post.setLikes(false);
                                post.setScore(post.getScore() - 1);
                            } else {
                                post.setLikes(null);
                                post.setScore(post.getScore() + 1);
                            }
                            RedditManager.vote(post.getFullName(), post.getLikes(), null);
                            setColorsAccordingToVote(post.getLikes());
                            scoreTextView.setText(String.valueOf(post.getScore()));
                            break;
                        case R.id.more_options_button:
                            AlertDialog moreOptionsDialog = new AlertDialog.Builder(context)
                                    .setItems(R.array.more_post_options, new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (i == 0) {  // View subreddit
                                                EventBus.getDefault().post(new ViewSubredditPostsEvent(post.getSubreddit(),
                                                        context.getResources().getString(R.string.action_sort_hot)));
                                            } else if (i == 1) { // Share link
                                                EventBus.getDefault().post(new ShareContentEvent(post.getUrl(), "text/plain"));
                                            } else if (i == 2) { // Copy link
                                                ClipboardManager clipboard =
                                                        (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                                clipboard.setPrimaryClip(ClipData.newPlainText(null, post.getUrl()));
                                                Toast.makeText(context, R.string.success_post_link_copied_to_clipboard,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .create();
                            moreOptionsDialog.show();
                            break;
                    }
                }
            };

            itemView.setOnClickListener(onClickListener);
            viewButton.setOnClickListener(onClickListener);
            commentsButton.setOnClickListener(onClickListener);
            selftextTextView.setOnClickListener(onClickListener);
            thumbnailImageView.setOnClickListener(onClickListener);
            upvoteButton.setOnClickListener(onClickListener);
            downvoteButton.setOnClickListener(onClickListener);
            moreOptionsButton.setOnClickListener(onClickListener);
        }

        /**
         * Sets colors for various ui elements within viewholder according to post's current vote status
         */

        private void setColorsAccordingToVote(int likes) {
            if (likes == 1) {
                upvoteButton.setTextColor(upvoteColor);
                scoreTextView.setTextColor(upvoteColor);
                downvoteButton.setTextColor(primaryTextColor);
            } else if (likes == -1) {
                downvoteButton.setTextColor(downvoteColor);
                scoreTextView.setTextColor(downvoteColor);
                upvoteButton.setTextColor(primaryTextColor);
            } else {
                upvoteButton.setTextColor(primaryTextColor);
                scoreTextView.setTextColor(primaryTextColor);
                downvoteButton.setTextColor(primaryTextColor);
            }
        }
    }
}
