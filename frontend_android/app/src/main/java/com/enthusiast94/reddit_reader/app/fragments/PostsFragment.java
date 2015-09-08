package com.enthusiast94.reddit_reader.app.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
    private List<Post> posts;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setRetainInstance(true);
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

        if (posts == null) {
            loadPosts(subreddit, sort);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            postsRecyclerView.setVisibility(View.VISIBLE);

            setPostsAdapter();
        }

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

                posts = data;

                // only proceed if fragment is still attached to its parent activity
                // this would prevent null pointer exception when adapter tries to use activity context
                if (getActivity() != null) {
                    setPostsAdapter();
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

    private void setPostsAdapter() {
        PostsAdapter postsAdapter = new PostsAdapter();
        postsRecyclerView.setAdapter(postsAdapter);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // disable change animation
        postsRecyclerView.getItemAnimator().setSupportsChangeAnimations(false);
    }

    private class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

        private LayoutInflater inflater;
        private int previouslySelectedPosition;
        private int currentlySelectedPosition;

        public PostsAdapter() {
            this.inflater = LayoutInflater.from(getActivity());
            previouslySelectedPosition = -1;
            currentlySelectedPosition = -1;
        }

        @Override
        public PostViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = inflater.inflate(R.layout.row_posts_recyclerview, viewGroup, false);
            return new PostViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(PostViewHolder postViewHolder, int i) {
            postViewHolder.bindItem(posts.get(i));
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private View rootLayout;
            private TextView scoreTextView;
            private TextView titleTextView;
            private TextView subredditTextView;
            private TextView numCommentsTextView;
            private TextView createdTextView;
            private ImageView thumbnailImageView;
            private View buttonsContainer;
            private Button viewButton;
            private Button commentsButton;

            public PostViewHolder(View itemView) {
                super(itemView);

                rootLayout = itemView.findViewById(R.id.root_layout);
                scoreTextView = (TextView) itemView.findViewById(R.id.score_textview);
                titleTextView = (TextView) itemView.findViewById(R.id.title_textview);
                subredditTextView = (TextView) itemView.findViewById(R.id.subreddit_textview);
                numCommentsTextView = (TextView) itemView.findViewById(R.id.num_comments_textview);
                createdTextView = (TextView) itemView.findViewById(R.id.created_textview);
                thumbnailImageView = (ImageView) itemView.findViewById(R.id.thumbnail_imageview);
                buttonsContainer = itemView.findViewById(R.id.buttons_container);
                viewButton = (Button) itemView.findViewById(R.id.view_button);
                commentsButton = (Button) itemView.findViewById(R.id.comments_button);

                // set event listeners
                itemView.setOnClickListener(this);
                viewButton.setOnClickListener(this);
                commentsButton.setOnClickListener(this);

            }

            public void bindItem(Post post) {
                scoreTextView.setText(String.valueOf(post.getScore()));
                titleTextView.setText(post.getTitle());
                subredditTextView.setText(post.getSubreddit());
                numCommentsTextView.setText(post.getNumComments() + " " + getResources().getString(R.string.label_comments));
                createdTextView.setText(post.getCreated());

                if (post.getThumbnail() != null) {
                    thumbnailImageView.setVisibility(View.VISIBLE);
                    Glide.with(getActivity()).load(post.getThumbnail()).crossFade().into(thumbnailImageView);
                } else {
                    thumbnailImageView.setVisibility(View.GONE);
                }

                if (getAdapterPosition() == currentlySelectedPosition) {
                    rootLayout.setBackgroundResource(R.color.post_selected_background);
                    buttonsContainer.setVisibility(View.VISIBLE);
                } else {
                    buttonsContainer.setVisibility(View.GONE);
                    rootLayout.setBackgroundResource(0);
                }
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.root_layout:
                        currentlySelectedPosition = getAdapterPosition();

                        notifyItemChanged(currentlySelectedPosition);
                        notifyItemChanged(previouslySelectedPosition);

                        previouslySelectedPosition = currentlySelectedPosition;
                        break;
                    case R.id.view_button:
                        Post currentPost = posts.get(getAdapterPosition());
                        EventBus.getDefault().post(new ViewContentEvent(currentPost.getTitle(), currentPost.getUrl()));
                        break;
                    case R.id.comments_button:
                        EventBus.getDefault().post(new ViewCommentsEvent(posts.get(getAdapterPosition())));
                        break;
                }
            }
        }
    }
}
