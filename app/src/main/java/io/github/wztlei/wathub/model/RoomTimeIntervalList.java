package io.github.wztlei.wathub.model;

import java.util.ArrayList;
import java.util.Collections;

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
}
