package com.example.cs4076_server;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;

public class ShiftLectures_ForkJoin extends RecursiveAction {
    static final int THRESHOLD = 1;
    private final ArrayList<String> days;
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<ModuleWrapper>> schedule;
    int low;
    int high;

    public ShiftLectures_ForkJoin(ConcurrentHashMap<String, CopyOnWriteArrayList<ModuleWrapper>> schedule, ArrayList<String> days, int low, int high) {
        this.days = days; //new ArrayList<String>(schedule.keySet());
        this.schedule = schedule;
        this.low = low;
        this.high = high;
    }

    @Override
    protected void compute() {
        if (high == low) {
            //shift schedule
            CopyOnWriteArrayList<ModuleWrapper> daySchedule = schedule.get(days.get(high));
            int i = 0;
            for (ModuleWrapper module : daySchedule) {
                if (i == 0) {
                    module.shiftTime(LocalTime.parse("09:00"));
                } else {
                    module.shiftTime(daySchedule.get(i - 1).getEndTime());
                }
                i++;
            }
            long startTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - startTime < 20000) {
            }
        } else {
            int mid = (low + high) / 2;
            ShiftLectures_ForkJoin left = new ShiftLectures_ForkJoin(schedule, days, low, mid);
            ShiftLectures_ForkJoin right = new ShiftLectures_ForkJoin(schedule, days, mid + 1, high);
            left.fork();
            right.compute();
            left.join();
        }
    }
}
