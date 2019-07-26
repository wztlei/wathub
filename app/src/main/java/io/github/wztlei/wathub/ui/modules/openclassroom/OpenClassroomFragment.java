package io.github.wztlei.wathub.ui.modules.openclassroom;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.controller.RoomScheduleManager;
import io.github.wztlei.wathub.model.RoomTimeInterval;
import io.github.wztlei.wathub.model.RoomTimeIntervalList;
import io.github.wztlei.wathub.ui.StringAdapter;
import io.github.wztlei.wathub.ui.modules.base.BaseModuleFragment;
import io.github.wztlei.wathub.utils.DateUtils;

public class OpenClassroomFragment extends BaseModuleFragment {

    @BindView(R.id.building_open_classroom_spinner)
    Spinner mBuildingSpinner;
    @BindView(R.id.hours_open_classroom_spinner)
    Spinner mHoursSpinner;
    @BindView(R.id.open_classroom_list)
    RecyclerView mOpenRoomList;
    @BindView(R.id.open_classroom_full_building_name)
    TextView mFullBuildingName;
    @BindView(R.id.open_classroom_no_results)
    TextView mNoResultsText;

    private RoomScheduleManager mRoomScheduleManager;
    private SharedPreferences mSharedPreferences;

    @SuppressWarnings("unused")
    private static final String TAG = "OpenClassroomFragment";
    private static final String BUILDING_KEY = "BUILDING_KEY";

    // TODO WL: Add functionality to the refresh icon to fetch the latest schedules from GitHub
    // TODO WL: Update the hour dropdown every 10s to keep the 'Now' option up-to-date
    // TODO WL: Update the TextView at the bottom of the page to display a building's full name
    // TODO WL: Display a loading animation whenever a new building or time is selected
    // TODO WL: Ask others for their opinions on this feature

    @Override
    public final View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {

        // Set up the view
        View root = inflater.inflate(R.layout.fragment_module, container, false);
        ViewGroup parent = root.findViewById(R.id.container_content_view);
        View contentView = inflater.inflate(R.layout.fragment_open_classrooms, parent, false);
        parent.addView(contentView);
        setHasOptionsMenu(true);

        // Initialize instance variables
        ButterKnife.bind(this, contentView);
        mRoomScheduleManager = RoomScheduleManager.getInstance();
        mOpenRoomList.setLayoutManager(new LinearLayoutManager(getContext()));
        mSharedPreferences = getContext().getSharedPreferences(
                Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        // Use a StringAdapter to display all potentially available buildings
        String[] buildings = mRoomScheduleManager.getBuildings();
        StringAdapter buildingsAdapter = new StringAdapter(getContext(), buildings);
        buildingsAdapter.setViewLayoutId(android.R.layout.simple_spinner_item);
        mBuildingSpinner.setAdapter(buildingsAdapter);

        // Use a StringAdapter to display all hours
        String[] hours = getHourDropdownOptionList();
        StringAdapter hoursAdapter = new StringAdapter(getContext(), hours);
        hoursAdapter.setViewLayoutId(android.R.layout.simple_spinner_item);
        mHoursSpinner.setAdapter(hoursAdapter);

        // Remember the last building selected
        String lastBuildingQueried = mSharedPreferences.getString(BUILDING_KEY, "");
        int indexLastBuildingQueried = Arrays.asList(buildings).indexOf(lastBuildingQueried);

        if (indexLastBuildingQueried != -1) {
            mBuildingSpinner.setSelection(indexLastBuildingQueried);
        }

        // Select the current hour
        mHoursSpinner.setSelection(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));

        setListeners();
        displayNewQueryResults();
        return root;
    }
    
