package io.github.wztlei.wathub.ui.modules.openclassroom;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import io.github.wztlei.wathub.controller.BuildingManager;
import io.github.wztlei.wathub.controller.RoomScheduleManager;
import io.github.wztlei.wathub.model.RoomTimeInterval;
import io.github.wztlei.wathub.model.RoomTimeIntervalList;
import io.github.wztlei.wathub.ui.StringAdapter;
import io.github.wztlei.wathub.ui.modules.base.BaseModuleFragment;
import io.github.wztlei.wathub.utils.DateTimeUtils;

public class OpenClassroomFragment extends BaseModuleFragment {

    @BindView(R.id.building_open_classroom_spinner)
    Spinner mBuildingsSpinner;
    @BindView(R.id.hours_open_classroom_spinner)
    Spinner mHoursSpinner;
    @BindView(R.id.open_classroom_list)
    RecyclerView mOpenRoomList;
    @BindView(R.id.open_classroom_full_building_name)
    TextView mFullBuildingName;
    @BindView(R.id.open_classroom_no_results)
    TextView mNoResultsText;
    @BindView(R.id.loading_layout)
    ViewGroup mLoadingLayout;
    @BindView(R.id.open_classroom_layout)
    ViewGroup mOpenClassroomLayout;

    private RoomScheduleManager mRoomScheduleManager;
    private SharedPreferences mSharedPreferences;
    private Runnable mHoursDropdownUpdater;
    private Context mContext;
    private MenuItem mRefreshMenuItem;
    private Calendar mLastUpdateTime;

