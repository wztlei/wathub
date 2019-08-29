package io.github.wztlei.wathub.controller;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import io.github.wztlei.wathub.R;

public class TermManager {
    private static TermManager sInstance;
    private static JSONObject sTermInfo;
    private static int sCurrentTerm;

    private static final String TAG = "WL/TermManager";
    private static final int START_MONTH_INDEX = 0;
    private static final int START_DATE_INDEX = 1;
    private static final int END_MONTH_INDEX = 2;
    private static final int END_DATE_INDEX = 3;

    /**
     * Initializes the static instance of a TermManager.
     *
     * @param context the context in which the TermManager is initialized
     */
    public static void init(Context context) {
        if (sInstance != null) {
            throw new IllegalStateException("RoomScheduleManager already instantiated!");
        }
        sInstance = new TermManager(context);
    }

    /**
     * Returns the static instance of a TermManager.
     *
     * @return the static instance of a TermManager.
     */
    public static TermManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("RoomScheduleManager not instantiated!");
        }
        return sInstance;
    }

    /**
     * Constructor for a TermManager object.
     *
     * @param context the context in which the TermManager is created
     */
    private TermManager(Context context) {
        updateCurrentTerm();
        updateTermInfo(context);
    }

    /**
     * Updates the number for the current term.
     */
    private void updateCurrentTerm() {
        // Determine the digits that denote the year of the current term
        Calendar calendar = Calendar.getInstance();
        int yearDigits = calendar.get(Calendar.YEAR) - 1900;
        int month = calendar.get(Calendar.MONTH);
        int startMonth;

        // Determine the starting month of the current term
        if (Calendar.JANUARY <= month && month <= Calendar.APRIL) {
            startMonth = 1;
        } else if (Calendar.MAY <= month && month <= Calendar.AUGUST) {
            startMonth = 5;
        } else if (Calendar.SEPTEMBER <= month && month <= Calendar.DECEMBER) {
            startMonth = 9;
        } else {
            throw new IllegalStateException(month + " is not a valid month number");
        }

        sCurrentTerm = yearDigits * 10 + startMonth;

        if (sCurrentTerm < 1195) {
            throw new IllegalStateException(sCurrentTerm + " is not a valid term number");
        }
    }

    /**
     * Updates the JSON object storing the info about the lecture
     * period dates for the current and future terms.
     *
     * @param context the context in which the TermManager is created
     */
    private void updateTermInfo(Context context) {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.lecture_period_dates);
            int size = is.available();
            byte[] buffer = new byte[size];
            // noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            is.close();
            sTermInfo = new JSONObject(new String(buffer));
        } catch (IOException | JSONException e) {
            Log.w(TAG, e.getMessage());
            sTermInfo = new JSONObject();
        }
    }

    /**
     * Returns the current term number.
     *
     * @return the current term number
     */
    public int currentTerm() {
        if (sCurrentTerm < 1195) {
            throw new IllegalStateException(sCurrentTerm + " is not a valid term number");
        } else {
            return sCurrentTerm;
        }
    }

    /**
     * Returns the start month for the current term.
     *
     * @return the start month for the current term
     */
    int lecturePeriodStartMonth() {
        updateCurrentTerm();

        // Use a default estimated starting month
        String currentTermString = Integer.toString(sCurrentTerm);
        int startMonth = sCurrentTerm % 10;

        // Use the JSON file to refine the starting month
        try {
            if (sTermInfo.has(currentTermString)) {
                startMonth = sTermInfo.getJSONArray(currentTermString).getInt(START_MONTH_INDEX);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return startMonth;
    }

    /**
     * Returns the start date for the current term.
     *
     * @return the start date for the current term
     */
    int lecturePeriodStartDate() {
        updateCurrentTerm();

        // Use a default estimated starting date
        String currentTermString = Integer.toString(sCurrentTerm);
        int startDate = 1;

        // Use the JSON file to refine the starting date
        try {
            if (sTermInfo.has(currentTermString)) {
                startDate = sTermInfo.getJSONArray(currentTermString).getInt(START_DATE_INDEX);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return startDate;
    }

    /**
     * Returns the ending month for the current term.
     *
     * @return the end month for the current term
     */
    int lecturePeriodEndMonth() {
        updateCurrentTerm();

        // Use a default estimated ending month
        String currentTermString = Integer.toString(sCurrentTerm);
        int endMonth = (sCurrentTerm % 10) + 3;

        // Use the JSON file to refine the ending month
        try {
            if (sTermInfo.has(currentTermString)) {
                endMonth = sTermInfo.getJSONArray(currentTermString).getInt(END_MONTH_INDEX);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return endMonth;
    }

    /**
     * Returns the end date for the current term.
     *
     * @return the end date for the current term
     */
    int lecturePeriodEndDate() {
        updateCurrentTerm();

        // Use a default estimated ending date
        String currentTermString = Integer.toString(sCurrentTerm);
        int endDate = (sCurrentTerm % 10) + 3;

        // Use the JSON file to refine the ending date
        try {
            if (sTermInfo.has(currentTermString)) {
                endDate = sTermInfo.getJSONArray(currentTermString).getInt(END_DATE_INDEX);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return endDate;
    }
}
