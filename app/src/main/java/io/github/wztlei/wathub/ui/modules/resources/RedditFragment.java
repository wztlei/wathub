package io.github.wztlei.wathub.ui.modules.resources;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

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

public class RedditFragment extends BaseModuleFragment {


    @BindView(R.id.reddit_posts_sort_spinner)
    Spinner mRedditSortSpinner;
    @BindView(R.id.reddit_top_posts_spinner)
    Spinner mRedditTopPostsSpinner;
    @BindView(R.id.reddit_posts_list)
    RecyclerView mRedditPostList;

    private Context mContext;
    private MenuItem mRefreshMenuItem;

    private static final String[] SORT_OPTION_SUFFIXES = {"hot.json", "top.json", "new.json"};
    private static final String UWATERLOO_SUBREDDIT_URL = "https://www.reddit.com/r/uwaterloo/";
    private static final int MAX_SELFTEXT_LENGTH = 160;

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
        setSpinnerSelectionListeners();
        refreshRedditList();
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
                return true;
            case R.id.menu_browser:
                IntentUtils.openBrowser(mContext, UWATERLOO_SUBREDDIT_URL);
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public String getToolbarTitle() {
        return getString(R.string.title_uwaterloo_subreddit);
    }

    private void refreshRedditList() {
        System.err.println("refreshRedditList");

        try {
            // Create a request using the OkHttpClient library
            OkHttpClient okHttpClient = new OkHttpClient();

            int sortOptionIndex = Math.max(0, mRedditSortSpinner.getSelectedItemPosition());
            String url = UWATERLOO_SUBREDDIT_URL + SORT_OPTION_SUFFIXES[sortOptionIndex];
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
                            RedditPostList redditPosts = new RedditPostList(dataChildren);

                            activity.runOnUiThread(() ->
                                    displayQueryResults(redditPosts));
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
                        activity.runOnUiThread(() -> Toast.makeText(
                                activity, errorText, Toast.LENGTH_SHORT).show());
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
                refreshRedditList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mRedditTopPostsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshRedditList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void displayQueryResults(RedditPostList redditPosts) {
        mRedditPostList.setAdapter(new RedditPostAdapter(redditPosts));
    }

    /**
     * A custom RecyclerView Adapter for the list of Reddit posts.
     */
    class RedditPostAdapter extends RecyclerView.Adapter<RedditPostViewHolder> {
        private RedditPostList mRedditPosts;

        RedditPostAdapter(RedditPostList redditPosts) {
            mRedditPosts = redditPosts;
        }

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
            String score = Integer.toString(redditPost.getScore());
            String numComments = Integer.toString(redditPost.getNumComments());
            String header;


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
        }

        @Override
        public int getItemCount() {
            return mRedditPosts.size();
        }
    }

    private void setTextOfTextView(TextView textView, String text) {
        if (text == null || text.length() == 0) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(text);
        }
    }

    /**
     * A custom RecyclerView ViewHolder for an item in the list of open classrooms.
     */
    class RedditPostViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.reddit_post_header)
        TextView headerText;
        @BindView(R.id.reddit_post_title)
        TextView titleText;
        @BindView(R.id.reddit_post_link_flair)
        TextView linkFlairText;
        @BindView(R.id.reddit_post_selftext)
        TextView selftextText;
        @BindView(R.id.reddit_post_content_icon)
        ImageView contentIconImage;
        @BindView(R.id.reddit_post_score)
        TextView scoreText;
        @BindView(R.id.reddit_post_num_comments)
        TextView numCommentsText;

        RedditPostViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
