package com.example.cs4076_server;

import javafx.concurrent.Task;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

/**
 * A javafx Task for shifting scheduled lectures to the earliest time available
 */
public class EarlyLecturesTask extends Task<Void> {
    /**
     * The shared schedule to be modified
     */
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<ModuleWrapper>> schedule;

    public EarlyLecturesTask(ConcurrentHashMap<String, CopyOnWriteArrayList<ModuleWrapper>> schedule) {
        this.schedule = schedule;
    }

    @Override
    protected Void call() {
        shiftLectures();
        return null;
    }

    /**
     * Use the Fork-Join rule to shift lectures
     */
    private void shiftLectures() {
        ForkJoinPool.commonPool().invoke(new ShiftLectures_ForkJoin(schedule, 0, 4));
    }
}
