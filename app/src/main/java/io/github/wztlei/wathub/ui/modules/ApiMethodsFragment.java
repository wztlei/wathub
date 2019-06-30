package io.github.wztlei.wathub.ui.modules;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;

import com.deange.uwaterlooapi.annotations.ModuleInfo;
import com.deange.uwaterlooapi.annotations.ModuleMap;
import io.github.wztlei.wathub.ui.ModuleListItemListener;
import io.github.wztlei.wathub.utils.Px;

public class ApiMethodsFragment extends ListFragment
    implements
    ModuleListItemListener {

  private static final String ARG_METHODS = "methods";

  public static ApiMethodsFragment newInstance(final String[] endpoints) {
    final ApiMethodsFragment fragment = new ApiMethodsFragment();

    final Bundle args = new Bundle();
    args.putStringArray(ARG_METHODS, endpoints);
    fragment.setArguments(args);

    return fragment;
  }

  public static void openModule(final Context context, final String endpoint) {
    final ModuleInfo fragmentInfo = ModuleMap.getFragmentInfo(endpoint);
    context.startActivity(ModuleHostActivity.getStartIntent(context, fragmentInfo.fragment));
  }

  public ApiMethodsFragment() {
    // Required empty public constructor
  }

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    final int padding = Px.fromDp(4);
    getListView().setPadding(0, padding, 0, padding);
    getListView().setDivider(null);
    getListView().setDividerHeight(0);

    final String[] methods = getArguments().getStringArray(ARG_METHODS);
    if (methods != null) {
      setListAdapter(new ApiMethodsAdapter(getActivity(), methods, ApiMethodsFragment.this));
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
