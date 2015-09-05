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
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.enthusiast94.reddit_reader.app.R;
import com.enthusiast94.reddit_reader.app.events.SubredditPreferencesUpdatedEvent;
import com.enthusiast94.reddit_reader.app.models.Subreddit;
import com.enthusiast94.reddit_reader.app.network.Callback;
import com.enthusiast94.reddit_reader.app.network.SubredditsManager;
import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manas on 05-09-2015.
 */
public class ManageSubredditsFragment extends Fragment {

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private RecyclerView subredditsRecyclerView;
    private SubredditsAdapter subredditsAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_subreddits, container, false);

        /**
         * Find views
         */

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_circular);
        subredditsRecyclerView = (RecyclerView) view.findViewById(R.id.subreddits_recyclerview);

        /**
         * Setup toolbar
         */

        toolbar.setTitle(R.string.action_manage_subreddits);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        toolbar.inflateMenu(R.menu.menu_manage_subreddits_fragment);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.action_done) {
                    List<Subreddit> selectedSubreddits = new ArrayList<Subreddit>();
                    for (Subreddit subreddit : subredditsAdapter.getSubreddits()) {
                        if (subreddit.isSelected()) {
                            selectedSubreddits.add(subreddit);
                        }
                    }

                    if (selectedSubreddits.size() == 0) {
                        Toast.makeText(getActivity(), R.string.error_atleast_one_selected_subreddit, Toast.LENGTH_LONG)
                                .show();
                    } else {
                        SubredditsManager.saveSubreddits(subredditsAdapter.getSubreddits());
                        Toast.makeText(getActivity(), R.string.success_subreddit_preferences_updated, Toast.LENGTH_SHORT)
                                .show();
                        getActivity().onBackPressed();
                        EventBus.getDefault().post(new SubredditPreferencesUpdatedEvent(selectedSubreddits));
                    }

                    return true;
                }

                return false;
            }
        });

        /**
         * Load subreddits and set recyclerview adapter
         */

        SubredditsManager.getSubreddits(new Callback<List<Subreddit>>() {

            @Override
            public void onSuccess(List<Subreddit> data) {
                progressBar.setVisibility(View.INVISIBLE);
                subredditsRecyclerView.setVisibility(View.VISIBLE);

                subredditsAdapter = new SubredditsAdapter(data);
                subredditsRecyclerView.setAdapter(subredditsAdapter);
                subredditsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            }

            @Override
            public void onFailure(String message) {
                // TODO display error message
            }
        });


        return view;

    }

    private class SubredditsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Subreddit> selectedSubreddits;
        private List<Subreddit> unselectedSubreddits;

        public SubredditsAdapter(List<Subreddit> subreddits) {
            selectedSubreddits = new ArrayList<Subreddit>();
            unselectedSubreddits = new ArrayList<Subreddit>();

            for (Subreddit subreddit : subreddits) {
                if (subreddit.isSelected()) {
                    selectedSubreddits.add(subreddit);
                } else {
                    unselectedSubreddits.add(subreddit);
                }
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            if (viewType == 0) {
                return new HeadingViewHolder(inflater.inflate(R.layout.row_subreddits_recyclerview_heading, parent, false));
            } else if (viewType == 1) {
                return new SubredditViewHolder(inflater.inflate(R.layout.row_subreddits_recyclerview, parent, false));
            } else {
                return null;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position > 0) {
                if (position < selectedSubreddits.size() + 1) {
                    ((SubredditViewHolder) holder).bindItem(selectedSubreddits.get(position-1));
                } else {
                    ((SubredditViewHolder) holder).bindItem(unselectedSubreddits.get(position-1-selectedSubreddits.size()));
                }
            }
        }

        @Override
        public int getItemCount() {
            return 1 + selectedSubreddits.size() + unselectedSubreddits.size();
        }

        public List<Subreddit> getSubreddits() {
            ArrayList<Subreddit> subreddits = new ArrayList<Subreddit>(selectedSubreddits);
            subreddits.addAll(unselectedSubreddits);

            return subreddits;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 0;
            } else {
                return 1;
            }
        }

        public class HeadingViewHolder extends RecyclerView.ViewHolder {

            public HeadingViewHolder(View itemView) {
                super(itemView);
            }
        }

        public class SubredditViewHolder extends RecyclerView.ViewHolder {

            private TextView subredditNameTextView;
            private CheckBox favouriteCheckbox;

            public SubredditViewHolder(View itemView) {
                super(itemView);

                subredditNameTextView = (TextView) itemView.findViewById(R.id.subreddit_name_textview);
                favouriteCheckbox = (CheckBox) itemView.findViewById(R.id.favourite_checkbox);

                itemView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Subreddit currentSubreddit;

                        if (getAdapterPosition() < selectedSubreddits.size() + 1) {
                            currentSubreddit = selectedSubreddits.get(getAdapterPosition()-1);
                        } else {
                            currentSubreddit = unselectedSubreddits.get(getAdapterPosition()-1-selectedSubreddits.size());
                        }

                        currentSubreddit.setSelected(!currentSubreddit.isSelected());
                        notifyItemChanged(getAdapterPosition());
                    }
                });
            }

            public void bindItem(Subreddit subreddit) {
                subredditNameTextView.setText(subreddit.getName());
                favouriteCheckbox.setChecked(subreddit.isSelected());
            }
        }
    }
}
