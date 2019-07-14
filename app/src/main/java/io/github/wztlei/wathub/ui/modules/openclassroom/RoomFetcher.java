package io.github.wztlei.wathub.ui.modules.openclassroom;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.deange.uwaterlooapi.UWaterlooApi;
import com.deange.uwaterlooapi.model.courses.Class;
import com.deange.uwaterlooapi.model.courses.CourseSchedule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.net.Calls;

public class RoomFetcher{

    private String[] mSubjects;
    private SharedPreferences mSharedPreferences;
    private static final String TAG = "WL/RoomFetcher";

    public RoomFetcher (Context context, String[] subjects) {
        mSubjects = subjects;
        mSharedPreferences = context.getSharedPreferences(
                Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        Log.d(TAG, "RoomFetcher");
    }

    public void retrieveSchedules() {
        Thread thread = new Thread(this::handleRetrieveSchedules);
        thread.start();
    }

    private void handleRetrieveSchedules() {
        // Get the arguments passed into the class
        UWaterlooApi api = new UWaterlooApi(Constants.UWATERLOO_API_KEY);
        Log.d(TAG, "doInBackground");
        // Initialize variables
        JSONObject rooms = new JSONObject();
        int currentTerm = Calls.unwrap(api.Terms.getTermList()).getData().getCurrentTerm();
        Log.d(TAG, "doInBackground2");
        // Iterate through every subject (ex. MATH, ECON, ENGL, ...)
        for (String subject : mSubjects) {
            Log.d(TAG, "Retrieving the " + subject + " schedule");

            List<CourseSchedule> subjectSchedule =
                     Calls.unwrap(api.Terms.getSchedule(currentTerm, subject)).getData();

            // Iterate through every course schedule in that subject (ex. CS 135 LEC 003, ...)
            for (CourseSchedule courseSchedule : subjectSchedule) {
                // Iterate through every class in that course section
                for (Class classSchedule : courseSchedule.getClasses()) {
                    if (classSchedule == null) {
                        continue;
                    }

                    String building = classSchedule.getBuilding();
                    String room = classSchedule.getRoom();

                    if (building == null || room == null) {
                        continue;
                    }

                    try {
                        // Check if we need to add a new building to the JSON object
                        if (!rooms.has(building)) {
                            rooms.put(building, new JSONArray());
                        }

                        // Put the room in the JSON object organized by buildings
                        rooms.getJSONArray(building).put(room);
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }

        // Put the rooms string in shared preferences
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.ROOMS_KEY, rooms.toString());
        editor.apply();
    }
}
