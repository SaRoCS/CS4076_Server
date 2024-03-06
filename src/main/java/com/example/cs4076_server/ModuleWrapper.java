package com.example.cs4076_server;

import org.json.simple.JSONObject;

import java.time.LocalTime;

/**
 * A univeristy module
 */
public class ModuleWrapper {

    /**
     * Holds the JSON data about the module
     */
    private final JSONObject data;

    /**
     * Class constructor
     *
     * @param data A JSONObject which contains information about the module
     */
    public ModuleWrapper(JSONObject data) {
        this.data = data;
    }

    /**
     * Retrieves the name of the module from the JSON data
     *
     * @return The name of the module
     */
    public String getName() {
        return data.get("name").toString();
    }

    /**
     * Retrieves the day of the week that this module takes place from the JSON data
     *
     * @return The day of the week
     */
    public String getDayOfWeek() {
        return data.get("dayOfWeek").toString();
    }

    /**
     * Retrieves the time that the module starts from the JSON data
     *
     * @return The start time
     */
    public LocalTime getStartTime() {
        return (LocalTime) data.get("startTime");
    }

    /**
     * Retrieves the time that the module ends from the JSON data
     *
     * @return The end time
     */
    public LocalTime getEndTime() {
        return (LocalTime) data.get("endTime");
    }

    /**
     * Retrieves the room number where the module takes place from the JSON data
     *
     * @return The room number
     */
    public String getRoomNumber() {
        return data.get("roomNumber").toString();
    }

    /**
     * Determines if a given module overlaps with this module
     *
     * @param newModule The module to compare with
     * @return True if the modules overlap, false otherwise
     */
    public boolean overlaps(ModuleWrapper newModule) {
        // Check if they are on the same day
        if (newModule.getDayOfWeek().equals(this.getDayOfWeek())) {
            // Is the start time in the time window of this module
            boolean startOverlaps = newModule.getStartTime().isAfter(this.getStartTime()) && newModule.getStartTime().isBefore(this.getEndTime());
            // Is the end time in the window of this module
            boolean endOverlaps = newModule.getEndTime().isAfter(this.getStartTime()) && newModule.getEndTime().isBefore(this.getEndTime());
            return startOverlaps || endOverlaps;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        // Make sure they are the same types
        if ((o == null) || (o.getClass() != this.getClass())) {
            return false;
        }

        ModuleWrapper other = (ModuleWrapper) o;

        // Compare the data fields
        return other.getName().equals(this.getName()) && other.getDayOfWeek().equals(this.getDayOfWeek()) && other.getStartTime().equals(this.getStartTime()) && other.getStartTime().equals(this.getStartTime()) && other.getEndTime().equals(this.getEndTime()) && other.getRoomNumber().equals(this.getRoomNumber());
    }
}
