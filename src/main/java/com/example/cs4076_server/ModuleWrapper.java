package com.example.cs4076_server;

import org.json.simple.JSONObject;

import java.time.LocalTime;

public class ModuleWrapper {

    private final JSONObject data;

    public ModuleWrapper(JSONObject data) {
        this.data = data;
    }

    public String getName() {
        return data.get("name").toString();
    }

    public String getDayOfWeek() {
        return data.get("dayOfWeek").toString();
    }

    public LocalTime getStartTime() {
        return (LocalTime) data.get("startTime");
    }

    public LocalTime getEndTime() {
        return (LocalTime) data.get("endTime");
    }

    public String getRoomNumber() {
        return data.get("roomNumber").toString();
    }

    public boolean overlaps(ModuleWrapper newModule) {
        if (newModule.getDayOfWeek().equals(this.getDayOfWeek())) {
            boolean startOverlaps = !newModule.getStartTime().isBefore(this.getStartTime()) && !newModule.getStartTime().isAfter(this.getEndTime());
            boolean endOverlaps = !newModule.getEndTime().isBefore(this.getStartTime()) && !newModule.getEndTime().isAfter(this.getEndTime());
            return startOverlaps || endOverlaps;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (o.getClass() != this.getClass())) {
            return false;
        }

        ModuleWrapper other = (ModuleWrapper) o;

        return other.getName().equals(this.getName()) && other.getDayOfWeek().equals(this.getDayOfWeek()) && other.getStartTime().equals(this.getStartTime()) && other.getStartTime().equals(this.getStartTime()) && other.getEndTime().equals(this.getEndTime()) && other.getRoomNumber().equals(this.getRoomNumber());
    }
}
