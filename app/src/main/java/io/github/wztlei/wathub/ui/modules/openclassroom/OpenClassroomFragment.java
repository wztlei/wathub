package io.github.wztlei.wathub.ui.modules.openclassroom;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

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

public class OpenClassroomFragment extends BaseModuleFragment {

    @BindView(R.id.building_open_classroom_spinner)
    Spinner mBuildingSpinner;

    private RoomScheduleManager mRoomScheduleManager;
    private SharedPreferences mSharedPreferences;

    private static final String TAG = "OpenClassroomFragment";
    private static final String BUILDING_KEY = "BUILDING_KEY";

    @Override
    public final View onCreateView(
            final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_module, container, false);
        final ViewGroup parent = root.findViewById(R.id.container_content_view);
        final View contentView = inflater.inflate(R.layout.fragment_open_classrooms, parent, false);

        ButterKnife.bind(this, contentView);
        mRoomScheduleManager = RoomScheduleManager.getInstance();

        if (contentView.getParent() == null) {
            parent.addView(contentView);
        }

        // Use an adapter to display all potentially available buildings
        StringAdapter adapter = new StringAdapter(getContext(), mRoomScheduleManager.getBuildings());
        adapter.setViewLayoutId(android.R.layout.simple_spinner_item);
        mBuildingSpinner.setAdapter(adapter);
        mSharedPreferences = getContext().getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        setListeners();

        return root;
    }
    
    @Override
    public String getToolbarTitle() {
        return "Open Classrooms";
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
    }
    /**
     * Updates the UI to display the open classroom schedule based on the building and hour 
     * that the user selected from the dropdowns.
     */
    private void displayNewQueryResults() {
        // Get the building and index of the selected hour option
        String building = mBuildingSpinner.getSelectedItem().toString();
        //int hourIndex = hourDropdown.getSelectedItemPosition();
        int hour = Calendar.getInstance().get(Calendar.HOUR);
        // Retrieve a schedule of the open classrooms for the query from roomScheduleManager
        RoomTimeIntervalList buildingOpenSchedule = mRoomScheduleManager.findOpenRooms(building,
                hour, hour+1);

        for (RoomTimeInterval r : buildingOpenSchedule) {
            Log.d(TAG, "r: " + r.getBuilding() + " " + r.getRoomNum() + " r");
        }

        // Update the recycler view displaying the open classroom schedule 
//        scheduleRecyclerView.setAdapter(new ScheduleAdapter(buildingOpenSchedule));

        // Update the text view displaying the building's full name
//        buildingFullNameTextView.setText(buildingCodeToFullName(building));

        // Store the latest building of the latest query in shared preferences for later recall
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(BUILDING_KEY, building);
        editor.apply();
    }
}
