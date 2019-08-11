package io.github.wztlei.wathub.ui.modules.home;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.Calendar;

import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.common.UpperCaseTextWatcher;
import io.github.wztlei.wathub.controller.RoomScheduleManager;
import io.github.wztlei.wathub.model.RoomTimeInterval;
import io.github.wztlei.wathub.model.RoomTimeIntervalList;
import io.github.wztlei.wathub.ui.modules.ModuleHostActivity;
import io.github.wztlei.wathub.ui.modules.courses.CourseFragment;
import io.github.wztlei.wathub.ui.modules.courses.CoursesFragment;
import io.github.wztlei.wathub.ui.modules.courses.SubjectAdapter;
import io.github.wztlei.wathub.ui.modules.events.EventFragment;
import io.github.wztlei.wathub.ui.modules.events.EventsFragment;
import io.github.wztlei.wathub.ui.modules.openclassroom.OpenClassroomFragment;
import io.github.wztlei.wathub.ui.modules.poi.PointsOfInterestFragment;
import io.github.wztlei.wathub.ui.modules.weather.WeatherFragment;
import io.github.wztlei.wathub.utils.Px;
import io.github.wztlei.wathub.utils.SimpleTextWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;

public class HomeFragment extends Fragment implements AdapterView.OnItemClickListener {

    private float mElevation;
    private Toolbar mToolbar;
    private SubjectAdapter mAdapter;
    private Context mContext;

    @BindView(R.id.home_open_classroom_list)
    RecyclerView mHomeOpenClassroomList;
    @BindView(R.id.home_course_subject)
    AutoCompleteTextView mSubjectPicker;
    @BindView(R.id.home_course_number)
    EditText mNumberPicker;
    @BindView(R.id.home_course_search)
    Button mSearchButton;
    @BindView(R.id.home_cards_parent)
    ViewGroup mCardsParent;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(getString(R.string.app_name));
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        mToolbar = getActivity().findViewById(R.id.toolbar);

        ButterKnife.bind(this, view);

        mCardsParent.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        mElevation = mToolbar.getElevation();
        mToolbar.setElevation(Px.fromDpF(8));

        // Remember the last building selected
        String lastBuildingQueried = mContext
                .getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                .getString(Constants.BUILDING_KEY, "");
        Calendar searchDate = Calendar.getInstance();

        RoomTimeIntervalList buildingOpenSchedule =
                RoomScheduleManager.getInstance().findOpenRooms(lastBuildingQueried, searchDate);

        mHomeOpenClassroomList.setLayoutManager(new LinearLayoutManager(mContext));
        mHomeOpenClassroomList.setAdapter(new OpenClassroomAdapter(buildingOpenSchedule));

        TextWatcher courseTextWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(final Editable editable) {
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

                if (TextUtils.isEmpty(number)) {
                    mSearchButton.setText(getString(R.string.home_quick_course_search_subject,
                            subject));

                } else {
                    mSearchButton.setText(getString(R.string.home_quick_course_search_course,
                            subject + " " + number));
                }
            }
        };

        mAdapter = new SubjectAdapter(getActivity());
        mSubjectPicker.setAdapter(mAdapter);
        mSubjectPicker.setOnItemClickListener(this);
        mSubjectPicker.addTextChangedListener(courseTextWatcher);
        mSubjectPicker.addTextChangedListener(new UpperCaseTextWatcher(mSubjectPicker));

        mNumberPicker.addTextChangedListener(courseTextWatcher);
        mNumberPicker.addTextChangedListener(new UpperCaseTextWatcher(mNumberPicker));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        mToolbar.setElevation(mElevation);

        super.onDestroyView();
    }

    @OnClick(R.id.home_events_card)
    public void onEventsClicked() {
        startActivity(ModuleHostActivity.getStartIntent(
                mContext, EventsFragment.class.getCanonicalName()));
    }

    @OnClick(R.id.home_poi_card)
    public void onPointsOfInterestClicked() {
        startActivity(ModuleHostActivity.getStartIntent(
                mContext, PointsOfInterestFragment.class.getCanonicalName()));
    }

    @OnClick(R.id.home_open_classroom_see_more_buttom)
    public void onSeeMoreOpenClassroomsClicked() {
        startActivity(ModuleHostActivity.getStartIntent(
                mContext, OpenClassroomFragment.class.getCanonicalName()));
    }

//    @OnClick(R.id.home_weather_card)
//    public void onWeatherClicked() {
//        startActivity(ModuleHostActivity.getStartIntent(
//                mContext, WeatherFragment.class.getCanonicalName()));
//    }

    @OnClick(R.id.home_course_search)
    public void onCourseSearchClicked() {
        final String subject = mSubjectPicker.getText().toString().trim();
        final String code = mNumberPicker.getText().toString().trim();
        final Intent intent;

        // Determine if a course code has been entered
        if (TextUtils.isEmpty(code)) {
            // Perform a search for all of the courses of that subject
            intent = ModuleHostActivity.getStartIntent(
                    mContext,
                    CoursesFragment.class.getCanonicalName(),
                    CoursesFragment.newBundle(subject));
        } else {
            // Perform a search for the specific course of that subject and code
            intent = ModuleHostActivity.getStartIntent(
                    mContext,
                    CourseFragment.class.getCanonicalName(),
                    CourseFragment.newBundle(subject, code));
        }

        startActivity(intent);
    }

    @SuppressWarnings("unused")
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mNumberPicker.requestFocus();
    }

    /**
     * A custom RecyclerView Adapter for the list of open classrooms.
     */
    class OpenClassroomAdapter extends RecyclerView.Adapter<HomeFragment.ClassroomViewHolder> {
        private RoomTimeIntervalList mRoomTimeIntervalList;

        OpenClassroomAdapter(RoomTimeIntervalList roomTimeIntervalList) {
            mRoomTimeIntervalList = roomTimeIntervalList.clone().truncate(4);
        }

        @NonNull
        @Override
        public HomeFragment.ClassroomViewHolder onCreateViewHolder(@NonNull ViewGroup vg, int i) {
            // Use layout_schedule_item.xml as the layout for each individual recycler view item
            View view = LayoutInflater.from(vg.getContext())
                    .inflate(R.layout.list_item_home_classroom, vg, false);
            return new HomeFragment.ClassroomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeFragment.ClassroomViewHolder viewHolder, int i) {
            // Display the RoomTimeInterval at index i in the recycler view
            RoomTimeInterval roomTimeInterval = mRoomTimeIntervalList.get(i);

            // Get the building and room number of the room that is open
            String building = roomTimeInterval.getBuilding();
            String roomNum = roomTimeInterval.getRoomNum();
            String room = building + " " + roomNum;

            // Update the text of the item in the recycler view
            viewHolder.mRoomTextView.setText(room);
            viewHolder.mTimeIntervalTextView.setText(roomTimeInterval.formatTimeInterval());
        }

        @Override
        public int getItemCount() {
            return mRoomTimeIntervalList.size();
        }
    }

    /**
     * A custom RecyclerView ViewHolder for an item in the list of open classrooms.
     */
    class ClassroomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.home_classroom_room)
        TextView mRoomTextView;

        @BindView(R.id.home_classroom_time)
        TextView mTimeIntervalTextView;

        ClassroomViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
