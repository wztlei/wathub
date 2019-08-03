package io.github.wztlei.wathub.model;

import java.util.Calendar;

import io.github.wztlei.wathub.utils.DateTimeUtils;

public class RoomTimeInterval {
    private String building;
    private String roomNum;
    private int startHour;
    private int startMin;
    private int endHour;
    private int endMin;

    public RoomTimeInterval(String building, String roomNum, int startHour,
                            int startMin, int endHour, int endMin) {
        this.building = building;
        this.roomNum = roomNum;
        this.startHour = startHour;
        this.startMin = startMin;
        this.endHour = endHour;
        this.endMin = endMin;
    }

    public String getBuilding() {
        return building;
    }

    public String getRoomNum() {
        return roomNum;
    }

    int getStartHour() {
        return startHour;
    }

    int getStartMin() {
        return startMin;
    }

    int getEndHour() {
        return endHour;
    }

    int getEndMin() {
        return endMin;
    }

    public String formatTimeInterval() {
        // Get the starting and ending times for when the room is open
        if (currentTimeWithinInterval()) {
            return "Now - " + DateTimeUtils.format12hTime(endHour, endMin);
        } else {
            // Create a string to store the formatted time interval
            return DateTimeUtils.format12hTime(startHour, startMin) + " - "
                    + DateTimeUtils.format12hTime(endHour, endMin);
        }
    }

    private boolean currentTimeWithinInterval() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMin = calendar.get(Calendar.MINUTE);
        int startMinOfDay = DateTimeUtils.minOfDay(startHour, startMin);
        int currentMinOfDay = DateTimeUtils.minOfDay(currentHour, currentMin);
        int endMinOfDay = DateTimeUtils.minOfDay(endHour, endMin);

        return startMinOfDay <= currentMinOfDay && currentMinOfDay <= endMinOfDay;
    }
}
