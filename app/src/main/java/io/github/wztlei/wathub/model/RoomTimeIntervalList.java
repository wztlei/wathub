package io.github.wztlei.wathub.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class RoomTimeIntervalList extends ArrayList<RoomTimeInterval>  {
    /**
     * Sorts a RoomTimeIntervalList chronologically and using
     * the building and room numbers for tie-breakers.
     */
    public void sort() {
        Collections.sort(this, (rti1, rti2) -> {
            // Compare the fields of the two RoomTimeIntervals in the following order:
            // 1. Starting hour
            // 2. Starting minute
            // 3. Ending hour
            // 4. Ending minute
            // 5. Building
            // 6. Room number
            if (rti1.getStartHour() != rti2.getStartHour()) {
                return rti1.getStartHour() - rti2.getStartHour();
            } else if (rti1.getStartMin() != rti2.getStartMin()) {
                return rti1.getStartMin() - rti2.getStartMin();
            } else if (rti1.getEndHour() != rti2.getEndHour()) {
                return rti1.getEndHour() - rti2.getEndHour();
            } else if (rti1.getEndMin() != rti2.getEndMin()) {
                return rti1.getEndMin() - rti2.getEndMin();
            }

            String room1 = rti1.getBuilding() + rti1.getRoomNum();
            String room2 = rti2.getBuilding() + rti2.getRoomNum();
            return room1.compareTo(room2);
        });
    }

    /**
     * Filters itself by only keeping time intervals that contain the hour passed in.
     *
     * @param hour  the hour that time intervals must contain
     */
    public void filterByHour(int hour) {
        for (Iterator<RoomTimeInterval> iterator = this.iterator(); iterator.hasNext();) {
            RoomTimeInterval rti = iterator.next();

            if (rti.getStartHour() < hour && rti.getEndHour() < hour) {
                iterator.remove();
            } else if (rti.getStartHour() > hour && rti.getEndHour() > hour) {
                iterator.remove();
            }
        }
    }

    /**
     * Filters itself by only keeping time intervals that contain the hour and min passed in.
     *
     * @param hour  the hour that time intervals must contain
     * @param min   the min that time intervals must contain
     */
    public void filterByHourAndMin(int hour, int min) {
        int filterMinOfDay = minOfDay(hour, min);

        for (Iterator<RoomTimeInterval> iterator = this.iterator(); iterator.hasNext();) {
            RoomTimeInterval rti = iterator.next();
            int startMinOfDay = minOfDay(rti.getStartHour(), rti.getStartMin());
            int endMinOfDay = minOfDay(rti.getEndHour(), rti.getEndMin());

            if (!(startMinOfDay <= filterMinOfDay && filterMinOfDay <= endMinOfDay)) {
                iterator.remove();
            }
        }
    }

    /**
     * Returns the minute of the day.
     *
     * @param   hour    the hour of the day
     * @param   min     the minute of the hour
     * @return          the minute of the day
     */
    private int minOfDay(int hour, int min) {
        return hour * 60 + min;
    }
}