    @SuppressWarnings("unused")
    private static final String TAG = "OpenClassroomFragment";
    private static final int HOURS_UPDATE_PERIOD_MS = 10000;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public final View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {

        // Set up the view
        View root = inflater.inflate(R.layout.fragment_module, container, false);
        ViewGroup parent = root.findViewById(R.id.container_content_view);
        View contentView = inflater.inflate(R.layout.fragment_open_classrooms,
                parent, false);
        parent.addView(contentView);
        setHasOptionsMenu(true);

        // Initialize instance variables
        ButterKnife.bind(this, contentView);
        mRoomScheduleManager = RoomScheduleManager.getInstance();
        mOpenRoomList.setLayoutManager(new LinearLayoutManager(mContext));
        mSharedPreferences = mContext.getSharedPreferences(
                Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        // Set up the options for the buildings and hours spinners
        updateBuildingsSpinner();
        updateHoursSpinner();

        // Define a Runnable object that updates the hours spinner whenever the hour changes
        mHoursDropdownUpdater = () -> {
            int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            if (mLastUpdateTime.get(Calendar.HOUR_OF_DAY) != currentHour) {
                updateHoursSpinner();
            }

            new Handler().postDelayed(mHoursDropdownUpdater, HOURS_UPDATE_PERIOD_MS);
        };

        // Initial call to set the hours dropdown
        new Handler().post(mHoursDropdownUpdater);

        return root;
    }
    
    @Override
    public String getToolbarTitle() {
        return getString(R.string.title_open_classrooms);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        // Inflate the menu
        inflater.inflate(R.menu.menu_info_and_refresh, menu);
        mRefreshMenuItem = menu.findItem(R.id.menu_refresh);

        // Set listeners on the dropdown spinners and display the initial query results
        setSpinnerSelectionListeners();
        displayQueryResults(true);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_refresh) {
            // Refresh the screen and retrieve the latest schedules from GitHub
            displayLoadingScreen(mLoadingLayout, mRefreshMenuItem, false);
            mRoomScheduleManager.handleManualRefresh(getActivity());
            return true;
        } else if (menuItem.getItemId() == R.id.menu_info) {
            // Creates an alert dialog displaying important info about the open classroom data
            new AlertDialog.Builder(mContext)
                    .setTitle(getString(R.string.open_classroom_dialog_title))
                    .setMessage(getString(R.string.open_classroom_dialog_message))
                    .setPositiveButton(android.R.string.ok, (dialog1, which) -> {})
                    .create()
                    .show();
            return true;
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
    }

    /**
     * Updates the dropdown options of the buildings spinner to include all building options.
     */
    private void updateBuildingsSpinner() {
        // Get the options for the buildings dropdown
        String[] buildings = mRoomScheduleManager.getBuildings();
        StringAdapter buildingsAdapter = new StringAdapter(mContext, buildings);
        buildingsAdapter.setViewLayoutId(android.R.layout.simple_spinner_item);
        mBuildingsSpinner.setAdapter(buildingsAdapter);

        // Remember the last building selected
        String lastBuildingQueried = mSharedPreferences.getString(Constants.BUILDING_KEY, "");
        int indexLastBuildingQueried = Arrays.asList(buildings).indexOf(lastBuildingQueried);

        // Set the spinner to the selection before the update
        if (indexLastBuildingQueried != -1) {
            mBuildingsSpinner.setSelection(indexLastBuildingQueried);
        }
    }

    /**
     * Updates the dropdown options of the hours spinner to include "Now" in place of
     * the current hour.
     */
    private void updateHoursSpinner() {
        // Record the hour when the spinner last updated
        mLastUpdateTime = Calendar.getInstance();

        // Get the options for the hours dropdown
        String[] hours = getHourDropdownOptionList(mLastUpdateTime.get(Calendar.HOUR_OF_DAY));
        StringAdapter hoursAdapter = new StringAdapter(mContext, hours);
        int spinnerSelectionIndex = mHoursSpinner.getSelectedItemPosition();
        hoursAdapter.setViewLayoutId(android.R.layout.simple_spinner_item);

        // Set the new dropdown options
        mHoursSpinner.setAdapter(hoursAdapter);

        // Set the spinner to the selection before the update
        if (spinnerSelectionIndex != AdapterView.INVALID_POSITION) {
            mHoursSpinner.setSelection(spinnerSelectionIndex);
        }
    }

    /**
     * Sets OnItemSelectedListeners on the two spinners to display the new building and hour query
     * results whenever a new dropdown option is selected.
     */
    private void setSpinnerSelectionListeners() {
        mBuildingsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                displayQueryResults(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        mHoursSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                displayQueryResults(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Updates the UI to display the open classroom schedule based on the building and hour 
     * that the user selected from the dropdowns.
     */
    private void displayQueryResults(boolean initialDisplay) {
        // Display the loading screen to provide feedback to the user
        displayLoadingScreen(mLoadingLayout, mRefreshMenuItem, initialDisplay);

        // Determine if a building is actually selected
        if (mBuildingsSpinner.getSelectedItem() == null) {
            return;
        }

        // Get the building and index of the selected hour option
        String building = mBuildingsSpinner.getSelectedItem().toString();
        int hourIndex = mHoursSpinner.getSelectedItemPosition();

        // Retrieve a schedule of the open classrooms for the query from roomScheduleManager
        Calendar searchDate = (Calendar) mLastUpdateTime.clone();
        searchDate.add(Calendar.HOUR_OF_DAY, hourIndex);

        RoomTimeIntervalList buildingOpenSchedule =
                mRoomScheduleManager.findOpenRooms(building, searchDate);

        // Check if any open classrooms has been found
        if (buildingOpenSchedule.size() > 0) {
            // Update the visibility of the views
            mOpenRoomList.setVisibility(View.VISIBLE);
            mFullBuildingName.setVisibility(View.VISIBLE);
            mNoResultsText.setVisibility(View.GONE);

            // Update the recycler view displaying the open classroom schedule
            mOpenRoomList.setAdapter(new OpenClassroomAdapter(buildingOpenSchedule));

            // Update the text view displaying the building's full name
            mFullBuildingName.setText(BuildingManager.getInstance().getBuildingFullName(building));
        } else {
            // Update the visibility of the views
            mOpenRoomList.setVisibility(View.GONE);
            mFullBuildingName.setVisibility(View.GONE);
            mNoResultsText.setVisibility(View.VISIBLE);
        }

        // Store the latest building of the latest query in shared preferences for later recall
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.BUILDING_KEY, building);
        editor.apply();
    }

    /**
     * Returns a list of hours as formatted strings to be displayed as options for the hours
     * dropdown and stores the hours in 24h format as integers for use in queries.
     *
     * @return a list of strings which are the hour dropdown selection options
     */
    private String[] getHourDropdownOptionList(int currentHour) {
        // Initialize a list to store the formatted times and determine the current hour of the day
        String[] timeStringOptions = new String[24];

        // Add all the hours from 1h in the future to 11PM as possible options
        for (int h = currentHour; h <= currentHour + 23; h++) {
            if (h == currentHour) {
                timeStringOptions[h - currentHour] = "Now";
            } else if (h <= 23){
                timeStringOptions[h - currentHour] =
                        String.format("%s, Today", DateTimeUtils.format12hTime(h));
            } else {
                timeStringOptions[h - currentHour] =
                        String.format("%s, Tomorrow", DateTimeUtils.format12hTime(h % 24));
            }
        }

        return timeStringOptions;
    }


    /**
     * A custom RecyclerView Adapter for the list of open classrooms.
     */
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

            // Update the text of the item in the recycler view
            viewHolder.mRoomTextView.setText(roomTimeInterval.formatRoom());
            viewHolder.mTimeIntervalTextView.setText(roomTimeInterval.formatTimeInterval());
            viewHolder.mDateTextView.setText(roomTimeInterval.formatMonthAndDate());
        }

        @Override
        public int getItemCount() {
            return mRoomTimeIntervalList.size();
        }
    }

    /**
     * A custom RecyclerView ViewHolder for an item in the list of open classrooms.
     */
    class OpenClassroomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.room_text_view)
        TextView mRoomTextView;

        @BindView(R.id.time_interval_text_view)
        TextView mTimeIntervalTextView;
        
        @BindView(R.id.date_text_view)
        TextView mDateTextView;

        OpenClassroomViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
