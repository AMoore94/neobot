package com.neobot;

import java.time.DayOfWeek;
import java.time.Instant;

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

    public String getFormattedTime() {
        int displayHour = (hour % 12 == 0) ? 12 : hour % 12;
        return String.format("%02d:%02d", displayHour, minute);
    }

    public Instant getInstant() {
        return Instant.now().atZone(java.time.ZoneId.of("-06:00"))
                                            .withHour(getHour())
                                            .withMinute(getMinute())
                                            .withSecond(0)
                                            .withNano(0)
                                            .toInstant();
    }

}
