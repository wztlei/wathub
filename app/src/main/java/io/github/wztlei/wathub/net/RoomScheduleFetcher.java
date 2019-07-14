package io.github.wztlei.wathub.net;

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
import java.util.List;

import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.R;

public class RoomScheduleFetcher {

    private String[] mSubjects;
    private SharedPreferences mSharedPreferences;
    private JSONObject mDaysOfWeekMap;
    private int mCurrentTerm;
    private static final String TAG = "WL/RoomScheduleFetcher";

    /**
     * Constructor for a RoomScheduleFetcher object.
     *
     * @param context the context in which the Room Fetcher is created
     */
    public RoomScheduleFetcher(Context context) {
        mSubjects = context.getResources().getStringArray(R.array.course_subjects);
        mSharedPreferences = context.getSharedPreferences(
                Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        InputStream inputStream = context.getResources().openRawResource(R.raw.days_of_week);

        // Get the json mapping of a days of week string to a JSON array of booleans
        try {
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            // noinspection ResultOfMethodCallIgnored
            inputStream.read(buffer);
            inputStream.close();
            mDaysOfWeekMap = new JSONObject(new String(buffer));
        } catch (IOException | JSONException e) {
            mDaysOfWeekMap = new JSONObject();
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the room schedules on a separate thread.
     */
    public void retrieveSchedules() {
        Thread thread = new Thread(this::handleRetrieveSchedules);
        thread.start();
    }

    /**
     * Handles retrieving the room schedules, but this will take over a minute and block the thread.
     */
    private void handleRetrieveSchedules() {
        // Get the arguments passed into the class
        UWaterlooApi api = new UWaterlooApi(Constants.UWATERLOO_API_KEY);

        // Initialize variables
        RoomSchedule roomSchedule = new RoomSchedule();
        mCurrentTerm = Calls.unwrap(api.Terms.getTermList()).getData().getCurrentTerm();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        int subjectProgressIndex = mSharedPreferences.getInt(Constants.SUBJECT_PROGRESS_KEY, 0)
                % mSubjects.length;

        // Iterate through every subject (ex. MATH, ECON, ENGL, ...) resuming from where we left off
        for (int i = subjectProgressIndex; i < mSubjects.length; i++) {
            String subject = mSubjects[i];

            Log.d(TAG, "Retrieving the schedule for subject #" + i + " - " + subject);

            // Get the schedule for the subject using a thread-blocking API call
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

            // Update the progress
            editor.putInt(Constants.SUBJECT_PROGRESS_KEY, i + 1);
            editor.apply();
        }

        editor.putString(Constants.ROOMS_KEY, roomSchedule.toString());
        editor.apply();
    }


    private class RoomSchedule extends JSONObject {
        void update(Class classInfo) throws JSONException, NullPointerException {
            if (classInfo == null) {
                return;
            }

            // Get the building
            String building = classInfo.getBuilding();

            // Add a new building if necessary
            if (building == null) {
               return;
            } else if (!this.has(building)) {
                this.put(building, new JSONObject());
            }

            // Get the room number and the JSON object for the building
            String room = classInfo.getRoom();
            JSONObject buildingRooms = this.getJSONObject(building);

            // Add a new room if necessary
            if (room == null) {
              return;
            } else if (!buildingRooms.has(room)) {
                buildingRooms.put(room, new JSONArray());
            }

            // Get the JSON array for the room
            JSONArray classTimes = buildingRooms.getJSONArray(room);
            ClassDate classTime = classInfo.getDate();

            // Determine if a class exists
            if (classTime == null || classTime.isCancelled()
                    || classTime.isClosed() || classTime.isTBA()) {
                return;
            }

            // Get the data from class time
            String startTime = classTime.getStartTime();
            String endTime = classTime.getEndTime();
            String startDateString = classTime.getStartDate();
            String endDateString = classTime.getEndDate();
            String weekdaysString = classTime.getWeekdays();

            // Create a new JSON array for the class time
            JSONArray newClassTime = new JSONArray();

            // Add the data by parsing the various strings
            newClassTime.put( getChars0And1(startTime, 8) );
            newClassTime.put( getChars3And4(startTime, 30) );
            newClassTime.put( getChars0And1(endTime, 10) );
            newClassTime.put( getChars3And4(endTime, 0) );
            newClassTime.put( mDaysOfWeekMap.getJSONArray(weekdaysString) );
            newClassTime.put( getChars0And1(startDateString, termStartMonth()) );
            newClassTime.put( getChars3And4(startDateString, termStartDate()) );
            newClassTime.put( getChars0And1(endDateString, termEndMonth()) );
            newClassTime.put( getChars3And4(endDateString, termEndDate()) );

            // Add the new class time to the list of existing class times
            classTimes.put(newClassTime);
        }

        /**
         * Returns the integer formed by the first and second chars in a string or
         * a default value if no integer is formed.
         *
         * @param string        the string to be parsed
         * @param defaultValue  the default value if the string is not formatted properly
         * @return              the integer formed by the first and second chars
         */
        private int getChars0And1(String string, int defaultValue) {
            try {
                return Integer.parseInt(string.substring(0, 2));
            } catch (Exception e) {
                return defaultValue;
            }
        }

        /**
         * Returns the integer formed by the fourth and fifth chars in a string or
         * a default value if no integer is formed.
         *
         * @param string        the string to be parsed
         * @param defaultValue  the default value if the string is not formatted properly
         * @return              the integer formed by the fourth and fifth chars
         */
        private int getChars3And4(String string, int defaultValue) {
            try {
                return Integer.parseInt(string.substring(3, 5));
            } catch (Exception e) {
                return defaultValue;
            }
        }

        /**
         * Returns the estimated start month for the term.
         *
         * @return the estimated start month for the term
         */
        private int termStartMonth() {
            return mCurrentTerm % 10;
        }

        /**
         * Returns the estimated start date for the term.
         *
         * @return the estimated start date for the term
         */
        private int termStartDate() {
            return 1;
        }

        /**
         * Returns the estimated ending month for the term.
         *
         * @return the estimated end month for the term
         */
        private int termEndMonth() {
            return (mCurrentTerm % 10) + 3;
        }

        /**
         * Returns the estimated end date for the term.
         *
         * @return the estimated end date for the term
         */
        private int termEndDate() {
            return 6;
        }
    }
}
