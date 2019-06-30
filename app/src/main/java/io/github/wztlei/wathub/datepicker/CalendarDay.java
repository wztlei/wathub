package io.github.wztlei.wathub.datepicker;

import java.util.Calendar;

/**
 * A convenience class to represent a specific date.
 */
public class CalendarDay {
    int year;
    int month;
    int day;
    private Calendar calendar;

    CalendarDay() {
        setTime(System.currentTimeMillis());
    }

    CalendarDay(long timeInMillis) {
        setTime(timeInMillis);
    }

    CalendarDay(Calendar calendar) {
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    CalendarDay(int year, int month, int day) {
        setDay(year, month, day);
    }

    public void set(CalendarDay date) {
        year = date.year;
        month = date.month;
        day = date.day;
    }

    private void setDay(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    private void setTime(long timeInMillis) {
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.setTimeInMillis(timeInMillis);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }
}
