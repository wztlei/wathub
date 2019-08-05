package io.github.wztlei.wathub.model;

import java.util.Calendar;

import io.github.wztlei.wathub.utils.DateTimeUtils;

public class RoomTimeInterval {
    private String building;
    private String roomNum;
    private int month;
    private int date;
    private int startHour;
    private int startMin;
    private int endHour;
    private int endMin;

    public RoomTimeInterval(String building, String roomNum, int month, int date,
                            int startHour, int startMin, int endHour, int endMin) {
        this.building = building;
        this.roomNum = roomNum;
        this.month = month;
        this.date = date;
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

    public int getMonth() {
        return month;
    }

    public int getDate() {
        return date;
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

    public String formatMonthAndDate() {
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        if (onCalendarDate(today)) {
            return "Today";
        } else if (onCalendarDate(tomorrow)) {
            return "Tomorrow";
        } else {
            return String.format("%s %s", formatMonth(), Integer.toString(date));
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

    private boolean onCalendarDate(Calendar calendar) {
        return (calendar.get(Calendar.MONTH) + 1 == month)
                && (calendar.get(Calendar.DAY_OF_MONTH) == date);
    }

    private String formatMonth() {
        switch (month) {
            case 1: return "Jan";
            case 2: return "Feb";
            case 3: return "Mar";
            case 4: return "Apr";
            case 5: return "May";
            case 6: return "Jun";
            case 7: return "Jul";
            case 8: return "Aug";
            case 9: return "Sep";
            case 10: return "Oct";
            case 11: return "Nov";
            case 12: return "Dec";
            default: return null;
        }
    }
}
