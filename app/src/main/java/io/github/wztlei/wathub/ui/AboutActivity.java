package io.github.wztlei.wathub.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.view.View;

import java.util.Objects;

import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.ui.view.ElevationOffsetListener;
import io.github.wztlei.wathub.utils.FontUtils;
import io.github.wztlei.wathub.utils.Px;

import butterknife.BindString;
import butterknife.BindView;

public class AboutActivity extends BaseActivity {

    @BindView(R.id.appbar)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindString(R.string.menu_about)
    String mAboutString;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        mAppBarLayout.addOnOffsetChangedListener(new ElevationOffsetListener(Px.fromDpF(8)));

        final Typeface typeface = FontUtils.getFont(FontUtils.BOOK);
        mCollapsingLayout.setCollapsedTitleTypeface(typeface);
        mCollapsingLayout.setExpandedTitleTypeface(typeface);
        mCollapsingLayout.setTitle(mAboutString);

        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mAboutString);
            actionBar.setElevation(Px.fromDpF(8));
        }

        final Button button = (Button) findViewById(R.id.goto_feedback_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), FeedbackActivity.class));
                overridePendingTransition(R.anim.bottom_in, R.anim.stay);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.stay, R.anim.bottom_out);
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
