package io.github.wztlei.wathub.model;

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

    public int getStartHour() {
        return startHour;
    }

    public int getStartMin() {
        return startMin;
    }

    public int getEndHour() {
        return endHour;
    }

    public int getEndMin() {
        return endMin;
    }
}
