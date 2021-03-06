package io.github.wztlei.wathub.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import io.github.wztlei.wathub.utils.DateTimeUtils;

public class RoomTimeIntervalList extends ArrayList<RoomTimeInterval>  {

    private static HashMap<String, HashSet<String>> invalidClassrooms;

    static {
        invalidClassrooms = new HashMap<>();
        invalidClassrooms.put("MC", new HashSet<>(Arrays.asList("2009", "5501", "5403", "5479", "6460")));
    }

    @Override
    public RoomTimeIntervalList clone() {
        return (RoomTimeIntervalList) super.clone();
    }

    /**
     * Sorts a RoomTimeIntervalList by building and room numbers and then chronologically.
     */
    public void sort() {
        Collections.sort(this, (rti1, rti2) -> {
            // Compare the fields of the two RoomTimeIntervals in the following order:
            // 1. Building
            // 2. Room number
            // 3. Starting hour
            // 4. Starting minute
            // 5. Ending hour
            // 6. Ending minute
            String room1 = rti1.formatRoom();
            String room2 = rti2.formatRoom();

            if (!room1.equals(room2)) {
                return room1.compareTo(room2);
            } else if (rti1.getStartHour() != rti2.getStartHour()) {
                return rti1.getStartHour() - rti2.getStartHour();
            } else if (rti1.getStartMin() != rti2.getStartMin()) {
                return rti1.getStartMin() - rti2.getStartMin();
            } else if (rti1.getEndHour() != rti2.getEndHour()) {
                return rti1.getEndHour() - rti2.getEndHour();
            } else {
                return rti1.getEndMin() - rti2.getEndMin();
            }
        });
    }

    /**
     * Removes invalid classrooms from the list.
     */
    public void filterInvalidClassrooms() {
        for (Iterator<RoomTimeInterval> iterator = this.iterator(); iterator.hasNext();) {
            RoomTimeInterval rti = iterator.next();

            if (invalidClassrooms.containsKey(rti.getBuilding())
                    && invalidClassrooms.get(rti.getBuilding()).contains(rti.getRoomNum())) {
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
        int filterMinOfDay = DateTimeUtils.minOfDay(hour, min);

        for (Iterator<RoomTimeInterval> iterator = this.iterator(); iterator.hasNext();) {
            RoomTimeInterval rti = iterator.next();
            int startMinOfDay = DateTimeUtils.minOfDay(rti.getStartHour(), rti.getStartMin());
            int endMinOfDay = DateTimeUtils.minOfDay(rti.getEndHour(), rti.getEndMin());

            if (!(startMinOfDay <= filterMinOfDay && filterMinOfDay <= endMinOfDay)) {
                iterator.remove();
            }
        }
    }

    /**
     * Truncates the RoomTimeInterval list to a maximum length of maxSize.
     *
     * @param   maxSize the maximum length of the truncated list
     * @return          the truncated list
     */
    public RoomTimeIntervalList truncate(int maxSize) {
        RoomTimeIntervalList truncatedList = new RoomTimeIntervalList();

        for (int i = 0; i < Math.min(maxSize, this.size()); i++) {
            truncatedList.add(this.get(i));
        }

        return truncatedList;
    }
}
