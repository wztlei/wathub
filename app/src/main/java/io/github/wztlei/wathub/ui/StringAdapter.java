package io.github.wztlei.wathub.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class StringAdapter
    extends ArrayAdapter<Object> {

  private int mViewId = android.R.layout.simple_list_item_1;
  private int mDropdownId = android.R.layout.simple_list_item_1;

  public StringAdapter(final Context context, final List<Object> objects) {
    this(context, objects, 0);
  }

  public StringAdapter(final Context context, final List<Object> objects, final int layoutId) {
    super(context, layoutId, objects);
  }

  @NonNull
  @Override
  public View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
    return doGetView(position, convertView, parent, mViewId);
  }

  @Override
  public View getDropDownView(
      final int position,
      final View convertView,
      @NonNull final ViewGroup parent) {
    return doGetView(position, convertView, parent, mDropdownId);
  }

  public void setViewLayoutId(final int viewId) {
    mViewId = viewId;
  }

  public void setDropdownLayoutId(final int dropdownId) {
    mDropdownId = dropdownId;
  }

  private View doGetView(
      final int position, final View convertView, final ViewGroup parent, final int layoutResId) {
    final View view;
    if (convertView == null) {
      view = LayoutInflater.from(getContext()).inflate(layoutResId, parent, false);
    } else {
      view = convertView;
    }

    final TextView textView;
    if (view instanceof TextView) {
      textView = (TextView) view;
    } else {
      textView = (TextView) view.findViewById(android.R.id.text1);
    }

    textView.setText(String.valueOf(getItem(position)));

    return view;
  }
}
