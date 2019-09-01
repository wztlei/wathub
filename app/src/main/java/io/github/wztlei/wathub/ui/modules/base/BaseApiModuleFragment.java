package io.github.wztlei.wathub.ui.modules.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Toast;

import com.deange.uwaterlooapi.UWaterlooApi;
import com.deange.uwaterlooapi.model.AbstractModel;
import com.deange.uwaterlooapi.model.BaseResponse;
import com.deange.uwaterlooapi.model.Metadata;
import com.deange.uwaterlooapi.model.common.SimpleListResponse;

import java.util.List;

import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.controller.NetworkController;
import io.github.wztlei.wathub.net.Calls;
import io.github.wztlei.wathub.ui.modules.ModuleHostActivity;
import retrofit2.Call;

public abstract class BaseApiModuleFragment<T extends Parcelable, V extends AbstractModel>
        extends BaseModuleFragment
        implements View.OnTouchListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String KEY_MODEL = "model";
    private static final String KEY_RESPONSE = "response";
    private static final String KEY_LAST_UPDATED = "last_updated";
    private static final String TAG = "BaseApiModuleFragment";

    private long mLastUpdate = 0;
    private ViewGroup mLoadingLayout;
    private ViewGroup mNetworkLayout;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private T mLastResponse;
    private Animator mLoadingAnimator;
    private LoadModuleDataTask<T, V> mTask;
    private SwipeRefreshLayout mSwipeLayout;

    public static <V extends AbstractModel> Bundle newBundle(final V model) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_MODEL, model);
        return bundle;
    }

    /**
     * Required constructor
     */
    public BaseApiModuleFragment() {
        setHasOptionsMenu(true);
    }

    public ModuleHostActivity getHostActivity() {
        return (ModuleHostActivity) getActivity();
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (!(context instanceof ModuleHostActivity)) {
            throw new RuntimeException("Parent activity not an instance of ModuleHostActivity");
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public final View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_module, container, false);
        final ViewGroup parent = root.findViewById(R.id.container_content_view);

        mLoadingLayout = root.findViewById(R.id.loading_layout);
        mLoadingLayout.setOnTouchListener(this);

        mNetworkLayout = root.findViewById(R.id.no_network_layout);
        mNetworkLayout.setOnTouchListener(this);

        final View contentView = getContentView(inflater, parent);
        if (contentView != null && contentView.getParent() == null) {
            parent.addView(contentView);
        }

        mSwipeLayout = root.findViewById(R.id.fragment_swipe_container);
        if (mSwipeLayout != null) {
            mSwipeLayout.setOnRefreshListener(this);
        }

        return root;
    }

    protected abstract View getContentView(final LayoutInflater inflater, final ViewGroup parent);

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mLastUpdate = savedInstanceState.getLong(KEY_LAST_UPDATED);
            mLastResponse = savedInstanceState.getParcelable(KEY_RESPONSE);
        }

        // Deliver the response if we still have one, otherwise load the data
        // (usually from coming back from another activity or rotating)
        // postDelayed() so that the SwipeRefreshLayout draws the indicator correctly (http://stackoverflow.com/a/26860930)
        postDelayed(() -> {
            if (mLastResponse != null) {
                onRefreshRequested();
                deliverResponse(mLastResponse);

            } else if (mLastUpdate == 0) {
                doRefresh();
            }
        }, 100);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        if (getModel() == null) {
            // If there is a response available in memory, no need to "refresh"
            inflater.inflate(R.menu.menu_base_module, menu);
        }

        syncRefreshMenuItem(menu);
    }

    protected final void syncRefreshMenuItem(final Menu menu) {
        final MenuItem refreshItem = menu.findItem(R.id.menu_refresh);
        if (refreshItem != null) {
            refreshItem.setVisible(noTaskRunning());
            refreshItem.setEnabled(noTaskRunning());
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            // Refresh manually requested
            doRefresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_LAST_UPDATED, mLastUpdate);
        outState.putParcelable(KEY_RESPONSE, mLastResponse);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    public void showModule(final Class<? extends BaseApiModuleFragment> fragment, final Bundle arguments) {
        // TODO BUG #1: Potential cause of Android 7/Nougat TransactionTooLargeException
        getActivity().startActivity(ModuleHostActivity.getStartIntent(
                getActivity(), fragment.getCanonicalName(), arguments));
    }

    public <M> M getModel() {
        // noinspection unchecked
        return (M) getArguments().getParcelable(KEY_MODEL);
    }

    @Override
    public void onRefresh() {
        doRefresh();
    }

    protected final void doRefresh() {

        onRefreshRequested();

        // Data can potentially be stored from above (maybe cached, or passed in as a Parcelable?)
        // In that case, there's no need to try and load any data from the network
        // Deliver it right away to the main thread!
        final V data = onLoadData();
        if (data != null) {
            mHandler.post(() -> {
                deliverData(data);
                onLoadFinished();
            });

        } else {
            changeLoadingVisibilityInternal(true);
            mLastUpdate = System.currentTimeMillis();

            mTask = new LoadModuleDataTask<>(this, getActivity() == null);
            mTask.execute(getApi());
        }
    }

    public UWaterlooApi getApi() {
        return ((ModuleHostActivity) getActivity()).getApi();
    }

    /**
     * Overridden by subclasses
     */
    protected void onRefreshRequested() {
    }

    private void changeLoadingVisibilityInternal(final boolean show) {

        // Allow the refresh menu item to be updated
        getActivity().supportInvalidateOptionsMenu();

        changeLoadingVisibility(show);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void changeLoadingVisibility(final boolean show) {
        if (mSwipeLayout != null) {
            mSwipeLayout.setRefreshing(show);
            mSwipeLayout.setEnabled(!show);
        }

        if (show) {
            mLoadingLayout.setVisibility(View.VISIBLE);
            if (mLastUpdate == 0) {
                return;
            }
        }

        final AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                mLoadingLayout.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(final Animator animation) {
                mLoadingLayout.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            }
        };

        if (mLoadingAnimator != null) {
            mLoadingAnimator.cancel();
        }

        mLoadingAnimator = getVisibilityAnimator(mLoadingLayout, show);

        if (mLoadingAnimator != null) {
            mLoadingAnimator.setInterpolator(ANIMATION_INTERPOLATOR);
            mLoadingAnimator.setDuration(ANIMATION_DURATION);
            mLoadingAnimator.addListener(listener);
            mLoadingAnimator.start();
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
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        // Loading/Network layouts intercept all touch events
        return true;
    }

    private boolean noTaskRunning() {
        return mTask == null;
    }

    protected void onContentShown() {
        // Can be Overridden by subclasses
    }

    protected void onLoadFinished() {
        final long delay;
        if (mSwipeLayout != null || mLastResponse == null) {
            // No minimum delay for the SwipeRefreshLayout or no response at all
            delay = 0;

        } else {
            // We want to keep the refresh UI up for *at least* MIN_UPDATE_DURATION
            // Otherwise it looks very choppy and overall not a pleasant look
            final long now = System.currentTimeMillis();
            delay = MIN_UPDATE_DURATION - (now - mLastUpdate);
        }

        mTask = null;
        mHandler.postDelayed(() -> {
            if (getActivity() != null) {
                // Ensure we haven't been detached
                changeLoadingVisibilityInternal(false);
                onContentShown();
            }
        }, delay);
    }

    protected void onNullResponseReceived() {
        Activity activity = getActivity();

        if (activity != null) {
            Toast.makeText(activity, activity.getText(R.string.error_no_network),
                    Toast.LENGTH_SHORT).show();
        }
    }

    protected void onNoDataReturned() {
        Activity activity = getActivity();

        if (activity != null) {
            Toast.makeText(activity, activity.getText(R.string.error_no_network),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void resolveNetworkLayoutVisibility() {
        final boolean connected = NetworkController.getInstance().isConnected();

        final Animator animator = getVisibilityAnimator(mNetworkLayout, !connected);
        if (animator != null && !connected && mNetworkLayout.getVisibility() == View.INVISIBLE) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(final Animator animation) {
                    mNetworkLayout.setVisibility(View.VISIBLE);
                }
            });

            animator.start();

        } else if (animator != null && connected && mNetworkLayout.getVisibility() == View.VISIBLE) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(final Animator animation) {
                    mNetworkLayout.setVisibility(View.INVISIBLE);
                }
            });

            animator.start();
        }
    }

    @SuppressWarnings("unchecked")
    protected void deliverResponse(final T data) {
        resolveNetworkLayoutVisibility();

        if (data == null) {
            onNullResponseReceived();

        } else if (data instanceof BaseResponse) {
            final BaseResponse response = (BaseResponse) data;
            final Metadata metadata = response.getMetadata();

            if (response.getData() == null) {
                onNullResponseReceived();

            } else if (metadata != null && metadata.getStatus() == 204) {
                onNoDataReturned();

            } else if (response instanceof SimpleListResponse) {
                onBindData(metadata, (List<V>) response.getData());

            } else {
                onBindData(metadata, (V) response.getData());
            }
        }

        if (getActivity() != null) {
            ((ModuleHostActivity) getActivity()).refreshActionBar();
        }
    }

    protected void deliverData(final V data) {
        if (data == null) {
            onNullResponseReceived();
        } else {
            onBindData(null, data);
        }

        if (getActivity() != null) {
            ((ModuleHostActivity) getActivity()).refreshActionBar();
        }
    }

    public void post(final Runnable runnable) {
        mHandler.post(runnable);
    }

    public void postDelayed(final Runnable runnable, final long delay) {
        mHandler.postDelayed(runnable, delay);
    }

    public Call<T> onLoadData(final UWaterlooApi api) {
        // Overridden by subclasses
        return null;
    }

    public V onLoadData() {
        // Overridden by subclasses
        return null;
    }

    public void onBindData(final Metadata metadata, final V data) {
        // Overridden by subclasses
    }

    public void onBindData(final Metadata metadata, final List<V> data) {
        // Overridden by subclasses
    }

    public abstract String getContentType();

    private void setLastResponse(T lastResponse) {
        mLastResponse = lastResponse;
    }

    private static final class LoadModuleDataTask<T extends Parcelable, V extends AbstractModel>
            extends AsyncTask<UWaterlooApi, Void, T> {

        private BaseApiModuleFragment<T, V> mBaseApiModuleFragment;
        private boolean mIsActivityNull;

        private LoadModuleDataTask(BaseApiModuleFragment<T, V> baseApiModuleFragment,
                                   boolean isActivityNull) {
            mBaseApiModuleFragment = baseApiModuleFragment;
            mIsActivityNull = isActivityNull;
        }

        @Override
        protected T doInBackground(final UWaterlooApi... apis) {
            // Performed on a background thread, so network calls are performed here
            try {
                if (NetworkController.getInstance().isConnected()) {
                    final Call<T> call = mBaseApiModuleFragment.onLoadData(apis[0]);
                    if (call != null) {
                        return Calls.unwrap(call);
                    }
                } else {
                    Thread.sleep(MIN_UPDATE_DURATION);
                }
            } catch (final Exception e) {
                Log.w("LoadModuleDataTask", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(final T data) {
            if (!mIsActivityNull) {
                // Performed on the main thread, so view manipulation is performed here
                mBaseApiModuleFragment.setLastResponse(data) ;
                mBaseApiModuleFragment.onLoadFinished();
                mBaseApiModuleFragment.deliverResponse(data);
            }
        }
    }
}
