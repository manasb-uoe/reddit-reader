package com.enthusiast94.reddit_reader.app.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.enthusiast94.reddit_reader.app.R;
import com.enthusiast94.reddit_reader.app.models.Comment;
import com.enthusiast94.reddit_reader.app.models.Post;
import com.enthusiast94.reddit_reader.app.network.Callback;
import com.enthusiast94.reddit_reader.app.network.CommentsManager;

import java.util.List;

/**
 * Created by manas on 07-09-2015.
 */
public class CommentsFragment extends Fragment {

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private RecyclerView commentsRecyclerView;
    private static final String SELECTED_POST_BUNDLE_KEY = "selected_subreddit_key";

    public static CommentsFragment newInstance(Post selectedPost) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SELECTED_POST_BUNDLE_KEY, selectedPost);
        CommentsFragment commentsFragment = new CommentsFragment();
        commentsFragment.setArguments(bundle);

        return commentsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        /**
         * Find views
         */

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_circular);
        commentsRecyclerView = (RecyclerView) view.findViewById(R.id.comments_recyclerview);

        /**
         * Retrieve selected subreddit from arguments
         */

        Post selectedPost = getArguments().getParcelable(SELECTED_POST_BUNDLE_KEY);


        /**
         * Setup toolbar
         */

        toolbar.setTitle(selectedPost.getTitle());
        toolbar.setSubtitle(selectedPost.getSubreddit());
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        /**
         * Load comments and set recyclerview adapter
         */

        CommentsManager.getComments(selectedPost.getSubreddit(), selectedPost.getId(), "best", new Callback<List<Comment>>() {

            @Override
            public void onSuccess(List<Comment> data) {
                progressBar.setVisibility(View.INVISIBLE);
                commentsRecyclerView.setVisibility(View.VISIBLE);

                if (getActivity() != null) {
                    commentsRecyclerView.setAdapter(new CommentsAdapter(data));
                    commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                }
            }

            @Override
            public void onFailure(String message) {
                progressBar.setVisibility(View.INVISIBLE);
                commentsRecyclerView.setVisibility(View.INVISIBLE);

                if (getActivity() != null) {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    private class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Comment> comments;
        private LayoutInflater inflater;
        private int previouslySelectedPosition;
        private int currentlySelectedPosition;
        private int[] childCommentIndicatorColors;

        public CommentsAdapter(List<Comment> comments) {
            this.comments = comments;
            inflater = LayoutInflater.from(getActivity());
            previouslySelectedPosition = -1;
            currentlySelectedPosition = -1;

            // retrieve child comment indicator colors
            Resources res = getResources();
            childCommentIndicatorColors = new int[]{
                    res.getColor(R.color.teal_500),
                    res.getColor(R.color.blue_500),
                    res.getColor(R.color.purple_500),
                    res.getColor(R.color.light_green_500),
                    res.getColor(R.color.deep_orange_500)
            };
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CommentViewHolder(inflater.inflate(R.layout.row_comments_recyclerview, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((CommentViewHolder) holder).bindItem(comments.get(position));
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private View rootLayout;
            private TextView authorTextView;
            private TextView scoreTextView;
            private TextView createdTextView;
            private TextView bodyTextView;
            private View childCommentIndicator;
            private View buttonsContainer;

            public CommentViewHolder(View itemView) {
                super(itemView);

                rootLayout = itemView.findViewById(R.id.root_layout);
                authorTextView = (TextView) itemView.findViewById(R.id.author_textview);
                scoreTextView = (TextView) itemView.findViewById(R.id.score_textview);
                createdTextView = (TextView) itemView.findViewById(R.id.created_textview);
                bodyTextView = (TextView) itemView.findViewById(R.id.body_textview);
                childCommentIndicator = itemView.findViewById(R.id.child_comment_indicator);
                buttonsContainer = itemView.findViewById(R.id.buttons_container);

                // set event listeners
                itemView.setOnClickListener(this);
            }

            public void bindItem(Comment comment) {
                authorTextView.setText(comment.getAuthor());
                scoreTextView.setText(comment.getScore() + " " + getResources().getString(R.string.label_points));
                createdTextView.setText(comment.getCreated());
                bodyTextView.setText(comment.getBody());
                if (comment.getLevel() == 0) {
                    childCommentIndicator.setVisibility(View.GONE);
                } else {
                    childCommentIndicator.setVisibility(View.VISIBLE);
                }

                if (getAdapterPosition() == currentlySelectedPosition) {
                    rootLayout.setBackgroundResource(R.color.post_selected_background);
                    buttonsContainer.setVisibility(View.VISIBLE);
                } else {
                    buttonsContainer.setVisibility(View.GONE);
                    rootLayout.setBackgroundResource(0);
                }

                // set child comment left spacing based on level
                itemView.setPadding((int) (comment.getLevel() *
                        getResources().getDimension(R.dimen.comment_left_spacing)), 0, 0, 0);

                // set child comment indicator color based on level
                childCommentIndicator.setBackgroundColor(childCommentIndicatorColors[comment.getLevel() % 5]);
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
                }
            }
        }
    }
}
