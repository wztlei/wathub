package io.github.wztlei.wathub.ui.modules.resources;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.TooltipCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.model.RedditPost;
import io.github.wztlei.wathub.model.RedditPostList;
import io.github.wztlei.wathub.ui.modules.base.BaseModuleFragment;
import io.github.wztlei.wathub.utils.IntentUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RedditFragment extends BaseModuleFragment
        implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.reddit_posts_sort_spinner)
    Spinner mRedditSortSpinner;
    @BindView(R.id.reddit_time_filter_spinner)
    Spinner mRedditTimeFilterSpinner;
    @BindView(R.id.reddit_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.reddit_posts_list)
    RecyclerView mRedditPostList;
    @BindView(R.id.reddit_no_results)
    TextView mNoResultsText;
    @BindView(R.id.reddit_loading_layout)
    ViewGroup mLoadingLayout;

    private Context mContext;
    private MenuItem mRefreshMenuItem;
    private RedditPostList mRedditPosts;

    private static final String[] SORT_OPTION_SUFFIXES =
            {"hot/.json", "top/.json", "new/.json", "controversial/.json", "rising/.json"};
    private static final String[] TIME_FILTER_OPTION_SUFFIXES =
            {"?t=hour", "?t=day", "?t=week", "?t=month", "?t=year", "?t=all"};
    private static final String UWATERLOO_SUBREDDIT_URL = "https://www.reddit.com/r/uwaterloo/";
    private static final int MAX_TITLE_LENGTH = 150;
    private static final int MAX_SELFTEXT_LENGTH = 150;
    private static final int TOP_POSTS_OPTION_INDEX = 1;
    private static final int CONTROVERSIAL_POSTS_OPTION_INDEX = 3;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public final View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {

        // Set up the view
        View root = inflater.inflate(R.layout.fragment_module, container, false);
        ViewGroup parent = root.findViewById(R.id.container_content_view);
        View contentView = inflater.inflate(R.layout.fragment_reddit, parent, false);
        parent.addView(contentView);
        setHasOptionsMenu(true);

        // Initialize instance variables
        ButterKnife.bind(this, contentView);
        mRedditPostList.setLayoutManager(new LinearLayoutManager(mContext));
        mSwipeRefreshLayout.setOnRefreshListener(this);
        setSpinnerSelectionListeners();
        refreshRedditList(UWATERLOO_SUBREDDIT_URL + SORT_OPTION_SUFFIXES[0]);
        showLoadingScreen(mSwipeRefreshLayout, mLoadingLayout,
                mRefreshMenuItem, true);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        // Inflate the menu
        inflater.inflate(R.menu.menu_open_browser, menu);
        mRefreshMenuItem = menu.findItem(R.id.menu_refresh);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            // TODO WL: Refresh
            case R.id.menu_refresh:
                onRefresh();
                return true;
            case R.id.menu_browser:
                IntentUtils.openBrowser(mContext, UWATERLOO_SUBREDDIT_URL);
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onRefresh() {
        String url = getQueryUrl();

        if (url != null) {
            // Refresh the screen and retrieve the latest schedules from GitHub
            showLoadingScreen(mSwipeRefreshLayout, mLoadingLayout,
                    mRefreshMenuItem, false);
            refreshRedditList(url);
        }
    }

    @Override
    public String getToolbarTitle() {
        return getString(R.string.title_uwaterloo_subreddit);
    }

    private void refreshRedditList(String url) {
        try {
            // Create a request using the OkHttpClient library
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) {
                    try {
                        Activity activity = getActivity();

                        // Display a toast for a no network error
                        if (activity != null) {
                            // noinspection ConstantConditions
                            String responseString = response.body().string();
                            JSONObject responseJson = new JSONObject(responseString);
                            JSONArray dataChildren = responseJson.getJSONObject("data")
                                    .getJSONArray("children");
                            activity.runOnUiThread(() -> {
                                displayQueryResults(new RedditPostList(dataChildren));
                                RedditFragment.this.hideLoadingScreen(
                                        mSwipeRefreshLayout, mLoadingLayout, mRefreshMenuItem);
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NonNull final Call call, @NonNull IOException e) {
                    Activity activity = getActivity();

                    // Display a toast for a no network error
                    if (activity != null) {
                        CharSequence errorText = activity.getText(R.string.error_no_network);
                        activity.runOnUiThread(() -> {
                            Toast.makeText(activity, errorText, Toast.LENGTH_SHORT).show();
                            RedditFragment.this.hideLoadingScreen(
                                    mSwipeRefreshLayout, mLoadingLayout, mRefreshMenuItem);
                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets OnItemSelectedListeners on the two spinners to display the new query
     * results whenever a new dropdown option is selected.
     */
    private void setSpinnerSelectionListeners() {
        mRedditSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onRefresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mRedditTimeFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onRefresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    String getQueryUrl() {
        String url = null;
        int sortOptionIndex = mRedditSortSpinner.getSelectedItemPosition();

        if (sortOptionIndex >= 0) {
             url = UWATERLOO_SUBREDDIT_URL + SORT_OPTION_SUFFIXES[sortOptionIndex];

            if (sortOptionIndex == TOP_POSTS_OPTION_INDEX
                    || sortOptionIndex == CONTROVERSIAL_POSTS_OPTION_INDEX) {
                mRedditTimeFilterSpinner.setVisibility(View.VISIBLE);
                int timeFilterIndex = mRedditTimeFilterSpinner.getSelectedItemPosition();

                if (timeFilterIndex >= 0) {
                    url += TIME_FILTER_OPTION_SUFFIXES[timeFilterIndex];
                }
            } else {
                mRedditTimeFilterSpinner.setVisibility(View.GONE);
            }
        }

        return url;
    }

    private void displayQueryResults(RedditPostList redditPosts) {
        mRedditPosts = redditPosts;
        mRedditPostList.setAdapter(new RedditPostAdapter());
    }

    /**
     * Sets the text of a TextView or hides it if the text is null or an empty string.
     *
     * @param textView  the TextView to modify
     * @param text      the intended text of the TextView
     */
    private void setTextOfTextView(TextView textView, String text) {
        if (text == null || text.length() == 0) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(text);
        }
    }

    /**
     * Returns the formatted string representing the score or number of comments.
     *
     * @param   num the number to format
     * @return      the formatted string
     */
    private String formatRedditPostNumber(int num) {
        if (num < 0) {
            throw new IllegalArgumentException(num + " < 0");
        } else if (num < 1000) {
            return Integer.toString(num);
        } else if (num < 100000) {
            int hundreds = num / 100;
            int thousands = hundreds / 10;
            int thousandsDecimal = hundreds % 10;
            return String.format(Locale.CANADA, "%d.%dk", thousands,  thousandsDecimal);
        } else if (num < 1000000) {
            return String.format(Locale.CANADA, "%dk", num / 1000);
        } else {
            throw new IllegalArgumentException(num + " >= 1000000");
        }
    }

    /**
     * A custom RecyclerView Adapter for the list of Reddit posts.
     */
    class RedditPostAdapter extends RecyclerView.Adapter<RedditPostViewHolder> {
        @NonNull
        @Override
        public RedditPostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            // Use layout_schedule_item.xml as the layout for each individual recycler view item
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.list_item_reddit_post, viewGroup, false);
            return new RedditPostViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RedditPostViewHolder viewHolder, int i) {
            // Display the RoomTimeInterval at index i in the recycler view
            RedditPost redditPost = mRedditPosts.get(i);
            String author = redditPost.getAuthor();
            String creationTime = redditPost.getCreationTime();
            String domain = redditPost.getDomain();
            String title = redditPost.getTitle();
            String linkFlair = redditPost.getLinkFlair();
            String selftext = redditPost.getSelftext().replace("\n"," ")
                    .replace("\r", " ");
            String score = formatRedditPostNumber(redditPost.getScore());
            String numComments = formatRedditPostNumber(redditPost.getNumComments());
            String header;

            if (title.length() > MAX_TITLE_LENGTH) {
                title = title.substring(0, MAX_TITLE_LENGTH - 4) + " ...";
            }

            if (selftext.length() > MAX_SELFTEXT_LENGTH) {
                selftext = selftext.substring(0, MAX_SELFTEXT_LENGTH - 4) + " ...";
            }

            if (redditPost.getDomain().equals(RedditPost.DEFAULT_UWATERLOO_DOMAIN)) {
                header = String.format("u/%s • %s", author, creationTime);
            } else {
                header = String.format("u/%s • %s • %s", author, creationTime, domain);
            }

            // Update the text of the item in the recycler view
            setTextOfTextView(viewHolder.headerText, header);
            setTextOfTextView(viewHolder.titleText, title);
            setTextOfTextView(viewHolder.linkFlairText, linkFlair);
            setTextOfTextView(viewHolder.selftextText, selftext);
            setTextOfTextView(viewHolder.scoreText, score);
            setTextOfTextView(viewHolder.numCommentsText, numComments);

            if (redditPost.isImage()) {
                viewHolder.contentIcon.setVisibility(View.VISIBLE);
                viewHolder.contentIcon.setImageResource(R.drawable.ic_image);
            } else if (redditPost.isVideo()) {
                viewHolder.contentIcon.setVisibility(View.VISIBLE);
                viewHolder.contentIcon.setImageResource(R.drawable.ic_video);
            } else {
                viewHolder.contentIcon.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return mRedditPosts.size();
        }
    }

    /**
     * A custom RecyclerView ViewHolder for an item in the list of Reddit posts.
     */
    class RedditPostViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.reddit_post_card)
        CardView redditPostCard;
        @BindView(R.id.reddit_post_header)
        TextView headerText;
        @BindView(R.id.reddit_post_title)
        TextView titleText;
        @BindView(R.id.reddit_post_link_flair)
        TextView linkFlairText;
        @BindView(R.id.reddit_post_selftext)
        TextView selftextText;
        @BindView(R.id.reddit_post_content_icon)
        ImageButton contentIcon;
        @BindView(R.id.reddit_post_score)
        TextView scoreText;
        @BindView(R.id.reddit_post_num_comments)
        TextView numCommentsText;

        RedditPostViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            redditPostCard.setOnClickListener(view -> {
                int position = getAdapterPosition();

                if (position >= 0) {
                    IntentUtils.openBrowser(mContext, mRedditPosts.get(position).getPermalink());
                }
            });

            contentIcon.setOnClickListener(view -> {
                int position = getAdapterPosition();

                if (position >= 0) {
                    IntentUtils.openBrowser(mContext, mRedditPosts.get(position).getUrl());
                }
            });
        }
    }
}
