package com.enthusiast94.reddit_reader.app.fragments;

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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.enthusiast94.reddit_reader.app.R;
import com.enthusiast94.reddit_reader.app.events.ViewContentEvent;
import com.enthusiast94.reddit_reader.app.models.Comment;
import com.enthusiast94.reddit_reader.app.models.Post;
import com.enthusiast94.reddit_reader.app.network.AuthManager;
import com.enthusiast94.reddit_reader.app.network.Callback;
import com.enthusiast94.reddit_reader.app.network.CommentsManager;
import com.enthusiast94.reddit_reader.app.network.RedditManager;
import com.enthusiast94.reddit_reader.app.utils.Helpers;
import com.enthusiast94.reddit_reader.app.utils.OnItemSelectedListener;
import com.enthusiast94.reddit_reader.app.utils.TextViewLinkHandler;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manas on 07-09-2015.
 */
public class CommentsFragment extends Fragment {

    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private LinearLayoutManager linearLayoutManager;
    private static final String SELECTED_POST_BUNDLE_KEY = "selected_subreddit_key";
    private static final String SORT_BUNDLE_KEY = "sort_bundle_key";
    private static final String COMMENTS_BUNDLE_KEY = "comments_key";
    private Post selectedPost;
    private String sort;
    private ArrayList<Comment> comments;

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
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        commentsRecyclerView = (RecyclerView) view.findViewById(R.id.comments_recyclerview);

        /**
         * Retrieve info required to load comments
         */

        selectedPost = getArguments().getParcelable(SELECTED_POST_BUNDLE_KEY);

        if (savedInstanceState == null) {
            sort = getResources().getString(R.string.action_sort_best);
            comments = new ArrayList<Comment>();
        } else {
            sort = savedInstanceState.getString(SORT_BUNDLE_KEY);
            comments = savedInstanceState.getParcelableArrayList(COMMENTS_BUNDLE_KEY);
        }

        /**
         * Configure recycler view
         */

        linearLayoutManager = new LinearLayoutManager(getActivity());
        commentsRecyclerView.setLayoutManager(linearLayoutManager);
        commentsRecyclerView.getItemAnimator().setSupportsChangeAnimations(false);
        commentsAdapter = new CommentsAdapter();
        commentsRecyclerView.setAdapter(commentsAdapter);

        /**
         * Configure swipe refresh layout
         */

        swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                loadComments();
            }
        });

        /**
         * Setup toolbar
         */

        updateToolbarTitles(selectedPost.getTitle(), sort);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        toolbar.inflateMenu(R.menu.menu_comments_fragment);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.action_sort_best || id == R.id.action_sort_top || id == R.id.action_sort_new ||
                        id == R.id.action_sort_controversial || id == R.id.action_sort_old) {
                    sort = item.getTitle().toString();
                    updateToolbarTitles(selectedPost.getTitle(), sort);
                    loadComments();

                    return true;
                }

                return false;
            }
        });

        /**
         * Load comments
         */

        if (savedInstanceState == null) {
            loadComments();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SORT_BUNDLE_KEY, sort);
        outState.putParcelableArrayList(COMMENTS_BUNDLE_KEY, comments);
    }

    private void updateToolbarTitles(String title, String subtitle) {
        toolbar.setTitle(title);
        toolbar.setSubtitle(subtitle);
    }

    private void loadComments() {
        setRefreshIndicatorVisiblity(true);
        // only hide recycler view if post item is the only item (i.e. when comments are being loadded for the first time)
        if (commentsAdapter.getItemCount() == 1) {
            commentsRecyclerView.setVisibility(View.INVISIBLE);
        }

        CommentsManager.getComments(selectedPost.getSubreddit(), selectedPost.getId(), sort, new Callback<List<Comment>>() {

            @Override
            public void onSuccess(List<Comment> data) {
                if (getActivity() != null) {
                    setRefreshIndicatorVisiblity(false);
                    commentsRecyclerView.setVisibility(View.VISIBLE);

                    if (data.size() > 0) {
                        commentsAdapter.setComments(data);
                    } else {
                        Toast.makeText(getActivity(), R.string.label_no_comments, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(String message) {
                if (getActivity() != null) {
                    setRefreshIndicatorVisiblity(false);
                    commentsRecyclerView.setVisibility(View.VISIBLE);

                    if (message != null)
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setRefreshIndicatorVisiblity(final boolean visiblity) {
        swipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(visiblity);
            }
        });
    }

    private class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemSelectedListener {

        private int previouslySelectedPosition;
        private int currentlySelectedPosition;
        private int[] childCommentIndicatorColors;
        private int upvoteColor;
        private int downvoteColor;
        private int primaryTextColor;
        private int secondaryTextColor;
        private int opHighlightColor;
        private int accentColor;
        private int lastParentCommentPosition;

        public CommentsAdapter() {
            previouslySelectedPosition = -1;
            currentlySelectedPosition = -1;

            Resources res = getActivity().getResources();

            childCommentIndicatorColors = new int[]{
                    res.getColor(R.color.teal_500),
                    res.getColor(R.color.blue_500),
                    res.getColor(R.color.purple_500),
                    res.getColor(R.color.light_green_500),
                    res.getColor(R.color.deep_orange_500)
            };

            upvoteColor = res.getColor(R.color.reddit_upvote);
            downvoteColor = res.getColor(R.color.reddit_downvote);
            primaryTextColor = res.getColor(R.color.primary_text_default_material_dark);
            secondaryTextColor = res.getColor(R.color.secondary_text_default_material_dark);
            accentColor = res.getColor(R.color.accent);
            opHighlightColor = res.getColor(R.color.blue_700);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            if (viewType == 0) {
                return new PostsFragment.PostViewHolder(getActivity(),
                        inflater.inflate(R.layout.row_posts_recyclerview, parent, false), this, true);
            } else {
                return new CommentViewHolder(inflater.inflate(R.layout.row_comments_recyclerview, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == 0) {
                ((PostsFragment.PostViewHolder) holder).bindItem(selectedPost, currentlySelectedPosition);
            } else {
                ((CommentViewHolder) holder).bindItem(comments.get(position-1));
            }
        }

        @Override
        public int getItemCount() {
            return 1 + comments.size();
        }

        @Override
        public void onItemSelected(int position) {
            currentlySelectedPosition = position;

            notifyItemChanged(currentlySelectedPosition);
            notifyItemChanged(previouslySelectedPosition);

            previouslySelectedPosition = currentlySelectedPosition;
        }

        public void setComments(List<Comment> comments) {
            CommentsFragment.this.comments = new ArrayList<Comment>(comments);
            notifyDataSetChanged();

            // update parent comment position, which will be later used to enable/disable nextButton
            for (int i=comments.size()-1; i>=0; i--) {
                if (comments.get(i).getLevel() == 0) {
                    lastParentCommentPosition = i;
                    break;
                }
            }
        }

        private class CommentViewHolder extends RecyclerView.ViewHolder {

            private View rootLayout;
            private TextView authorTextView;
            private TextView scoreTextView;
            private TextView createdTextView;
            private TextView bodyTextView;
            private View childCommentIndicator;
            private View buttonsContainerTop;
            private Button nextButton;
            private Button previousButton;
            private View buttonsContainerBottom;
            private Button upvoteButton;
            private Button downvoteButton;

            public CommentViewHolder(View itemView) {
                super(itemView);

                rootLayout = itemView.findViewById(R.id.root_layout);
                authorTextView = (TextView) itemView.findViewById(R.id.author_textview);
                scoreTextView = (TextView) itemView.findViewById(R.id.score_textview);
                createdTextView = (TextView) itemView.findViewById(R.id.created_textview);
                bodyTextView = (TextView) itemView.findViewById(R.id.body_textview);
                childCommentIndicator = itemView.findViewById(R.id.child_comment_indicator);
                buttonsContainerTop = itemView.findViewById(R.id.buttons_container_top);
                nextButton = (Button) buttonsContainerTop.findViewById(R.id.next_parent_comment_button);
                previousButton = (Button) buttonsContainerTop.findViewById(R.id.previous_parent_comment_button);
                buttonsContainerBottom = itemView.findViewById(R.id.buttons_container_bottom);
                upvoteButton = (Button) itemView.findViewById(R.id.upvote_button);
                downvoteButton = (Button) itemView.findViewById(R.id.downvote_button);
            }

            public void bindItem(final Comment comment) {
                authorTextView.setText(comment.getAuthor());
                scoreTextView.setText(comment.getScore() + " " + getActivity().getResources().getString(R.string.label_points));
                createdTextView.setText(comment.getCreated());
                // the inner fromHtml unescapes html entities, while the outer fromHtml returns a formatted Spannable
                bodyTextView.setText(
                        Helpers.trimTrailingWhitespace(Html.fromHtml(Html.fromHtml(comment.getBody()).toString()))
                );
                bodyTextView.setMovementMethod(new TextViewLinkHandler() {
                    @Override
                    public void onLinkClick(String url) {
                        EventBus.getDefault().post(new ViewContentEvent(null, url));
                    }
                });
                if (comment.getLevel() == 0) {
                    childCommentIndicator.setVisibility(View.GONE);
                } else {
                    childCommentIndicator.setVisibility(View.VISIBLE);
                }

                // highlight comment author's name if they are also the post's author
                if (comment.getAuthor().equals(selectedPost.getAuthor())) {
                    authorTextView.setTextColor(primaryTextColor);
                    authorTextView.setBackgroundColor(opHighlightColor);
                } else {
                    authorTextView.setTextColor(accentColor);
                    authorTextView.setBackgroundColor(0);
                }

                if (getAdapterPosition() == currentlySelectedPosition) {
                    rootLayout.setBackgroundResource(R.color.selected_item_background);
                    buttonsContainerTop.setVisibility(View.VISIBLE);
                    buttonsContainerBottom.setVisibility(View.VISIBLE);
                } else {
                    rootLayout.setBackgroundResource(0);
                    buttonsContainerTop.setVisibility(View.GONE);
                    buttonsContainerBottom.setVisibility(View.GONE);
                }

                if (getAdapterPosition() <= 1) {
                    previousButton.setEnabled(false);
                } else {
                    previousButton.setEnabled(true);
                }

                if (getAdapterPosition() > lastParentCommentPosition) {
                    nextButton.setEnabled(false);
                } else {
                    nextButton.setEnabled(true);
                }

                // set child comment left spacing based on level
                itemView.setPadding((int) (comment.getLevel() *
                        getActivity().getResources().getDimension(R.dimen.comment_left_spacing)), 0, 0, 0);

                // set child comment indicator color based on level
                childCommentIndicator.setBackgroundColor(childCommentIndicatorColors[comment.getLevel() % 5]);

                if (AuthManager.isUserAuthenticated()) {
                    setUpvoteDownvoteColors(comment.getLikes());
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
                            case R.id.body_textview:
                                onItemSelected(getAdapterPosition());
                                break;
                            case R.id.next_parent_comment_button:
                                for (int i=getAdapterPosition()+1; i<=comments.size(); i++) {
                                    if (comments.get(i-1).getLevel() == 0) {
                                        onItemSelected(i);
                                        linearLayoutManager.scrollToPositionWithOffset(i, itemView.getTop());
                                        break;
                                    }
                                }
                                break;
                            case R.id.previous_parent_comment_button:
                                for (int i=getAdapterPosition()-1; i >0; i--) {
                                    if (comments.get(i-1).getLevel() == 0) {
                                        onItemSelected(i);
                                        linearLayoutManager.scrollToPositionWithOffset(i, itemView.getTop());
                                        break;
                                    }
                                }
                                break;
                            case R.id.upvote_button:
                                if (comment.getLikes() == -1 || comment.getLikes() == 0) {
                                    comment.setLikes(true);
                                } else {
                                    comment.setLikes(null);
                                }
                                RedditManager.vote(comment.getFullName(), comment.getLikes(), null);
                                setUpvoteDownvoteColors(comment.getLikes());
                                break;
                            case R.id.downvote_button:
                                if (comment.getLikes() == 1 || comment.getLikes() == 0) {
                                    comment.setLikes(false);
                                } else {
                                    comment.setLikes(null);
                                }
                                RedditManager.vote(comment.getFullName(), comment.getLikes(), null);
                                setUpvoteDownvoteColors(comment.getLikes());
                                break;
                        }
                    }
                };

                itemView.setOnClickListener(onClickListener);
                nextButton.setOnClickListener(onClickListener);
                previousButton.setOnClickListener(onClickListener);
                bodyTextView.setOnClickListener(onClickListener);
                upvoteButton.setOnClickListener(onClickListener);
                downvoteButton.setOnClickListener(onClickListener);
            }

            /**
             * Sets colors for various ui elements within viewholder according to comment's current vote status
             */

            private void setUpvoteDownvoteColors(int likes) {
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
                    scoreTextView.setTextColor(secondaryTextColor);
                    downvoteButton.setTextColor(primaryTextColor);
                }
            }
        }
    }
}
