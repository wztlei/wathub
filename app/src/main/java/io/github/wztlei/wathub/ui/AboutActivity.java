package io.github.wztlei.wathub.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;

import butterknife.BindString;
import butterknife.BindView;
import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.utils.IntentUtils;
import io.github.wztlei.wathub.utils.Px;

public class AboutActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindString(R.string.menu_about)
    String mAboutString;

    @SuppressWarnings("unused")
    private static final String TAG = "WL/AboutActivity";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the layout of the About activity
        setContentView(R.layout.activity_about);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        // Set up the action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mAboutString);
            actionBar.setElevation(Px.fromDpF(8));
        }

        // Set up a listener on the submit feedback button to open the Google form in a browser
        Button button = (Button) findViewById(R.id.submit_feedback_button);
        button.setOnClickListener(view -> {
            IntentUtils.openBrowser(AboutActivity.this, Constants.FEEDBACK_GOOGLE_FORM_URL);
//            startActivity(new Intent(getApplicationContext(), FeedbackActivity.class));
//            overridePendingTransition(R.anim.bottom_in, R.anim.stay);
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
