package io.github.wztlei.wathub.ui.modules.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
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

    /**
     * Displays the loading screen when the fragment is refreshed.
     *
     * @param loadingLayout     the layout which contains the loading screen
     * @param refreshMenuItem   the menu item for the user to refresh the fragment
     * @param initialDisplay    true if the fragment has just been started, and false otherwise
     */
    protected void displayLoadingScreen(SwipeRefreshLayout swipeRefreshLayout, View loadingLayout,
                                        MenuItem refreshMenuItem, boolean initialDisplay) {
        changeLoadingVisibility(swipeRefreshLayout, loadingLayout, refreshMenuItem, initialDisplay, true);
    }

    /**
     * Reveals and hides the visibility of the loading screen with a circular animation.
     *
     * @param loadingLayout     the layout which contains the loading screen
     * @param refreshMenuItem   the menu item for the user to refresh the fragment
     * @param initialDisplay    true if the fragment has just been started, and false otherwise
     * @param show              true if the loading screen is to be revealed, and false otherwise
     */
    private void changeLoadingVisibility(SwipeRefreshLayout swipeRefreshLayout, View loadingLayout,
                                         MenuItem refreshMenuItem,
                                         boolean initialDisplay, boolean show) {
        Runnable hideLoadingVisibility = () ->
                changeLoadingVisibility(swipeRefreshLayout, loadingLayout,
                        refreshMenuItem, false, false);

        // Update the appearance of the swipe refresh layout
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(show);
            swipeRefreshLayout.setEnabled(!show);
        }

        // Update the visibility of the refresh menu item
        if (refreshMenuItem != null) {
            refreshMenuItem.setVisible(!show);
            refreshMenuItem.setEnabled(!show);
        }

        // Just hide the loading screen if the fragment has just been created
        if (initialDisplay) {
            mIsRefreshing = true;
            new Handler(Looper.getMainLooper()).postDelayed(
                    hideLoadingVisibility, MINIMUM_UPDATE_DURATION);
            return;
        } else if (mIsRefreshing && show) {
            return;
        }

        // Update the visibility of the loading layout at the end of the animation
        AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                loadingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onAnimationCancel(final Animator animation) {
                loadingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        };

        // Cancel the loading animation if needed
        if (mLoadingAnimator != null) {
            mLoadingAnimator.cancel();
        }

        // Create a new loading animation
        mLoadingAnimator = getVisibilityAnimator(loadingLayout, show);

        // Start a new loading animation
        if (mLoadingAnimator != null) {
            mLoadingAnimator.setInterpolator(ANIMATION_INTERPOLATOR);
            mLoadingAnimator.setDuration(ANIMATION_DURATION);
            mLoadingAnimator.addListener(listener);
            mLoadingAnimator.start();
        }

        // If we are showing the loading screen, then hide it after a delay
        if (show) {
            mIsRefreshing = true;
            loadingLayout.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(
                    hideLoadingVisibility, DEFAULT_REFRESH_DURATION);
        } else {
            mIsRefreshing = false;
        }
    }

    /**
     * Creates an circular animation that reveals or hides a view.
     *
     * @param view  the view to reveal or hide
     * @param show  true if the view is to be revealed, or false if the view is to be hidden
     * @return      a circular animation
     */
    private Animator getVisibilityAnimator(final View view, final boolean show) {
        // Get the dimensions of the animation
        final int full = Math.max(view.getWidth(), view.getHeight());
        final int startRadius = show ? 0 : full;
        final int finalRadius = show ? full : 0;
        final int centerX = (view.getLeft() + view.getRight()) / 2;
        final int centerY = (view.getTop() + view.getBottom()) / 2;

        // Create a circular revealing animation
        try {
            return ViewAnimationUtils.createCircularReveal(view, centerX, centerY, startRadius,
                    finalRadius);
        } catch (IllegalStateException e) {
            Log.w(TAG, e.getMessage());
            return null;
        }
    }

    /**
     * Returns the title of the toolbar. This method is overridden by subclasses.
     *
     * @return the title of the toolbar at the top of the fragment
     */
    public String getToolbarTitle() {
        return null;
    }

    /**
     * Returns the subtitle of the toolbar. This method is overridden by subclasses.
     *
     * @return the subtitle of the toolbar at the top of the fragment
     */
    public String getToolbarSubtitle() {
        return null;
    }

    /**
     * Returns the pixel elevation of the toolbar. This method is overridden by subclasses.
     *
     * @return the pixel elevation of the toolbar
     */
    public float getToolbarElevationPx() {
        return Px.fromDpF(8);
    }
}
