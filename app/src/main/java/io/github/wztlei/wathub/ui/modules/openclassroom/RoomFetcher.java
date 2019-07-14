package io.github.wztlei.wathub.ui.modules.openclassroom;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.deange.uwaterlooapi.UWaterlooApi;
import com.deange.uwaterlooapi.model.courses.Class;
import com.deange.uwaterlooapi.model.courses.ClassDate;
import com.deange.uwaterlooapi.model.courses.CourseSchedule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.net.Calls;

public class RoomFetcher{

    private String[] mSubjects;
    private SharedPreferences mSharedPreferences;
    private JSONObject mDaysOfWeekMap;
    private int mCurrentTerm;
    private static final String TAG = "WL/RoomFetcher";

    public RoomFetcher (Context context, String[] subjects) {
        mSubjects = subjects;
        mSharedPreferences = context.getSharedPreferences(
                Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        InputStream inputStream = context.getResources().openRawResource(R.raw.days_of_week);

        try {
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            inputStream.read(buffer);
            inputStream.close();
            mDaysOfWeekMap = new JSONObject(new String(buffer));
        } catch (IOException | JSONException e) {
            mDaysOfWeekMap = new JSONObject();
            e.printStackTrace();
        }

        Log.d(TAG, mDaysOfWeekMap.toString());
    }

    public void retrieveSchedules() {
        Thread thread = new Thread(this::handleRetrieveSchedules);
        thread.start();
    }

    private void handleRetrieveSchedules() {
        // Get the arguments passed into the class
        UWaterlooApi api = new UWaterlooApi(Constants.UWATERLOO_API_KEY);

        // Initialize variables
        RoomSchedule roomSchedule = new RoomSchedule();
        mCurrentTerm = Calls.unwrap(api.Terms.getTermList()).getData().getCurrentTerm();
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        // Iterate through every subject (ex. MATH, ECON, ENGL, ...)
        for (String subject : mSubjects) {
            Log.d(TAG, "Retrieving the " + subject + " schedule");

            List<CourseSchedule> subjectSchedule =
                     Calls.unwrap(api.Terms.getSchedule(mCurrentTerm, subject)).getData();

            // Iterate through every course schedule in that subject (ex. CS 135 LEC 003, ...)
            for (CourseSchedule courseSchedule : subjectSchedule) {
                // Iterate through every class in that course section
                for (Class classInfo : courseSchedule.getClasses()) {
                    try {
                        roomSchedule.update(classInfo);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }

        editor.putString(Constants.ROOMS_KEY, roomSchedule.toString());
        editor.apply();
    }


    private class RoomSchedule extends JSONObject {
        void update(Class classInfo) throws JSONException, NullPointerException {
            if (classInfo == null) {
                return;
            }

            String building = classInfo.getBuilding();

            // Add a new building if necessary
            if (building == null) {
               return;
            } else if (!this.has(building)) {
                this.put(building, new JSONObject());
            }

            // Get the JSON object for the building
            JSONObject buildingRooms = this.getJSONObject(building);
            String room = classInfo.getRoom();

            // Add a new room if necessary
            if (room == null) {
              return;
            } else if (!buildingRooms.has(room)) {
                buildingRooms.put(room, new JSONArray());
            }

            // Get the JSON array for the room
            JSONArray classTimes = buildingRooms.getJSONArray(room);
            ClassDate classTime = classInfo.getDate();

            if (classTime == null || classTime.isCancelled()
                    || classTime.isClosed() || classTime.isTBA()) {
                return;
            }

            String startTime = classTime.getStartTime();
            String endTime = classTime.getEndTime();
            String startDateString = classTime.getStartDate();
            String endDateString = classTime.getEndDate();
            String weekdaysString = classTime.getWeekdays();
            JSONArray newClassTime = new JSONArray();

            newClassTime.put( getChars0And1(startTime, 8) );
            newClassTime.put( getChars3And4(startTime, 30) );
            newClassTime.put( getChars0And1(endTime, 10) );
            newClassTime.put( getChars3And4(endTime, 0) );
            newClassTime.put( mDaysOfWeekMap.getJSONArray(weekdaysString) );
            newClassTime.put( getChars0And1(startDateString, termStartMonth(mCurrentTerm)) );
            newClassTime.put( getChars3And4(startDateString, termStartDate()) );
            newClassTime.put( getChars0And1(endDateString, termEndMonth(mCurrentTerm)) );
            newClassTime.put( getChars3And4(endDateString, termEndDate()) );

            classTimes.put(newClassTime);
        }

        private int getChars0And1(String time, int defaultValue) {
            if (time == null || time.length() < 5) {
                return defaultValue;
            }

            return Integer.parseInt(time.substring(0, 2));
        }

        private int getChars3And4(String time, int defaultValue) {
            if (time == null || time.length() < 5) {
                return defaultValue;
            }

            return Integer.parseInt(time.substring(3, 5));
        }

        private int termStartMonth(int termNumber) {
            return termNumber % 10;
        }

        private int termStartDate() {
            return 1;
        }

        private int termEndMonth(int termNumber) {
            return (termNumber % 10) + 3;
        }

        private int termEndDate() {
            return 6;
        }
    }
}
