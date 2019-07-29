package io.github.wztlei.wathub.ui;

import android.graphics.Typeface;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.util.Log;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.*;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.ui.view.ElevationOffsetListener;
import io.github.wztlei.wathub.utils.FontUtils;
import io.github.wztlei.wathub.utils.IntentUtils;
import io.github.wztlei.wathub.utils.Px;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import io.github.wztlei.wathub.net.feedback.FeedbackInterface;

// Rolf Li, July 2019

public class FeedbackActivity extends BaseActivity {

    @BindView(R.id.appbarfeedback)
    AppBarLayout mAppBarLayout;
    //@BindView(R.id.collapsing_toolbar_feedback)
    //CollapsingToolbarLayout mCollapsingLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindString(R.string.feedback_input)
    String mFeedbackString;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        mAppBarLayout.addOnOffsetChangedListener(new ElevationOffsetListener(Px.fromDpF(8)));

        final Typeface typeface = FontUtils.getFont(FontUtils.BOOK);
        //mCollapsingLayout.setCollapsedTitleTypeface(typeface);
        //mCollapsingLayout.setExpandedTitleTypeface(typeface);
        //mCollapsingLayout.setTitle(mFeedbackString);

        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mFeedbackString);
            actionBar.setElevation(Px.fromDpF(8));
        }
    }

    // check for a network connection
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.stay, R.anim.bottom_out);
    }

    @OnClick(R.id.feedback_browser)
    public void onOpenInBrowserClicked() {
        IntentUtils.openBrowser(FeedbackActivity.this, "https://docs.google.com/forms/d/e/1FAIpQLSc3qx_rg_v6uZbzX6c0tw8uhOpsCRgAfH2-FiB1j0hLbIw_mA/viewform?usp=sf_link");
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