package com.example.cs4076_server;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;

/**
 * Class that uses divide-and-conquer method to shift lectures
 */
public class ShiftLectures_ForkJoin extends RecursiveAction {
    /**
     * List of the days of the week
     */
    private final ArrayList<String> days;
    /**
     * The schedule to operate on
     */
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<ModuleWrapper>> schedule;

    private final int low;
    private final int high;

    public ShiftLectures_ForkJoin(ConcurrentHashMap<String, CopyOnWriteArrayList<ModuleWrapper>> schedule, int low, int high) {
        this.days = new ArrayList<>(schedule.keySet());
        this.schedule = schedule;
        this.low = low;
        this.high = high;
    }

    @Override
    protected void compute() {
        // Divide the work until it can be run by a single thread
        if (high == low) {
            // Get the schedule for the particular day
            CopyOnWriteArrayList<ModuleWrapper> daySchedule = schedule.get(days.get(high));

            int i = 0;
            for (ModuleWrapper module : daySchedule) {
                // Since the list is sorted, the first module will always be shifted to 09:00
                if (i == 0) {
                    module.shiftTime(LocalTime.parse("09:00"));
                } else {
                    // Shift the module so that it starts right after the previous module
                    module.shiftTime(daySchedule.get(i - 1).getEndTime());
                }
                i++;
            }

        } else {
            // Split the work
            int mid = (low + high) / 2;
            ShiftLectures_ForkJoin left = new ShiftLectures_ForkJoin(schedule, low, mid);
            ShiftLectures_ForkJoin right = new ShiftLectures_ForkJoin(schedule, mid + 1, high);
            left.fork();
            right.compute();
            left.join();
        }
    }
}
