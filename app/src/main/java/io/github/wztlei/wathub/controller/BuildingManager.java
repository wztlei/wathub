package io.github.wztlei.wathub.controller;

import android.content.Context;

public class BuildingManager {
    private static BuildingManager sInstance;

    /**
     * Initializes the static instance of a BuildingManager.
     *
     * @param context the context in which the BuildingManager is initialized
     */
    public static void init(Context context) {
        if (sInstance != null) {
            throw new IllegalStateException("RoomScheduleManager already instantiated!");
        }
        sInstance = new BuildingManager(context);
    }

    /**
     * Returns the static instance of a BuildingManager.
     *
     * @return the static instance of a BuildingManager.
     */
    public static BuildingManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("RoomScheduleManager not instantiated!");
        }
        return sInstance;
    }

    /**
     * Constructor for a BuildingManager object.
     *
     * @param context the context in which the BuildingManager is created
     */
    private BuildingManager(Context context) {

    }
}
