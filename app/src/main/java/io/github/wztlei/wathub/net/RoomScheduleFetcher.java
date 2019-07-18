package io.github.wztlei.wathub.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.deange.uwaterlooapi.UWaterlooApi;
import com.deange.uwaterlooapi.model.common.Responses;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RoomScheduleFetcher {

    private String[] mSubjects;
    private String[] mBuildings;
    private SharedPreferences mSharedPreferences;
    private JSONObject mDaysOfWeekMap;
    private int mCurrentTerm;

    private static final String SUBJECT_SCHEDULE_PROGRESS_KEY = "SUBJECT_SCHEDULE_PROGRESS_KEY";
    private static final String ROOM_SCHEDULES_GITHUB_URL =
            "https://raw.githubusercontent.com/wztlei/wathub/master/app/src/main/res/raw/room_schedule.json";
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

        // Get the json mapping of a days of week string to a JSON array of booleans
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.days_of_week);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            // noinspection ResultOfMethodCallIgnored
            inputStream.read(buffer);
            inputStream.close();
            mDaysOfWeekMap = new JSONObject(new String(buffer));

            // Use the room schedule from res/raw if shared preferences is missing room schedules
            if (!mSharedPreferences.contains(Constants.ROOM_SCHEDULE_KEY)) {
                inputStream = context.getResources().openRawResource(R.raw.days_of_week);
                size = inputStream.available();
                buffer = new byte[size];
                // noinspection ResultOfMethodCallIgnored
                inputStream.read(buffer);
                inputStream.close();

                // Update the room schedule with the data from res/raw
                updateRoomSchedule(new JSONObject(new String(buffer)).toString());
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            mDaysOfWeekMap = new JSONObject();
        }
    }


    /**
     * Returns a list of buildings that contain classrooms.
     *
     * @return a list of buildings that contain classrooms
     */
    public String[] getBuildings() {
        return mBuildings;
    }

    /**
     * Retrieves the room schedules from a JSON file hosted on GitHub and then by using the
     * UWaterloo Open Data API to get the schedule for each course.
     */
    public void retrieveRoomScheduleAsync() {
        try {
            // Create a request using the OkHttpClient library
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url(ROOM_SCHEDULES_GITHUB_URL).build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) {
                    try {
                        // Update the room schedules with a JSON string from the response body
                        // noinspection ConstantConditions
                        String jsonString = response.body().string();
                        updateRoomSchedule(jsonString);
                        Log.d(TAG, "Updated room schedules from GitHub");
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        // Use the UWaterloo API to get room schedules
                        retrieveSchedulesWithApi();
                    }
                }

                @Override
                public void onFailure(@NonNull final Call call, @NonNull IOException e) {
                    // Use the UWaterloo API to get room schedules
                    retrieveSchedulesWithApi();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the room schedules using the UWaterloo Open Data API on a separate thread.
     */
    private void retrieveSchedulesWithApi() {
        new Thread(this::handleRetrieveSchedulesWithApi).start();
    }

    /**
     * Handles retrieving the room schedules, but this will take over a minute and block the thread.
     */
    private void handleRetrieveSchedulesWithApi() {
        // Get the arguments passed into the class
        UWaterlooApi api = new UWaterlooApi(Constants.UWATERLOO_API_KEY);

        // Initialize variables
        RoomSchedule roomSchedule = new RoomSchedule();
        mCurrentTerm = Calls.unwrap(api.Terms.getTermList()).getData().getCurrentTerm();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        int subjectProgressIndex = mSharedPreferences.getInt(SUBJECT_SCHEDULE_PROGRESS_KEY, 0)
                % mSubjects.length;

        // Iterate through every subject (ex. MATH, ECON, ENGL, ...) resuming from where we left off
        for (int i = subjectProgressIndex; i < mSubjects.length; i++) {
            String subject = mSubjects[i];

            Log.d(TAG, "Retrieving the schedule for subject #" + i + " - " + subject);

            // Get the schedule for the subject using a thread-blocking API call
            Responses.CoursesSchedule response = Calls.unwrap(api.Terms.getSchedule(mCurrentTerm, subject));
            List<CourseSchedule> subjectSchedule;
            if (response != null) {
                subjectSchedule = response.getData();
            } else {
                i--;
                continue;
            }

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
            editor.putInt(SUBJECT_SCHEDULE_PROGRESS_KEY, i + 1);
            editor.apply();
        }

        // Put the room schedule in shared preferences
        updateRoomSchedule(roomSchedule.toString());
    }

    /**
     * Updates the state of the class upon receiving a JSON string storing the room schedule.
     *
     * @param jsonString        a JSON string storing the room schedule data
     */
    private void updateRoomSchedule(String jsonString) {
        // Put the json string in shared preferences
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.ROOM_SCHEDULE_KEY, jsonString);
        editor.apply();

        // Get a list of buildings from the JSON object
        try {
            JSONObject roomSchedule = new JSONObject(jsonString);
            JSONArray buildingNames = roomSchedule.names();
            mBuildings = new String[buildingNames.length()];

            // Store all of the building names in a list
            for (int i = 0; i < buildingNames.length(); i++) {
                mBuildings[i] = buildingNames.getString(i);
            }
        } catch (JSONException e) {
            Log.w(TAG, e.getMessage());   
        }
    }


    /**
     * A JSONObject representation of the room schedule with an additional method for updating
     * itself with new data about a class.
     */
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
