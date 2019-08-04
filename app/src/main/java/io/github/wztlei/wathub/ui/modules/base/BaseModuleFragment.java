package io.github.wztlei.wathub.ui.modules.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Interpolator;

import io.github.wztlei.wathub.utils.Px;

public class BaseModuleFragment extends Fragment {

    private Animator mLoadingAnimator;
    private boolean mIsRefreshing;

    protected static final Interpolator ANIMATION_INTERPOLATOR = new FastOutSlowInInterpolator();
    protected static final int MINIMUM_UPDATE_DURATION = 1000;
    protected static final int ANIMATION_DURATION = 300;

    protected static final int DEFAULT_REFRESH_DURATION = 500;
    private static final String TAG = "WL/BaseModuleFragment";

    public String getToolbarTitle() {
        return null;
    }

    public String getToolbarSubtitle() {
        return null;
    }

    /**
     * Overridden by subclasses.
     *
     * @return the pixel elevation of the toolbar
     */
    public float getToolbarElevationPx() {
        return Px.fromDpF(8);
    }

    protected void displayLoadingScreen(View loadingLayout, MenuItem menuItem,
                                        boolean initialDisplay) {
        changeLoadingVisibility(loadingLayout, menuItem, initialDisplay, true);
    }

    private void changeLoadingVisibility(View loadingLayout, MenuItem menuItem,
                                         boolean initialDisplay, boolean show) {
        if (menuItem != null) {
            menuItem.setVisible(!show);
            menuItem.setEnabled(!show);
        }

        if (initialDisplay) {
            mIsRefreshing = true;
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                            changeLoadingVisibility(loadingLayout, menuItem,
                                    false, false),
                    MINIMUM_UPDATE_DURATION);
            return;
        } else if (mIsRefreshing && show) {
            return;
        }

        final AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                loadingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onAnimationCancel(final Animator animation) {
                loadingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        };

        if (mLoadingAnimator != null) {
            mLoadingAnimator.cancel();
        }

        mLoadingAnimator = getVisibilityAnimator(loadingLayout, show);

        if (mLoadingAnimator != null) {
            mLoadingAnimator.setInterpolator(ANIMATION_INTERPOLATOR);
            mLoadingAnimator.setDuration(ANIMATION_DURATION);
            mLoadingAnimator.addListener(listener);
            mLoadingAnimator.start();
        }

        if (show) {
            mIsRefreshing = true;
            loadingLayout.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                            changeLoadingVisibility(loadingLayout, menuItem, false, false),
                    DEFAULT_REFRESH_DURATION);
        } else {
            mIsRefreshing = false;
        }
    }

    private Animator getVisibilityAnimator(final View view, final boolean show) {
        final int full = Math.max(view.getWidth(), view.getHeight());
        final int startRadius = (show) ? 0 : full;
        final int finalRadius = (show) ? full : 0;
        final int centerX = (view.getLeft() + view.getRight()) / 2;
        final int centerY = (view.getTop() + view.getBottom()) / 2;

        try {
            return ViewAnimationUtils.createCircularReveal(view, centerX, centerY, startRadius,
                    finalRadius);
        } catch (IllegalStateException e) {
            Log.w(TAG, e.getMessage());
            return null;
        }
    }
}
