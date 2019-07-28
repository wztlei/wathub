package io.github.wztlei.wathub.controller;

import android.content.Context;

public class TermManager {
    private static TermManager sInstance;

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
     * Constructor for a RoomScheduleManager object.
     *
     * @param context the context in which the Room Fetcher is created
     */
    private TermManager(Context context) {

    }
}
