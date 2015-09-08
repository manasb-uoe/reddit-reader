package com.enthusiast94.reddit_reader.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.enthusiast94.reddit_reader.app.events.ViewCommentsEvent;
import com.enthusiast94.reddit_reader.app.events.ViewContentEvent;
import com.enthusiast94.reddit_reader.app.models.Post;
import com.enthusiast94.reddit_reader.app.network.Callback;
import com.enthusiast94.reddit_reader.app.network.PostsManager;
import com.enthusiast94.reddit_reader.app.utils.Helpers;
import com.enthusiast94.reddit_reader.app.utils.OnItemSelectedListener;
import com.enthusiast94.reddit_reader.app.utils.TextViewLinkHandler;
import de.greenrobot.event.EventBus;

import java.util.List;

/**
 * Created by manas on 03-09-2015.
 */
public class PostsFragment extends Fragment {

    public static final String TAG = Fragment.class.getSimpleName();
    private static final String SUBREDDIT_BUNDLE_KEY = "subreddit_key";
    private static final String SORT_BUNDLE_KEY = "sort_key";
    private static final String SHOULD_USE_TOOLBAR_BUNDLE_KEY = "should_use_toolbar_key";
    private Toolbar toolbar;
    private RecyclerView postsRecyclerView;
    private ProgressBar progressBar;
    private String subreddit;
    private String sort;

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

        // enable options menu
        setHasOptionsMenu(true);

        /**
         * Find views
         */

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        postsRecyclerView = (RecyclerView) view.findViewById(R.id.posts_recyclerview);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_circular);

        /**
         * Retrieve info about posts to load from arguments
         */

        Bundle bundle = getArguments();
        subreddit = bundle.getString(SUBREDDIT_BUNDLE_KEY);
        sort = bundle.getString(SORT_BUNDLE_KEY);
        boolean shouldUseToolbar = bundle.getBoolean(SHOULD_USE_TOOLBAR_BUNDLE_KEY);

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
            toolbar.inflateMenu(R.menu.menu_post_fragment);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.action_sort_hot || id == R.id.action_sort_new || id == R.id.action_sort_rising ||
                            id ==R.id.action_sort_controversial || id ==R.id.action_sort_top) {
                        sort = item.getTitle().toString();
                        updateToolbarTitles();
                        loadPosts(subreddit, sort);

                        return true;
                    }

                    return false;
                }
            });
        } else {
            toolbar.setVisibility(View.GONE);
        }

        /**
         * Load posts and setup recycler view adapter
         */

        loadPosts(subreddit, sort);

        return view;
    }

    public void loadPosts(String subreddit, String sort) {
        progressBar.setVisibility(View.VISIBLE);
        postsRecyclerView.setVisibility(View.INVISIBLE);

        PostsManager.getPosts(subreddit, sort, null, new Callback<List<Post>>() {

            @Override
            public void onSuccess(List<Post> data) {
                progressBar.setVisibility(View.INVISIBLE);
                postsRecyclerView.setVisibility(View.VISIBLE);

                // only proceed if fragment is still attached to its parent activity
                // this would prevent null pointer exception when adapter tries to use activity context
                if (getActivity() != null) {
                    PostsAdapter postsAdapter = new PostsAdapter(getActivity(), data);
                    postsRecyclerView.setAdapter(postsAdapter);
                    postsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    // disable change animation
                    postsRecyclerView.getItemAnimator().setSupportsChangeAnimations(false);
                }
            }

            @Override
            public void onFailure(String message) {
                progressBar.setVisibility(View.INVISIBLE);
                postsRecyclerView.setVisibility(View.VISIBLE);

                if (getActivity() != null) {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateToolbarTitles() {
        toolbar.setTitle(subreddit);
        toolbar.setSubtitle(sort);
    }

    private static class PostsAdapter extends RecyclerView.Adapter<PostViewHolder> implements OnItemSelectedListener {

        private Context context;
        private List<Post> posts;
        private int previouslySelectedPosition;
        private int currentlySelectedPosition;

        public PostsAdapter(Context context, List<Post> posts)  {
            this.context = context;
            this.posts = posts;

            previouslySelectedPosition = -1;
            currentlySelectedPosition = -1;
        }

        @Override
        public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.row_posts_recyclerview, viewGroup, false);
            return new PostViewHolder(context, itemView, this, false);
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
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        private Context context;
        private OnItemSelectedListener onItemSelectedListener;
        boolean shouldShowSelftext;
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

        public PostViewHolder(Context context, View itemView, OnItemSelectedListener onItemSelectedListener, boolean shouldShowSelftext) {
            super(itemView);

            this.context = context;
            this.onItemSelectedListener = onItemSelectedListener;
            this.shouldShowSelftext = shouldShowSelftext;

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
            viewButton = (Button) itemView.findViewById(R.id.view_button);
            commentsButton = (Button) itemView.findViewById(R.id.comments_button);
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
                            EventBus.getDefault().post(new ViewContentEvent(post.getTitle(), post.getUrl()));
                            break;
                        case R.id.comments_button:
                            EventBus.getDefault().post(new ViewCommentsEvent(post));
                            break;
                    }
                }
            };

            itemView.setOnClickListener(onClickListener);
            viewButton.setOnClickListener(onClickListener);
            commentsButton.setOnClickListener(onClickListener);
            selftextTextView.setOnClickListener(onClickListener);
        }
    }
}
