package io.github.wztlei.wathub.ui.modules.home;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.common.UpperCaseTextWatcher;
import io.github.wztlei.wathub.ui.modules.ModuleHostActivity;
import io.github.wztlei.wathub.ui.modules.courses.CourseFragment;
import io.github.wztlei.wathub.ui.modules.courses.CoursesFragment;
import io.github.wztlei.wathub.ui.modules.courses.SubjectAdapter;
import io.github.wztlei.wathub.ui.modules.weather.WeatherFragment;
import io.github.wztlei.wathub.utils.Px;
import io.github.wztlei.wathub.utils.SimpleTextWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;

public class HomeFragment extends Fragment implements AdapterView.OnItemClickListener {

    private final TextWatcher mCourseTextWatcher = new SimpleTextWatcher() {
        @Override
        public void afterTextChanged(final Editable s) {
            if (mSubjectPicker == null || mNumberPicker == null) {
                return;
            }

            final String subject = mSubjectPicker.getText().toString().trim();
            final String number = mNumberPicker.getText().toString().trim();
            final boolean validSubject = mAdapter.getSubjects().contains(subject);

            mSearchButton.setEnabled(validSubject);
            if (!validSubject) {
                return;
            }

            final String buttonText;
            if (TextUtils.isEmpty(number)) {
                buttonText = getString(R.string.home_quick_course_search_subject, subject);

            } else {
                buttonText = getString(R.string.home_quick_course_search_course, subject + " " + number);
            }

            mSearchButton.setText(buttonText);
        }
    };

    private float mElevation;
    private Toolbar mToolbar;
    private SubjectAdapter mAdapter;

    @BindView(R.id.home_course_subject)
    AutoCompleteTextView mSubjectPicker;
    @BindView(R.id.home_course_number)
    EditText mNumberPicker;
    @BindView(R.id.home_course_search)
    Button mSearchButton;
    @BindView(R.id.home_cards_parent)
    ViewGroup mCardsParent;

    private NearbyLocationsFragment mNearbyLocationsFragment;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(getString(R.string.app_name));
    }

    @Nullable
    @Override
    public View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        ButterKnife.bind(this, view);

        mCardsParent.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        mElevation = mToolbar.getElevation();
        mToolbar.setElevation(Px.fromDpF(8));

        mAdapter = new SubjectAdapter(getActivity());
        mSubjectPicker.setAdapter(mAdapter);
        mSubjectPicker.setOnItemClickListener(this);
        mSubjectPicker.addTextChangedListener(mCourseTextWatcher);
        mSubjectPicker.addTextChangedListener(new UpperCaseTextWatcher(mSubjectPicker));

        mNumberPicker.addTextChangedListener(mCourseTextWatcher);
        mNumberPicker.addTextChangedListener(new UpperCaseTextWatcher(mNumberPicker));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mNearbyLocationsFragment = (NearbyLocationsFragment)
                getChildFragmentManager().findFragmentById(R.id.home_nearby_locations_fragment);
    }

    @Override
    public void onDestroyView() {
        mToolbar.setElevation(mElevation);

        super.onDestroyView();
    }

    @OnClick(R.id.home_weather_selectable)
    public void onWeatherClicked() {
        startActivity(ModuleHostActivity.getStartIntent(
                getContext(), WeatherFragment.class.getCanonicalName()));
    }

    @OnClick(R.id.home_course_search)
    public void onCourseSearchClicked() {
        final String subject = mSubjectPicker.getText().toString().trim();
        final String code = mNumberPicker.getText().toString().trim();

        final Intent intent;
        if (!TextUtils.isEmpty(code)) {
            intent = ModuleHostActivity.getStartIntent(
                    getContext(),
                    CourseFragment.class.getCanonicalName(),
                    CourseFragment.newBundle(subject, code));
        } else {
            intent = ModuleHostActivity.getStartIntent(
                    getContext(),
                    CoursesFragment.class.getCanonicalName(),
                    CoursesFragment.newBundle(subject));
        }

        startActivity(intent);
    }

    @OnEditorAction({R.id.home_course_subject, R.id.home_course_number})
    public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            mNumberPicker.requestFocus();
            return true;

        } else if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            onCourseSearchClicked();
            return true;
        }

        return false;
    }

    @Override
    public void onItemClick(
            final AdapterView<?> parent,
            final View view,
            final int position,
            final long id) {
        mNumberPicker.requestFocus();
    }
}
