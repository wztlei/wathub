package io.github.wztlei.wathub.ui.modules.openclassroom;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

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

    @BindView(R.id.open_classroom_list)
    RecyclerView mOpenRoomList;

    private RoomScheduleManager mRoomScheduleManager;
    private SharedPreferences mSharedPreferences;

    @SuppressWarnings("unused")
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
        mOpenRoomList.setLayoutManager(new LinearLayoutManager(getContext()));

        parent.addView(contentView);

        // Use an adapter to display all potentially available buildings
        StringAdapter adapter = new StringAdapter(getContext(), mRoomScheduleManager.getBuildings());
        adapter.setViewLayoutId(android.R.layout.simple_spinner_item);
        mBuildingSpinner.setAdapter(adapter);
        mSharedPreferences = getContext().getSharedPreferences(
                Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        setListeners();
        displayNewQueryResults();
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
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        // Retrieve a schedule of the open classrooms for the query from roomScheduleManager
        RoomTimeIntervalList buildingOpenSchedule = mRoomScheduleManager.findOpenRooms(building,
                hour, hour+1);

        // Update the recycler view displaying the open classroom schedule 
        mOpenRoomList.setAdapter(new OpenClassroomAdapter(buildingOpenSchedule));

        Log.d(TAG, "displayNewQueryResults");

        // Update the text view displaying the building's full name
//        buildingFullNameTextView.setText(buildingCodeToFullName(building));

        // Store the latest building of the latest query in shared preferences for later recall
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(BUILDING_KEY, building);
        editor.apply();
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
            Log.d(TAG, "getItemCount" + mRoomTimeIntervalList.size());
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
