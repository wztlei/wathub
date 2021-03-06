package io.github.wztlei.wathub.ui.modules;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;

import com.deange.uwaterlooapi.annotations.ModuleInfo;

import io.github.wztlei.wathub.ui.ModuleListItemListener;
import io.github.wztlei.wathub.utils.Px;

public class ApiMethodsFragment extends ListFragment implements ModuleListItemListener {

    private static final String ARG_METHODS = "methods";
    @SuppressWarnings("unused")
    private static final String TAG = "WL/ApiMethodsFragment";

    public static ApiMethodsFragment newInstance(final String[] endpoints) {
        final ApiMethodsFragment fragment = new ApiMethodsFragment();

        final Bundle args = new Bundle();
        args.putStringArray(ARG_METHODS, endpoints);
        fragment.setArguments(args);

        return fragment;
    }

    public static void openModule(final Context context, final String endpoint) {
        final ModuleInfo fragmentInfo = ModuleMap.getFragmentInfo(endpoint);
        String fragmentCanonicalName = fragmentInfo.fragment.getCanonicalName();
        context.startActivity(ModuleHostActivity.getStartIntent(context, fragmentCanonicalName));
    }

    /**
     * Required empty public constructor
     */
    public ApiMethodsFragment() {}

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final int padding = Px.fromDp(4);
        getListView().setPadding(0, padding, 0, padding);
        getListView().setDivider(null);
        getListView().setDividerHeight(0);

        final String[] methods = getArguments().getStringArray(ARG_METHODS);
        try {
            if (methods != null) {
                setListAdapter(new ApiMethodsAdapter(
                        getActivity(), methods, ApiMethodsFragment.this));
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClicked(final int position) {
        final String[] methods = getArguments().getStringArray(ARG_METHODS);
        if (methods != null) {
            openModule(getActivity(), methods[position]);
        }
    }
}
