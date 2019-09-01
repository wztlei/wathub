package io.github.wztlei.wathub.ui.modules.resources;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    Spinner mSortSpinner;
    @BindView(R.id.reddit_time_filter_spinner)
    Spinner mTimeFilterSpinner;
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
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private RedditPostList mRedditPosts;
    private int mPrevSortOptionPos = 0;
    private int mPrevTimeFilterPos = 0;
    private boolean mIgnoreOnSortOptionSelectedEvent = false;
    private boolean mIgnoreOnTimeFilterSelectedEvent = false;

    private static final String[] SORT_OPTION_SUFFIXES =
            {"hot/.json", "top/.json", "new/.json", "controversial/.json", "rising/.json"};
    private static final String[] TIME_FILTER_OPTION_SUFFIXES =
            {"?t=hour", "?t=day", "?t=week", "?t=month", "?t=year", "?t=all"};
    private static final String UWATERLOO_SUBREDDIT_URL = "https://www.reddit.com/r/uwaterloo/";
    private static final int MAX_TITLE_LENGTH = 200;
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
        mRedditPosts = new RedditPostList(new JSONArray());

        // Attach an adapter onto the Reddit post list
        mRedditPostList.setLayoutManager(new LinearLayoutManager(mContext));
        mRedditPostList.setAdapter(new RedditPostAdapter());

        // Set up listeners
        mSwipeRefreshLayout.setOnRefreshListener(this);
        setSpinnerSelectionListeners();

        // Prepare the initial view
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
        String url = null;
        int sortOptionIndex = mSortSpinner.getSelectedItemPosition();

        // Build the url for the request to get the Reddit posts
        if (sortOptionIndex >= 0) {
            url = UWATERLOO_SUBREDDIT_URL + SORT_OPTION_SUFFIXES[sortOptionIndex];

            // Add a time filter url suffix for querying top and controversial posts
            if (sortOptionIndex == TOP_POSTS_OPTION_INDEX
                    || sortOptionIndex == CONTROVERSIAL_POSTS_OPTION_INDEX) {
                int timeFilterIndex = mTimeFilterSpinner.getSelectedItemPosition();

                if (timeFilterIndex >= 0) {
                    url += TIME_FILTER_OPTION_SUFFIXES[timeFilterIndex];
                } else {
                    url += TIME_FILTER_OPTION_SUFFIXES[0];
                }
            }
        }

        // Refresh the screen and retrieve the latest schedules from GitHub
        if (url != null) {
            showLoadingScreen(mSwipeRefreshLayout, mLoadingLayout,
                    mRefreshMenuItem, false);
            refreshRedditList(url);
        }
    }

    @Override
    public String getToolbarTitle() {
        return getString(R.string.title_uwaterloo_subreddit);
    }

    /**
     * Refreshes the list of Reddit posts by sending a request for updated data.
     *
     * @param url the url to retrieve the new list of Reddit posts
     */
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

                        if (activity == null) {
                            return;
                        }

                        // noinspection ConstantConditions
                        String responseString = response.body().string();
                        JSONObject responseJson = new JSONObject(responseString);
                        JSONArray dataChildren = responseJson.getJSONObject("data")
                                .getJSONArray("children");

                        // Update the display on the UI thread
                        activity.runOnUiThread(() -> {
                            // Update the Reddit post list and hide the loading screen
                            mRedditPosts = new RedditPostList(dataChildren);
                            mRedditPostList.setAdapter(new RedditPostAdapter());
                            RedditFragment.this.hideLoadingScreen(
                                    mSwipeRefreshLayout, mLoadingLayout, mRefreshMenuItem);

                            // Listen for future spinner selections
                            mIgnoreOnSortOptionSelectedEvent = false;
                            mIgnoreOnTimeFilterSelectedEvent = false;

                            // Update the spinner selections to record the last successful request
                            mPrevSortOptionPos = mSortSpinner.getSelectedItemPosition();
                            mPrevTimeFilterPos = mTimeFilterSpinner.getSelectedItemPosition();

                            // Update the time filter spinner's visibility based on the sort option
                            if (mPrevSortOptionPos == TOP_POSTS_OPTION_INDEX
                                    || mPrevSortOptionPos == CONTROVERSIAL_POSTS_OPTION_INDEX) {
                                mTimeFilterSpinner.setVisibility(View.VISIBLE);
                            } else {
                                mTimeFilterSpinner.setVisibility(View.GONE);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NonNull final Call call, @NonNull IOException e) {
                    Activity activity = getActivity();

                    if (activity == null) {
                        return;
                    }

                    // Update the display on the UI thread
                    activity.runOnUiThread(() -> {
                        // Display a toast for a no network error and hide the loading screen
                        CharSequence errorText = activity.getText(R.string.error_no_network);
                        Toast.makeText(activity, errorText, Toast.LENGTH_SHORT).show();
                        RedditFragment.this.hideLoadingScreen(
                                mSwipeRefreshLayout, mLoadingLayout, mRefreshMenuItem);

                        // Update the selection of the sort option spinner if it needs changing
                        if (mSortSpinner.getSelectedItemPosition() != mPrevSortOptionPos) {
                            mIgnoreOnSortOptionSelectedEvent = true;
                            mSortSpinner.setSelection(mPrevSortOptionPos);
                        }

                        // Update the selection of the time filter spinner if it needs changing
                        if (mTimeFilterSpinner.getSelectedItemPosition() != mPrevTimeFilterPos) {
                            mIgnoreOnTimeFilterSelectedEvent = true;
                            mTimeFilterSpinner.setSelection(mPrevTimeFilterPos);
                        }

                        // Update the time filter spinner's visibility based on the sort option
                        if (mPrevSortOptionPos == TOP_POSTS_OPTION_INDEX
                                || mPrevSortOptionPos == CONTROVERSIAL_POSTS_OPTION_INDEX) {
                            mTimeFilterSpinner.setVisibility(View.VISIBLE);
                        } else {
                            mTimeFilterSpinner.setVisibility(View.GONE);
                        }
                    });
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
        mSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mIgnoreOnSortOptionSelectedEvent) {
                    mIgnoreOnSortOptionSelectedEvent = false;
                } else {
                    onRefresh();

                    if (mTimeFilterSpinner.getSelectedItemPosition() != 0) {
                        mIgnoreOnTimeFilterSelectedEvent = true;
                        mTimeFilterSpinner.setSelection(0);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mTimeFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mIgnoreOnTimeFilterSelectedEvent) {
                    mIgnoreOnTimeFilterSelectedEvent = false;
                } else {
                    onRefresh();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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
            // Negative numbers are prohibited
            throw new IllegalArgumentException(num + " < 0");
        } else if (num < 1000) {
            // For numbers less than 1000, just display the number itself
            return Integer.toString(num);
        } else if (num < 100000) {
            // For numbers between 1000 and 99 999, display as 9.0k or 15.6k
            int hundreds = num / 100;
            int thousands = hundreds / 10;
            int thousandsDecimal = hundreds % 10;
            return String.format(Locale.CANADA, "%d.%dk", thousands,  thousandsDecimal);
        } else if (num < 1000000) {
            // For numbers between 100 000 and 999 999, display as 153k or 900k
            return String.format(Locale.CANADA, "%dk", num / 1000);
        } else {
            // No Reddit post has over 1 million upvotes or comments
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
            // Display the Reddit post at index i in the recycler view
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

            // Truncate the title of the Reddit post if needed
            if (title.length() > MAX_TITLE_LENGTH) {
                title = title.substring(0, MAX_TITLE_LENGTH - 4) + " ...";
            }

            // Truncate the selftext of the Reddit post if needed
            if (selftext.length() > MAX_SELFTEXT_LENGTH) {
                selftext = selftext.substring(0, MAX_SELFTEXT_LENGTH - 4) + " ...";
            }

            // Build the header of the Reddit post
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

            // Update the icon button for a Reddit post linking to an image or video
            if (redditPost.isImage()) {
                viewHolder.iconLayout.setVisibility(View.VISIBLE);
                viewHolder.iconImage.setImageResource(R.drawable.ic_image);
                viewHolder.iconText.setText(R.string.reddit_view_content);
            } else if (redditPost.isVideo()) {
                viewHolder.iconLayout.setVisibility(View.VISIBLE);
                viewHolder.iconImage.setImageResource(R.drawable.ic_video);
                viewHolder.iconText.setText(R.string.reddit_watch_content);
            } else if (redditPost.isLink()) {
                viewHolder.iconLayout.setVisibility(View.VISIBLE);
                viewHolder.iconImage.setImageResource(R.drawable.ic_link);
                viewHolder.iconText.setText(R.string.reddit_open_content);
            } else {
                viewHolder.iconLayout.setVisibility(View.GONE);
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
        @BindView(R.id.reddit_post_icon_layout)
        FrameLayout iconLayout;
        @BindView(R.id.reddit_post_icon_button)
        ImageButton iconButton;
        @BindView(R.id.reddit_post_icon_image)
        ImageView iconImage;
        @BindView(R.id.reddit_post_icon_text)
        TextView iconText;
        @BindView(R.id.reddit_post_score)
        TextView scoreText;
        @BindView(R.id.reddit_post_num_comments)
        TextView numCommentsText;

        RedditPostViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            // Clicking the card will open the Reddit post and its comments in a browser
            redditPostCard.setOnClickListener(view -> {
                int position = getAdapterPosition();

                if (position >= 0) {
                    IntentUtils.openBrowser(mContext, mRedditPosts.get(position).getPermalink());
                }
            });

            // Clicking the icon for an image or video will open that content in a browser
            iconButton.setOnClickListener(view -> {
                int position = getAdapterPosition();

                if (position >= 0) {
                    IntentUtils.openBrowser(mContext, mRedditPosts.get(position).getUrl());
                }
            });
        }
    }
}
