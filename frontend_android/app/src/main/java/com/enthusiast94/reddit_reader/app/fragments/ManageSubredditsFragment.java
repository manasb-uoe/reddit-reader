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
                    List<Subreddit> subreddits = subredditsAdapter.getSubreddits();

                    int selected = 0;
                    for (Subreddit subreddit : subreddits) {
                        if (subreddit.isSelected()) selected++;
                    }

                    if (selected == 0) {
                        Toast.makeText(getActivity(), R.string.error_atleast_one_selected_subreddit, Toast.LENGTH_LONG)
                                .show();
                    } else {
                        SubredditsManager.saveSubreddits(subreddits);
                        Toast.makeText(getActivity(), R.string.success_subreddit_preferences_updated, Toast.LENGTH_SHORT)
                                .show();
                        getActivity().onBackPressed();
                        EventBus.getDefault().post(new SubredditPreferencesUpdatedEvent(subreddits));
                    }

                    return true;
                } else if (id == R.id.action_select_all) {
                    subredditsAdapter.selectAll();
                } else if (id == R.id.action_deselect_all) {
                    subredditsAdapter.deselectAll();
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
                return new DividerViewHolder(inflater.inflate(R.layout.row_subreddits_recyclerview_divider, parent, false));
            } else {
                return new SubredditViewHolder(inflater.inflate(R.layout.row_subreddits_recyclerview, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position > 0 && position != 1 + selectedSubreddits.size()) {
                if (position < selectedSubreddits.size() + 1) {
                    ((SubredditViewHolder) holder).bindItem(selectedSubreddits.get(position-1));
                } else {
                    ((SubredditViewHolder) holder).bindItem(unselectedSubreddits.get(position-1-selectedSubreddits.size()-1));
                }
            }
        }

        @Override
        public int getItemCount() {
            return 1 + selectedSubreddits.size() + 1 + unselectedSubreddits.size();
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
            } else if (position == 1 + selectedSubreddits.size()) {
                return 1;
            } else {
                return 2;
            }
        }

        public void selectAll() {
            for (Subreddit subreddit : selectedSubreddits) {
                subreddit.setSelected(true);
            }

            for (Subreddit subreddit : unselectedSubreddits) {
                subreddit.setSelected(true);
            }

            notifyDataSetChanged();
        }

        public void deselectAll() {
            for (Subreddit subreddit : selectedSubreddits) {
                subreddit.setSelected(false);
            }

            for (Subreddit subreddit : unselectedSubreddits) {
                subreddit.setSelected(false);
            }

            notifyDataSetChanged();
        }

        public class HeadingViewHolder extends RecyclerView.ViewHolder {

            public HeadingViewHolder(View itemView) {
                super(itemView);
            }
        }

        public class DividerViewHolder extends RecyclerView.ViewHolder {

            public DividerViewHolder(View itemView) {
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
                            currentSubreddit = unselectedSubreddits.get(getAdapterPosition()-1-selectedSubreddits.size()-1);
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
