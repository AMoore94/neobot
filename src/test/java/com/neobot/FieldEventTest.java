package com.neobot;

import org.junit.Test;

public class FieldEventTest {
    
    @Test
    public void testGetFormattedTime() {
        FieldEvent fe = new FieldEvent("00:00", "TestLocation", java.time.DayOfWeek.MONDAY);
        assert fe.getFormattedTime().equals("12:00") : "Expected 12:00 but got " + fe.getFormattedTime();

        FieldEvent fe1 = new FieldEvent("04:15", "TestLocation", java.time.DayOfWeek.MONDAY);
        assert fe1.getFormattedTime().equals("4:15") : "Expected 4:15 but got " + fe1.getFormattedTime();

        FieldEvent fe2 = new FieldEvent("12:30", "TestLocation", java.time.DayOfWeek.MONDAY);
        assert fe2.getFormattedTime().equals("12:30") : "Expected 12:30 but got " + fe2.getFormattedTime();

        FieldEvent fe3 = new FieldEvent("13:05", "TestLocation", java.time.DayOfWeek.MONDAY);
        assert fe3.getFormattedTime().equals("1:05") : "Expected 1:05 but got " + fe3.getFormattedTime();

        FieldEvent fe4 = new FieldEvent("23:59", "TestLocation", java.time.DayOfWeek.MONDAY);
        assert fe4.getFormattedTime().equals("11:59") : "Expected 11:59 but got " + fe4.getFormattedTime();
    }
}
