package io.github.wztlei.wathub.ui.modules;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.deange.uwaterlooapi.UWaterlooApi;

import io.github.wztlei.wathub.ApiKeys;
import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.controller.WatcardManager;
import io.github.wztlei.wathub.ui.BaseActivity;
import io.github.wztlei.wathub.ui.modules.base.BaseApiModuleFragment;
import io.github.wztlei.wathub.ui.modules.base.BaseModuleFragment;
import io.github.wztlei.wathub.utils.FontUtils;


public class ModuleHostActivity extends BaseActivity implements FragmentManager.OnBackStackChangedListener {

    private static final String TAG = "WL/module_fragment";
    private static final String ARG_FRAGMENT_CLASS = "fragment_class";

    private UWaterlooApi mApi;
    private BaseModuleFragment mChildFragment;

    public static <T extends BaseModuleFragment> Intent getStartIntent(
            final Context context,
            final String fragmentCanonicalName) {
        return getStartIntent(context, fragmentCanonicalName, new Bundle());
    }

    public static <T extends BaseModuleFragment> Intent getStartIntent(
            final Context context,
            final String fragmentCanonicalName,
            final Bundle args) {
        final Intent intent = new Intent(context, ModuleHostActivity.class);

        intent.putExtra(ARG_FRAGMENT_CLASS, fragmentCanonicalName);
        if (args != null) {
            // TODO BUG #1: Potential cause of Android 7/Nougat TransactionTooLargeException
            intent.putExtras(args);
        }

        return intent;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void startActivityForResult(
            final Intent intent,
            final int requestCode,
            final Bundle options) {
        try {
            super.startActivityForResult(intent, requestCode, options);
        } catch (final ActivityNotFoundException e) {
            Log.e(TAG, "No Activity found to handle Intent", e);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_module_host_simple);

        mApi = new UWaterlooApi(ApiKeys.UWATERLOO_API_KEY);
        mApi.setWatcardCredentials(WatcardManager.getInstance().getCredentials());

        Toolbar toolbar = getToolbar();
        setSupportActionBar(toolbar);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        mChildFragment = findContentFragment();
        if (mChildFragment == null) {
            final String fragmentName = getIntent().getStringExtra(ARG_FRAGMENT_CLASS);
            Fragment fragment = Fragment.instantiate(this, fragmentName);

            if (fragment instanceof BaseApiModuleFragment) {
                showFragment((BaseApiModuleFragment) fragment, false,
                        getIntent().getExtras());
            } else if (fragment instanceof BaseModuleFragment){
                mChildFragment = (BaseModuleFragment) fragment;
                final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content, mChildFragment, TAG).commit();
            }
        }
        refreshActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshActionBar();
    }

    public Toolbar getToolbar() {
        return (Toolbar) findViewById(R.id.host_toolbar);
    }

    private BaseModuleFragment findContentFragment() {
        return (BaseModuleFragment) getSupportFragmentManager().findFragmentById(R.id.content);
    }

    public void showFragment(
            final BaseModuleFragment fragment, final boolean addToBackStack,
            final Bundle arguments) {
        mChildFragment = fragment;
        mChildFragment.setArguments(arguments);

        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (addToBackStack) {
            transaction.addToBackStack(mChildFragment.getClass().getCanonicalName());
        }
        transaction.replace(R.id.content, mChildFragment, TAG).commit();
    }

    public void refreshActionBar() {
        System.err.print("refreshActionBar");
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar == null || mChildFragment == null || !mChildFragment.isAdded()) {
            return;
        }

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(mChildFragment.getToolbarTitle());
        actionBar.setSubtitle(mChildFragment.getToolbarSubtitle());
        actionBar.setElevation(mChildFragment.getToolbarElevationPx());

        FontUtils.apply(getToolbar(), FontUtils.DEFAULT);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttachFragment(final Fragment fragment) {
        onBackStackChanged();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public UWaterlooApi getApi() {
        return mApi;
    }

    @Override
    public void onBackStackChanged() {
        mChildFragment = findContentFragment();
        refreshActionBar();
    }
}
