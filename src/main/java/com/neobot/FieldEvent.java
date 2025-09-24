package com.neobot;

import java.time.DayOfWeek;

import lombok.Getter;

@Getter
public class FieldEvent {

    private int hour;
    private int minute;
    private DayOfWeek day;
    private String location;

    public FieldEvent(String hourMinute, String location, DayOfWeek day) {
        String[] parts = hourMinute.split(":");
        this.hour = Integer.parseInt(parts[0]);
        this.minute = Integer.parseInt(parts[1]);
        this.location = location;
        this.day = day;
    }

}
