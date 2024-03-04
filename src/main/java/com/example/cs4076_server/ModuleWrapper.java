package com.example.cs4076_server;

import org.json.simple.JSONObject;

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

    public String getStartTime() {
        return data.get("startTime").toString();
    }

    public String getEndTime() {
        return data.get("endTime").toString();
    }

    public String getRoomNumber() {
        return data.get("roomNUmber").toString();
    }

    public boolean overlaps(ModuleWrapper newModule) {
        return true;
    }
}
