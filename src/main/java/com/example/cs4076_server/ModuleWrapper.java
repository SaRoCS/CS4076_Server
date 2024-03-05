package com.example.cs4076_server;

import org.json.simple.JSONObject;

import java.time.LocalTime;

public class ModuleWrapper {

    private JSONObject data;

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
        return data.get("roomNUmber").toString();
    }

    public boolean overlaps(ModuleWrapper newModule) {
        if (newModule.getDayOfWeek().equals(this.getDayOfWeek())) {
            boolean startOverlaps = newModule.getStartTime().compareTo(this.getStartTime()) >= 0 && newModule.getStartTime().compareTo(this.getEndTime()) <= 0;
            boolean endOverlaps = newModule.getEndTime().compareTo(this.getStartTime()) >= 0 && newModule.getEndTime().compareTo(this.getEndTime()) <= 0;
            return startOverlaps || endOverlaps;
        }
        return false;
    }

    public boolean equals(ModuleWrapper newModule) {
        if(newModule.getName().equals(this.getName()) && newModule.getDayOfWeek().equals(this.getDayOfWeek()) &&
                newModule.getStartTime().equals(this.getStartTime()) && newModule.getStartTime().equals(this.getStartTime()) &&
                newModule.getEndTime().equals(this.getEndTime()) && newModule.getRoomNumber().equals(this.getRoomNumber())) {
            return true;
        }
        return false;
    }
}
