package io.github.wztlei.wathub.ui.view;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.datepicker.DatePickerDialog;
import io.github.wztlei.wathub.utils.Px;

import org.joda.time.LocalDate;

public class DateSelectorView extends FrameLayout implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    public static final String TAG = DateSelectorView.class.getSimpleName();

    private OnDateChangedListener mListener;
    private TextView mPickerButton;
    private LocalDate mDate;

    public DateSelectorView(final Context context) {
        this(context, null);
    }

    public DateSelectorView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DateSelectorView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.date_picker_view, this);

        final TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorPrimary, value, true);
        setBackgroundColor(value.data);
        setElevation(Px.fromDpF(8));

        mPickerButton = (TextView) findViewById(R.id.date_picker_view);
        mPickerButton.setOnClickListener(this);

        onDateCleared();

        post(() -> {
            final DatePickerDialog dialog =
                    (DatePickerDialog) getFragmentManager().findFragmentByTag(TAG);
            if (dialog != null) {
                dialog.setOnDateSetListener(DateSelectorView.this);
            }
        });
    }

    private FragmentManager getFragmentManager() {
        return ((FragmentActivity) getContext()).getSupportFragmentManager();
    }

    private void bindView() {
        final SpannableString content = new SpannableString(mDate.toString("MMMM d, YYYY"));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        mPickerButton.setText(content);

        if (mListener != null) {
            mListener.onDateSet(mDate.getYear(), mDate.getMonthOfYear() - 1, mDate.getDayOfMonth());
        }
    }

    public LocalDate getDate() {
        return new LocalDate(mDate);
    }

    public void setOnDateSetListener(final OnDateChangedListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(final View v) {
        final DatePickerDialog dialog = DatePickerDialog.newInstance(
                this, mDate.getYear(), mDate.getMonthOfYear() - 1, mDate.getDayOfMonth());
        dialog.setHighlightWeeksEnabled(true);
        dialog.setClearButtonVisibility(true);
        dialog.show(getFragmentManager(), TAG);
    }

    @Override
    public void onDateSet(
            final DatePickerDialog datePickerDialog,
            final int year,
            final int monthOfYear,
            final int dayOfMonth) {
        mDate = mDate.withYear(year).withMonthOfYear(monthOfYear + 1).withDayOfMonth(dayOfMonth);
        bindView();
    }

    @Override
    public void onDateCleared() {
        mDate = LocalDate.now();
        bindView();
    }

    public interface OnDateChangedListener {
        void onDateSet(int year, int monthOfYear, int dayOfMonth);
    }

}
