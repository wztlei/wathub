package io.github.wztlei.wathub.ui.modules.resources;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.ui.modules.base.BaseModuleFragment;
import io.github.wztlei.wathub.utils.IntentUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RedditFragment extends BaseModuleFragment {


    @BindView(R.id.reddit_sort_spinner)
    Spinner mRedditSortSpinner;
    @BindView(R.id.reddit_top_posts_spinner)
    Spinner mRedditTopPostsSpinner;

    private Context mContext;
    private MenuItem mRefreshMenuItem;

    private static final String[] SORT_OPTION_SUFFIXES = {"hot.json", "top.json", "new.json"};
    private static final String UWATERLOO_SUBREDDIT_URL = "https://www.reddit.com/r/uwaterloo/";

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
        setSpinnerSelectionListeners();
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
                        // Update the room schedules with a JSON string from the response body
                        // noinspection ConstantConditions
                        String jsonString = response.body().string();

                        // Use the UWaterloo API to get room schedules if needed
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NonNull final Call call, @NonNull IOException e) {
                    Toast.makeText(mContext, mContext.getText(R.string.error_no_network),
                            Toast.LENGTH_SHORT).show();
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

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mRedditTopPostsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
