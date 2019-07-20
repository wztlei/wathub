package io.github.wztlei.wathub.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class StringAdapter extends ArrayAdapter<Object> {

    private int mViewId = android.R.layout.simple_list_item_1;

    StringAdapter(final Context context, final List<Object> objects) {
        this(context, objects, 0);
    }

    public StringAdapter(final Context context, final String[] objects) {
        super(context, 0, objects);
    }

    private StringAdapter(final Context context, final List<Object> objects, final int layoutId) {
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
        int dropdownId = android.R.layout.simple_list_item_1;
        return doGetView(position, convertView, parent, dropdownId);
    }

    public void setViewLayoutId(final int viewId) {
        mViewId = viewId;
    }

    private View doGetView(final int position, final View convertView,
                           final ViewGroup parent, final int layoutResId) {
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
            textView = view.findViewById(android.R.id.text1);
        }

        textView.setText(String.valueOf(getItem(position)));

        return view;
    }
}