    @Override
    public String getToolbarTitle() {
        return getString(R.string.title_open_classrooms);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_info_and_refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {

            return true;
        } else if (item.getItemId() == R.id.menu_info) {
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.open_classroom_dialog_title))
                    .setMessage(getString(R.string.open_classroom_dialog_message))
                    .setPositiveButton(android.R.string.ok, (dialog1, which) -> {})
                    .create()
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setListeners() {
        mBuildingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                displayNewQueryResults();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        mHoursSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                displayNewQueryResults();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    /**
     * Updates the UI to display the open classroom schedule based on the building and hour 
     * that the user selected from the dropdowns.
     */
    private void displayNewQueryResults() {
        if (mBuildingSpinner.getSelectedItem() == null) {
            return;
        }

        // Get the building and index of the selected hour option
        String building = mBuildingSpinner.getSelectedItem().toString();
        int hourIndex = mHoursSpinner.getSelectedItemPosition();

        // Retrieve a schedule of the open classrooms for the query from roomScheduleManager
        RoomTimeIntervalList buildingOpenSchedule =
                mRoomScheduleManager.findOpenRooms(building, hourIndex);

        // Check if any open classrooms has been found
        if (buildingOpenSchedule.size() > 0) {
            // Update the visibility of the views
            mOpenRoomList.setVisibility(View.VISIBLE);
            mFullBuildingName.setVisibility(View.VISIBLE);
            mNoResultsText.setVisibility(View.GONE);

            // Update the recycler view displaying the open classroom schedule
            mOpenRoomList.setAdapter(new OpenClassroomAdapter(buildingOpenSchedule));

            // Update the text view displaying the building's full name
            mFullBuildingName.setText("Douglas Wright Engineering Building Douglas Wright Engineering Building");
        } else {
            // Update the visibility of the views
            mOpenRoomList.setVisibility(View.GONE);
            mFullBuildingName.setVisibility(View.GONE);
            mNoResultsText.setVisibility(View.VISIBLE);
        }

        // Store the latest building of the latest query in shared preferences for later recall
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(BUILDING_KEY, building);
        editor.apply();
    }

    /**
     * Returns a list of hours as formatted strings to be displayed as options for the hours
     * dropdown and stores the hours in 24h format as integers for use in queries.
     *
     * @return a list of strings which are the hour dropdown selection options
     */
    private String[] getHourDropdownOptionList() {
        // Initialize a list to store the formatted times and determine the current hour of the day
        String[] timeStringOptions = new String[24];
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        // Add all the hours from 1h in the future to 11PM as possible options
        for (int h = 0; h <= 23; h++) {
            if (h == currentHour) {
                timeStringOptions[h] = "Now";
            } else {
                timeStringOptions[h] = DateUtils.format12hTime(h, 0);
            }
        }

        return timeStringOptions;
    }

    class OpenClassroomAdapter extends RecyclerView.Adapter<OpenClassroomViewHolder> {
        private RoomTimeIntervalList mRoomTimeIntervalList;

        OpenClassroomAdapter(RoomTimeIntervalList roomTimeIntervalList) {
            mRoomTimeIntervalList = roomTimeIntervalList;
        }

        @NonNull
        @Override
        public OpenClassroomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            // Use layout_schedule_item.xml as the layout for each individual recycler view item
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.list_item_open_classroom, viewGroup, false);
            return new OpenClassroomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OpenClassroomViewHolder viewHolder, int i) {
            // Display the RoomTimeInterval at index i in the recycler view
            RoomTimeInterval roomTimeInterval = mRoomTimeIntervalList.get(i);

            // Get the building and room number of the room that is open
            String building = roomTimeInterval.getBuilding();
            String roomNum = roomTimeInterval.getRoomNum();
            String room = building + " " + roomNum;

            // Get the starting and ending times for when the room is open
            int startHour = roomTimeInterval.getStartHour();
            int startMin = roomTimeInterval.getStartMin();
            int endHour = roomTimeInterval.getEndHour();
            int endMin = roomTimeInterval.getEndMin();

            // Create a string to store the formatted time interval
            String timeInterval = DateUtils.format12hTime(startHour, startMin) + " - "
                    + DateUtils.format12hTime(endHour, endMin);

            // Update the text of the item in the recycler view
            viewHolder.roomTextView.setText(room);
            viewHolder.timeIntervalTextView.setText(timeInterval);
        }

        @Override
        public int getItemCount() {
            return mRoomTimeIntervalList.size();
        }
    }

    class OpenClassroomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.room_text_view)
        TextView roomTextView;

        @BindView(R.id.time_interval_text_view)
        TextView timeIntervalTextView;

        OpenClassroomViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
