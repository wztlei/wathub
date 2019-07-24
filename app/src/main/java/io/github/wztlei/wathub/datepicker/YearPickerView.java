/*
 * Copyright 2013 Christian De Angelis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.wztlei.wathub.datepicker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github.wztlei.wathub.R;


/**
 * Displays a selectable list of years.
 */
public class YearPickerView extends ListView
        implements OnItemClickListener, DatePickerDialog.OnDateChangedListener {
    @SuppressWarnings("unused")
    private static final String TAG = "WL/YearPickerView";

    private final DatePickerController mController;
    private YearAdapter mAdapter;
    private int mViewSize;
    private int mChildSize;
    private TextViewWithCircularIndicator mSelectedView;

    /**
     * @param context the context in which the YearPickerView is instantiated
     */
    public YearPickerView(
            Context context,
            DatePickerController controller) {
        super(context);
        mController = controller;
        mController.registerOnDateChangedListener(this);
        ViewGroup.LayoutParams frame = new ViewGroup.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        setLayoutParams(frame);
        Resources res = context.getResources();
        mViewSize = res.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height);
        mChildSize = res.getDimensionPixelOffset(R.dimen.year_label_height);
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(mChildSize / 3);
        init(context);
        setOnItemClickListener(this);
        setSelector(new StateListDrawable());
        setDividerHeight(0);
        onDateChanged();
    }

    private void init(Context context) {
        ArrayList<String> years = new ArrayList<>();
        for (int year = mController.getMinYear(); year <= mController.getMaxYear(); year++) {
            years.add(String.format(Locale.CANADA, "%d", year));
        }
        mAdapter = new YearAdapter(context, R.layout.view_datetimepicker_year_label_text_view, years);
        setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mController.tryVibrate();
        TextViewWithCircularIndicator clickedView = (TextViewWithCircularIndicator) view;
        if (clickedView != null) {
            if (clickedView != mSelectedView) {
                if (mSelectedView != null) {
                    mSelectedView.setDrawIndicator(false);
                    mSelectedView.requestLayout();
                }
                clickedView.setDrawIndicator(true);
                clickedView.requestLayout();
                mSelectedView = clickedView;
            }
            mController.onYearSelected(getYearFromTextView(clickedView));
            mAdapter.notifyDataSetChanged();
        }
    }

    private int getYearFromTextView(TextView view) {
        return Integer.valueOf(view.getText().toString());
    }

    public void postSetSelectionCentered(final int position) {
        postSetSelectionFromTop(position, mViewSize / 2 - mChildSize / 2);
    }

    public void postSetSelectionFromTop(final int position, final int offset) {
        post(() -> {
            setSelectionFromTop(position, offset);
            requestLayout();
        });
    }

    public int getFirstPositionOffset() {
        final View firstChild = getChildAt(0);
        if (firstChild == null) {
            return 0;
        }
        return firstChild.getTop();
    }

    @Override
    public void onDateChanged() {
        mAdapter.notifyDataSetChanged();
        postSetSelectionCentered(mController.getSelectedDay().year - mController.getMinYear());
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            event.setFromIndex(0);
            event.setToIndex(0);
        }
    }

    private class YearAdapter extends ArrayAdapter<String> {

        YearAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(
                int position,
                View convertView,
                @NonNull ViewGroup parent) {
            TextViewWithCircularIndicator v = (TextViewWithCircularIndicator)
                    super.getView(position, convertView, parent);
            v.requestLayout();
            int year = getYearFromTextView(v);
            boolean selected = mController.getSelectedDay().year == year;
            v.setDrawIndicator(selected);
            if (selected) {
                mSelectedView = v;
            }
            return v;
        }
    }
}
