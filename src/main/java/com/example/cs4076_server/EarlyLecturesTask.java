package com.example.cs4076_server;

import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

public class EarlyLecturesTask extends Task<Void> {

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<ModuleWrapper>> schedule;

    public EarlyLecturesTask(ConcurrentHashMap<String, CopyOnWriteArrayList<ModuleWrapper>> schedule) {
        this.schedule = schedule;
    }

    @Override
    protected Void call() throws Exception {
        shiftLectures();
        return null;
    }

    private void shiftLectures() {
        ForkJoinPool.commonPool().invoke(new ShiftLectures_ForkJoin(schedule, new ArrayList<String>(schedule.keySet()), 0, 4));
    }
}
