package io.github.wztlei.wathub.controller;

import android.content.Context;
import android.content.SharedPreferences;

import com.deange.uwaterlooapi.UWaterlooApi;
import com.deange.uwaterlooapi.model.common.Responses;

import java.util.Calendar;

import io.github.wztlei.wathub.ApiKeys;
import io.github.wztlei.wathub.Constants;
import io.github.wztlei.wathub.net.Calls;

public class TermManager {
    private static TermManager sInstance;
    private static SharedPreferences sSharedPreferences;

    private static int sCurrentTerm = 1199;

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
        sSharedPreferences = context.getSharedPreferences(
                Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        Calendar calendar = Calendar.getInstance();

        int yearDigits = calendar.get(Calendar.YEAR) - 1900;
        int month = calendar.get(Calendar.MONTH);
        int startMonth;

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


    }

    int currentTerm() {
        return sCurrentTerm;
    }

    /**
     * Returns the start month for the current term.
     *
     * @return the start month for the current term
     */
    int currentTermStartMonth() {
        return sCurrentTerm % 10;
    }

    /**
     * Returns the start date for the current term.
     *
     * @return the start date for the current term
     */
    int currentTermStartDate() {
        return 1;
    }

    /**
     * Returns the ending month for the current term.
     *
     * @return the end month for the current term
     */
    int currentTermEndMonth() {
        return (sCurrentTerm % 10) + 3;
    }

    /**
     * Returns the end date for the current term.
     *
     * @return the end date for the current term
     */
    int currentTermEndDate() {
        return 6;
    }
}
