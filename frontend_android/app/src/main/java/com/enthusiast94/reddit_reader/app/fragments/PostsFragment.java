package com.enthusiast94.reddit_reader.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.enthusiast94.reddit_reader.app.R;
import com.enthusiast94.reddit_reader.app.models.Post;
import com.enthusiast94.reddit_reader.app.network.Callback;
import com.enthusiast94.reddit_reader.app.network.PostsManager;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by manas on 03-09-2015.
 */
public class PostsFragment extends Fragment {

    public static final String TAG = Fragment.class.getSimpleName();
    private static final String SUBREDDIT_BUNDLE_KEY = "subreddit_key";
    private static final String SORT_BUNDLE_KEY = "sort_key";
    private RecyclerView postsRecyclerView;
    private ProgressBar progressBar;
    private List<Post> posts;

    public static PostsFragment newInstance(String subreddit, String sort) {
        Bundle bundle = new Bundle();
        bundle.putString(SUBREDDIT_BUNDLE_KEY, subreddit);
        bundle.putString(SORT_BUNDLE_KEY, sort);

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

        /**
         * Find views
         */

        postsRecyclerView = (RecyclerView) view.findViewById(R.id.posts_recyclerview);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_circular);

        /**
         * Retrieve info about posts to load from arguments
         */

        Bundle bundle = getArguments();
        String subreddit = bundle.getString(SUBREDDIT_BUNDLE_KEY);
        String sort = bundle.getString(SORT_BUNDLE_KEY);

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

    private void setPostsAdapter() {
        PostsAdapter postsAdapter = new PostsAdapter();
        postsRecyclerView.setAdapter(postsAdapter);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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
            return new PostViewHolder(getActivity(), itemView);
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

            private Context context;
            private TextView scoreTextView;
            private TextView titleTextView;
            private TextView infoTextView;
            private ImageView thumbnailImageView;
            private View buttonsContainer;

            public PostViewHolder(Context context, View itemView) {
                super(itemView);

                this.context = context;

                scoreTextView = (TextView) itemView.findViewById(R.id.score_textview);
                titleTextView = (TextView) itemView.findViewById(R.id.title_textview);
                infoTextView = (TextView) itemView.findViewById(R.id.info_textview);
                thumbnailImageView = (ImageView) itemView.findViewById(R.id.thumbnail_imageview);
                buttonsContainer = itemView.findViewById(R.id.buttons_container);

                itemView.setOnClickListener(this);
            }

            public void bindItem(Post post) {
                scoreTextView.setText(String.valueOf(post.getScore()));
                titleTextView.setText(post.getTitle());
                infoTextView.setText(post.getCreated() + " \u25CF " + post.getNumComments() + " comments");
                if (post.getThumbnail() != null) {
                    thumbnailImageView.setVisibility(View.VISIBLE);
                    Picasso.with(context).load(post.getThumbnail()).into(thumbnailImageView);
                } else {
                    thumbnailImageView.setVisibility(View.GONE);
                }

                if (getAdapterPosition() == currentlySelectedPosition) {
                    buttonsContainer.setVisibility(View.VISIBLE);
                } else {
                    buttonsContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onClick(View view) {
                currentlySelectedPosition = getAdapterPosition();

                notifyItemChanged(currentlySelectedPosition);
                notifyItemChanged(previouslySelectedPosition);

                previouslySelectedPosition = currentlySelectedPosition;
            }
        }
    }
}
