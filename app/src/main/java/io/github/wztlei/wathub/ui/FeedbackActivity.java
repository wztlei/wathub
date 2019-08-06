package io.github.wztlei.wathub.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.utils.IntentUtils;
import io.github.wztlei.wathub.utils.Px;

// Rolf Li, July 2019

public class FeedbackActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindString(R.string.feedback_input)
    String mFeedbackString;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mFeedbackString);
            actionBar.setElevation(Px.fromDpF(8));
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.stay, R.anim.bottom_out);
    }

    @OnClick(R.id.feedback_browser)
    public void onOpenInBrowserClicked() {
        IntentUtils.openBrowser(FeedbackActivity.this, Constants.FEEDBACK_GOOGLE_FORM_URL);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}