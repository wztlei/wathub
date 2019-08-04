package io.github.wztlei.wathub.controller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import io.github.wztlei.wathub.ApiKeys;
import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.R;
import io.github.wztlei.wathub.model.RoomTimeInterval;
import io.github.wztlei.wathub.model.RoomTimeIntervalList;
import io.github.wztlei.wathub.net.Calls;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RoomScheduleManager {

    private static RoomScheduleManager sInstance;
    private static TermManager sTermManager;
    private static SharedPreferences sSharedPreferences;
    private static RoomSchedule sRoomSchedule;
    private static JSONObject sDaysOfWeekMap;
    private static Thread sApiRetrievalThread;
    private static String[] sSubjects;
    private static String[] sBuildings;
    private static int sCurrentMonth;
    private static int sCurrentDate;
    private static int sCurrentDayOfWeek;
    private static int sCurrentHour;
    private static int sCurrentMin;
    private static int sNumNetworkFailures;

    private static final String PREFS_SOURCE = "PREFS_SOURCE";
    private static final String RES_SOURCE = "RES_SOURCE";
    private static final String GITHUB_SOURCE = "GITHUB_SOURCE";
    private static final String UNKNOWN_SOURCE = "UNKNOWN_SOURCE";
    private static final String API_SOURCE = "API_SOURCE";

    private static final String ROOM_SCHEDULES_GITHUB_URL =
            "https://raw.githubusercontent.com/wztlei/wathub/master/app/src/main/res/raw/room_schedule.json";
    private static final String TAG = "WL/RoomScheduleManager";
    private static final int REFRESH_WAIT_MS = 10000;
    private static final int DEFAULT_MAX_NETWORK_FAILURES = 3;
    private static final int START_HOUR_INDEX = 0;
    private static final int START_MIN_INDEX = 1;
    private static final int END_HOUR_INDEX = 2;
    private static final int END_MIN_INDEX = 3;
    private static final int DAY_OF_WEEK_INDEX = 4;
    private static final int START_MONTH_INDEX = 5;
    private static final int START_DATE_INDEX = 6;
    private static final int END_MONTH_INDEX = 7;
    private static final int END_DATE_INDEX = 8;
    private static final int HALF_HOURS_PER_DAY = 48;

    /**
     * Initializes the static instance of a RoomScheduleManager.
     *
     * @param context the context in which the RoomScheduleManager is initialized
     */
    public static void init(Context context) {
        if (sInstance != null) {
            throw new IllegalStateException("RoomScheduleManager already instantiated!");
        }
        sInstance = new RoomScheduleManager(context);
    }

    /**
     * Returns the static instance of a RoomScheduleManager.
     *
     * @return the static instance of a RoomScheduleManager.
     */
    public static RoomScheduleManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("RoomScheduleManager not instantiated!");
        }
        return sInstance;
    }

    /**
     * Constructor for a RoomScheduleManager object.
     *
     * @param context the context in which the RoomScheduleManager is created
     */
    private RoomScheduleManager(Context context) {
        // Initialize static variables
        sSubjects = context.getResources().getStringArray(R.array.course_subjects);
        sSharedPreferences = context.getSharedPreferences(
                Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        sTermManager = TermManager.getInstance();
        sApiRetrievalThread = null;

        // Get the json mapping of a days of week string to a JSON array of booleans
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.days_of_week);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            // noinspection ResultOfMethodCallIgnored
            inputStream.read(buffer);
            inputStream.close();
            sDaysOfWeekMap = new JSONObject(new String(buffer));

            // Use the room schedule from res/raw if shared preferences is missing room schedules
            if (!sSharedPreferences.contains(Constants.ROOM_SCHEDULE_KEY)) {
                inputStream = context.getResources().openRawResource(R.raw.room_schedule);
                size = inputStream.available();
                buffer = new byte[size];
                // noinspection ResultOfMethodCallIgnored
                inputStream.read(buffer);
                inputStream.close();

                // Update the room schedule with the data from res/raw
                updateRoomSchedule(new JSONObject(new String(buffer)).toString(), RES_SOURCE);
            } else {
                updateRoomSchedule(sSharedPreferences.getString(
                        Constants.ROOM_SCHEDULE_KEY, ""), PREFS_SOURCE);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            sDaysOfWeekMap = new JSONObject();
        }
    }

    /**
     * Returns a list of buildings that contain classrooms.
     *
     * @return a list of buildings that contain classrooms
     */
    public String[] getBuildings() {
        return sBuildings == null ? new String[0] : sBuildings;
    }

    /**
     * Handles a user-requested refresh of the room schedules by 
     */
    public void handleManualRefresh(Activity activity) {
        refreshRoomScheduleAsync(activity, 0, true, false);
    }

    /**
     * Retrieves the room schedules from a JSON file hosted on GitHub and then by using the
     * UWaterloo Open Data API to get the schedule for each course.
     */
    public void refreshRoomScheduleAsync() {
        refreshRoomScheduleAsync(null, DEFAULT_MAX_NETWORK_FAILURES,
                false, true);
    }
    
    /**
     * Retrieves the room schedules from a JSON file hosted on GitHub and then by using the
     * UWaterloo Open Data API to get the schedule for each course.
     */
    private void refreshRoomScheduleAsync(Activity activity, int maxFailures,
                                          boolean showFailureToast, boolean useApi) {
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
                        String source = sSharedPreferences.getString(
                                Constants.ROOM_SCHEDULE_SOURCE_KEY, UNKNOWN_SOURCE);

                        if (!source.equals(API_SOURCE)) {
                            updateRoomSchedule(jsonString, GITHUB_SOURCE);
                        }
                        Log.d(TAG, "Updated room schedules from GitHub");
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        // Use the UWaterloo API to get room schedules if needed
                        if (useApi) {
                            retrieveSchedulesWithApi();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull final Call call, @NonNull IOException e) {
                    // Display the failure toast
                    if (showFailureToast && activity != null) {
                        CharSequence failureText = activity.getText(R.string.error_no_network);
                        activity.runOnUiThread(() ->
                                Toast.makeText(activity, failureText, Toast.LENGTH_SHORT).show());
                    }

                    // Try again after a delay if there has not been a certain number of failures
                    if (sNumNetworkFailures < maxFailures) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            sNumNetworkFailures++;
                            refreshRoomScheduleAsync(activity, maxFailures,
                                    showFailureToast, useApi);
                        }, REFRESH_WAIT_MS);
                    } else {
                        sNumNetworkFailures = 0;
                    }
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
        if (sApiRetrievalThread == null) {
            sApiRetrievalThread = new Thread(this::handleRetrieveSchedulesWithApi);
            sApiRetrievalThread.start();
        }
    }

    /**
     * Handles retrieving the room schedules, but this will take over a minute and block the thread.
     */
    private void handleRetrieveSchedulesWithApi() {
        // Initialize variables
        UWaterlooApi api = new UWaterlooApi(ApiKeys.UWATERLOO_API_KEY);
        String incompleteJson = sSharedPreferences.getString(
                Constants.INCOMPLETE_SCHEDULE_JSON_KEY, "");
        SharedPreferences.Editor editor = sSharedPreferences.edit();
        RoomSchedule incompleteRoomSchedule;
        
        // Initialize a room schedule with the incomplete JSON file
        try {
            incompleteRoomSchedule = new RoomSchedule(incompleteJson);
        } catch (JSONException e) {
            // Reset the progress
            incompleteRoomSchedule = new RoomSchedule();
            editor.putInt(Constants.SCHEDULE_PROGRESS_INDEX_KEY, 0);
            editor.putString(Constants.INCOMPLETE_SCHEDULE_JSON_KEY, "");
            editor.apply();
            Log.e(TAG, e.getMessage());
        }

        // Get the current term and the progress within the subjects
        int subjectProgressIndex = sSharedPreferences
                .getInt(Constants.SCHEDULE_PROGRESS_INDEX_KEY, 0)
                % sSubjects.length;

        // Iterate through every subject (ex. MATH, ECON, ENGL, ...) resuming from where we left off
        for (int i = subjectProgressIndex; i < sSubjects.length; i++) {
            // Get the subject to download
            String subject = sSubjects[i];
            Responses.CoursesSchedule response = null;

            // Get the schedule for the subject using a thread-blocking API call
            while (response == null) {
                response = Calls.unwrap(api.Terms.getSchedule(sTermManager.currentTerm(), subject));
            }

            List<CourseSchedule> subjectSchedule = response.getData();

            // Iterate through every course schedule in that subject (ex. CS 135 LEC 003, ...)
            for (CourseSchedule courseSchedule : subjectSchedule) {
                // Iterate through every class in that course section
                for (Class classInfo : courseSchedule.getClasses()) {
                    try {
                        incompleteRoomSchedule.update(classInfo);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }

            // Update the progress
            editor.putInt(Constants.SCHEDULE_PROGRESS_INDEX_KEY, i + 1);
            editor.putString(Constants.INCOMPLETE_SCHEDULE_JSON_KEY,
                    incompleteRoomSchedule.toString());
            editor.apply();
        }

        // Put the room schedule in shared preferences
        updateRoomSchedule(incompleteRoomSchedule.toString(), API_SOURCE);
    }

    /**
     * Updates the state of the class upon receiving a JSON string storing the room schedule.
     *
     * @param jsonString        a JSON string storing the room schedule data
     * @param source            the source of the JSON string
     */
    private void updateRoomSchedule(String jsonString, String source) {
        // Put the json string in shared preferences
        SharedPreferences.Editor editor = sSharedPreferences.edit();
        editor.putString(Constants.ROOM_SCHEDULE_KEY, jsonString);
        editor.putString(Constants.ROOM_SCHEDULE_SOURCE_KEY, source);
        editor.apply();

        // Get a list of buildings from the JSON object
        try {
            sRoomSchedule = new RoomSchedule(jsonString);
            JSONArray buildingNames = sRoomSchedule.names();
            sBuildings = new String[buildingNames.length()];

            // Store all of the building names in a list
            for (int i = 0; i < buildingNames.length(); i++) {
                sBuildings[i] = buildingNames.getString(i);
            }

            Arrays.sort(sBuildings);
        } catch (JSONException e) {
            Log.w(TAG, e.getMessage());   
        }
    }

    /**
     * Returns a list of rooms and the time intervals for which they are open for a given building
     * and the hours at which to start and end the search.
     *
     * @param building          the building in which to find open rooms
     * @param searchHour        the hour in which to search for an open classroom
     * @return                  a list of rooms and the time intervals at which they are open
     */
    public RoomTimeIntervalList findOpenRooms(String building, int searchHour) {
        try {
            // Get all of the rooms in that building and their room numbers
            JSONObject buildingRooms = sRoomSchedule.getJSONObject(building);
            JSONArray roomNums = buildingRooms.names();
            RoomTimeIntervalList buildingOpenSchedule = new RoomTimeIntervalList();

            // Update the variables storing the current time
            updateCurrentTime();

            // Iterate through each room in the building and add the time intervals
            // when that room is available to buildingOpenSchedule
            for (int i = 0; i < roomNums.length(); i++) {
                String roomNum = roomNums.getString(i);
                JSONArray classTimes = buildingRooms.getJSONArray(roomNum);
                addOpenTimeIntervals(buildingOpenSchedule, building, roomNum, classTimes);
            }

            // Sort the schedule chronologically and then by room number as a tie-breaker
            buildingOpenSchedule.sort();

            if (searchHour == sCurrentHour) {
                buildingOpenSchedule.filterByHourAndMin(sCurrentHour, sCurrentMin);
            } else {
                buildingOpenSchedule.filterByHourAndMin(searchHour, 0);
            }

            return buildingOpenSchedule;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Adds the time intervals that a particular room is open to a pre-existing
     * schedule of open classrooms.
     *
     * @param   buildingOpenSchedule    a schedule of when classroom are open for a building
     * @param   building                the building of the query
     * @param   roomNum                 the room number for which to find open time intervals
     * @param   classTimes              a list of the starting and ending date and time
     *                                  for all the classes that use that room
     * @throws  JSONException           if the classTimes JSONArray is not formatted properly
     */
    private static void addOpenTimeIntervals(
            RoomTimeIntervalList buildingOpenSchedule,
            String building, String roomNum, JSONArray classTimes) throws JSONException {

        // Use the fact that all classes start at either XX:00 or XX:30 and end at
        // either XX:20 or XX:50 to cleanly divide the day into half-hour blocks.
        // Note that Java arrays are auto-initialized to 0 or false in this case
        boolean[] occupiedHalfHours = new boolean[HALF_HOURS_PER_DAY * 2];

        // Iterate through each class in the JSON array
        for (int i = 0; i < classTimes.length(); i++) {
            // Get the class time at index i
            JSONArray classTime = classTimes.getJSONArray(i);

            // Only record the times that the classroom is used if the class occurs today
            if (classOccursToday(classTime)) {
                // Get the starting and ending hour and minute at pre-determined indices
                int startHour = classTime.getInt(START_HOUR_INDEX);
                int startMin = classTime.getInt(START_MIN_INDEX);
                int endHour = classTime.getInt(END_HOUR_INDEX);
                int endMin = classTime.getInt(END_MIN_INDEX);

                // Get the indices of the half hours when class starts and ends
                int startIndex = calcHalfHourIndex(startHour, startMin);
                int endIndex = calcHalfHourIndex(endHour, endMin);

                // Record each of the half hours in between the start and ending times as occupied
                for (int occupiedTime = startIndex; occupiedTime <= endIndex; occupiedTime++) {
                    occupiedHalfHours[occupiedTime] = true;
                }
            }
        }

        // Initialize variables to store the time when a classroom's open time interval begins
        // The value of -1 signifies that an open time interval has not yet begun yet
        int openStartHour = -1, openStartMin = -1;

        // Iterate from the starting index of the search to the index at the end of the day
        for (int i = 0; i < HALF_HOURS_PER_DAY; i++) {
            // If this is an open half-hour and we were not in the middle of an open time interval,
            // then we have entered an open time interval, so we record the starting hour and min.
            if (!occupiedHalfHours[i] && openStartHour == -1) {
                // We record open classroom times as starting at either XX:00 or XX:30
                // to match actual class times which always start at XX:00 or XX:30.
                int oneDayIndex = i % 48;
                openStartHour = oneDayIndex / 2;
                openStartMin = (oneDayIndex % 2 == 0) ? 0 : 30;
            }

            // If this is an occupied half-hour and we were in the middle of an open time interval,
            // then we have exited an open time interval, so we record the ending hour and min.
            if (occupiedHalfHours[i] && openStartHour != -1) {
                int oneDayIndex = i % 48;

                // Use oneDayIndex - 1 since we need the previous day's hour and minute.
                // We record open classroom times as ending at either XX:20 or XX:50
                // to match actual class times which always start at XX:20 or XX:50.
                int openEndHour = (oneDayIndex - 1) / 2;
                int openEndMin = ((oneDayIndex - 1) % 2 == 0) ? 20 : 50;

                // Add the time interval when the room is open to the building's open room schedule
                RoomTimeInterval openRoomTimeInterval = new RoomTimeInterval(
                        building, roomNum, openStartHour, openStartMin, openEndHour, openEndMin);
                buildingOpenSchedule.add(openRoomTimeInterval);

                // Record that we have exited an interval for when the room was open
                openStartHour = -1;
                openStartMin = -1;
            } else if (i == (HALF_HOURS_PER_DAY - 1) && openStartHour != -1) {
                // Record the ending time as 11:59PM
                int openEndHour = 23;
                int openEndMin = 59;

                // Add the time interval when the room is open to the building's open room schedule
                RoomTimeInterval openRoomTimeInterval = new RoomTimeInterval(
                        building, roomNum, openStartHour, openStartMin, openEndHour, openEndMin);
                buildingOpenSchedule.add(openRoomTimeInterval);
            }
        }
    }

    /**
     * Returns the index of the half-hour block for a given time.
     *
     * @param hour  the hour of the time in 24h format
     * @param min   the minute of the time
     * @return      the index of the half-hour block
     */
    private static int calcHalfHourIndex(int hour, int min) {
        if (min < 30) {
            return 2 * hour;
        } else {
            return 2 * hour + 1;
        }
    }

    /**
     * Returns true if the class occurs today and false otherwise. A class occurs today if
     * it occurs on the current day of the week and the current date is within the starting
     * and ending dates for that class.
     *
     * @param   classTime       the starting and ending dates and times for a class
     * @return                  true if the class occurs today, and false otherwise
     * @throws  JSONException   if the classTime JSON array is not formatted properly
     */
    private static boolean classOccursToday(JSONArray classTime) throws JSONException {
        return onCurrentDayOfWeek(classTime) && sCurrentDateWithinInterval(classTime);
    }


    /**
     * Returns true if the class occurs on the current day of the week and false otherwise.
     *
     * @param   classTime       the starting and ending dates and times for a class
     * @return                  true if the class occurs on the current day of the week,
     *                          and false otherwise
     * @throws  JSONException   if the classTime JSON array is not formatted properly
     */
    private static boolean onCurrentDayOfWeek(JSONArray classTime) throws JSONException {
        return classTime.getJSONArray(DAY_OF_WEEK_INDEX).getBoolean(sCurrentDayOfWeek);
    }

    /**
     * Returns true if the current date is within the starting and ending dates for that class
     * and false otherwise.
     *
     * @param   classTime       the starting and ending dates and times for a class
     * @return                  true if the current date is within the starting and
     *                          ending dates for that class and false otherwise
     * @throws  JSONException   if the classTime JSON array is not formatted properly
     */
    private static boolean sCurrentDateWithinInterval(JSONArray classTime) throws JSONException {
        int startMonth = classTime.getInt(START_MONTH_INDEX);
        int startDate = classTime.getInt(START_DATE_INDEX);
        int endMonth = classTime.getInt(END_MONTH_INDEX);
        int endDate = classTime.getInt(END_DATE_INDEX);

        int startDateCode = startMonth * 100 + startDate;
        int sCurrentDateCode = sCurrentMonth * 100 + sCurrentDate;
        int endDateCode = endMonth * 100 + endDate;

        return withinClosedInterval(startDateCode, sCurrentDateCode, endDateCode);
    }

    /**
     * Updates the instance variables that store the current time and date.
     */
    private static void updateCurrentTime() {
//        // Get an instance of a Calendar object that stores all the date for the current time
//        Calendar calendar = Calendar.getInstance();
//
//        // Update the current month, date of the month, and minute
//        sCurrentMonth = calendar.get(Calendar.MONTH) + 1;
//        sCurrentDate = calendar.get(Calendar.DATE);
//        sCurrentHour = calendar.get(Calendar.HOUR_OF_DAY);
//        sCurrentMin = calendar.get(Calendar.MINUTE);
//
//        // Update the current day of the week
//        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
//            case Calendar.MONDAY:
//                sCurrentDayOfWeek = 0;
//                break;
//            case Calendar.TUESDAY:
//                sCurrentDayOfWeek = 1;
//                break;
//            case Calendar.WEDNESDAY:
//                sCurrentDayOfWeek = 2;
//                break;
//            case Calendar.THURSDAY:
//                sCurrentDayOfWeek = 3;
//                break;
//            case Calendar.FRIDAY:
//                sCurrentDayOfWeek = 4;
//                break;
//            case Calendar.SATURDAY:
//                sCurrentDayOfWeek = 5;
//                break;
//            case Calendar.SUNDAY:
//                sCurrentDayOfWeek = 6;
//                break;
//        }

        // TODO WL: IMPORTANT!!! Use the actual date and time
        sCurrentMonth = 7;
        sCurrentDate = 30;
        sCurrentHour = 12;
        sCurrentMin = 12;
        sCurrentDayOfWeek = 1;
    }

    /**
     * Returns true if num is within the closed interval [min, max] and false otherwise.
     *
     * @param   min the left-hand boundary of the interval
     * @param   num the number to check
     * @param   max the right-hand boundary of the interval
     * @return  true if num is within [min, max] and false otherwise
     */
    private static boolean withinClosedInterval(int min, int num, int max) {
        return min <= num && num <= max;
    }


    /**
     * A JSONObject representation of the room schedule with an additional method for updating
     * itself with new data about a class.
     */
    private class RoomSchedule extends JSONObject {
        RoomSchedule() {
            super();
        }

        RoomSchedule(String json) throws JSONException{
            super(json);
        }

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
            newClassTime.put( getChars0And1(endTime, 22) );
            newClassTime.put( getChars3And4(endTime, 0) );
            newClassTime.put( sDaysOfWeekMap.getJSONArray(weekdaysString) );
            newClassTime.put( getChars0And1(startDateString, sTermManager.lecturePeriodStartMonth()) );
            newClassTime.put( getChars3And4(startDateString, sTermManager.lecturePeriodStartDate()) );
            newClassTime.put( getChars0And1(endDateString, sTermManager.lecturePeriodEndMonth()) );
            newClassTime.put( getChars3And4(endDateString, sTermManager.lecturePeriodEndDate()) );

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
    }
}
