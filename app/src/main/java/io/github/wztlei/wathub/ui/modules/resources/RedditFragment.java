package io.github.wztlei.wathub.ui.modules.resources;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.ui.modules.base.BaseModuleFragment;
import io.github.wztlei.wathub.utils.IntentUtils;

public class RedditFragment extends BaseModuleFragment {


    @BindView(R.id.reddit_sort_spinner)
    Spinner mRedditSortSpinner;
    @BindView(R.id.reddit_top_posts_spinner)
    Spinner mRedditTopPostsSpinner;

    private Context mContext;
    private MenuItem mRefreshMenuItem;

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
                IntentUtils.openBrowser(mContext, Constants.UWATERLOO_SUBREDDIT_URL);
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public String getToolbarTitle() {
        return getString(R.string.title_uwaterloo_subreddit);
    }

    /**
     * Sets OnItemSelectedListeners on the two spinners to display the new building and hour query
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
