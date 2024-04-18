package com.example.cs4076_server;

import javafx.application.Platform;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.time.DayOfWeek;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A runnable object for handling a single connection
 */
class ConnectionThread implements Runnable {
    /**
     * The link this thread is managing
     */
    private final Socket link;
    /**
     * The server's shared schedule
     */
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<ModuleWrapper>> schedule;
    /**
     * The status of the early lectures task
     */
    private String earlyLectureStatus = "Not Started";

    public ConnectionThread(Socket link, ConcurrentHashMap<String, CopyOnWriteArrayList<ModuleWrapper>> schedule) {
        this.link = link;
        this.schedule = schedule;
    }

    @Override
    public void run() {
        // To use javafx.concurrent, the JFX runtime must be started
        Platform.startup(() -> {
        });

        try {
            while (true) {
                // Get I/O streams for sending and receiving objects
                ObjectInputStream in = new ObjectInputStream(link.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(link.getOutputStream());

                // Read in the client's message
                JSONObject obj = (JSONObject) in.readObject();
                JSONObject data = (JSONObject) obj.get("data");
                String resMsg;

                // Determine the server's response
                try {
                    if (obj.get("action").equals("STOP")) {
                        resMsg = "TERMINATE";
                    } else {
                        resMsg = processMsg((String) obj.get("action"), data);
                    }
                } catch (IncorrectActionException e) {
                    resMsg = e.getMessage();
                }

                // Send the response
                JSONObject res = new JSONObject();
                res.put("response", resMsg);
                res.put("earlyLectureStatus", earlyLectureStatus);
                out.writeObject(res);

                // Close the connection if requested
                if (resMsg.equals("TERMINATE")) {
                    try {
                        link.close();
                        break;
                    } catch (IOException e) {
                        System.out.println("Unable to disconnect!");
                        System.exit(1);
                    }
                }

            }
        } catch (SocketException e) {
            if (e.getMessage().equals("Connection reset")) {
                System.out.println("Client disconnected.");
            } else {
                e.printStackTrace();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("\n* Closing connection... *");
                link.close();
            } catch (IOException e) {
                System.out.println("Unable to disconnect!");
                System.exit(1);
            }
            // Stop the JFX runtime
            Platform.exit();
        }
    }

    /**
     * Process client messages
     *
     * @param action The requested action
     * @param data   The given data to use with the action
     * @return The server's response message
     * @throws IncorrectActionException Thrown when an invalid action-data pair is received
     */
    private String processMsg(String action, JSONObject data) throws IncorrectActionException {
        String response = "";
        if (action.equals("Display Schedule")) {
            response = displaySchedule();
            //response = "Schedule displayed";
        } else if (action.equals("Add Class") || action.equals("Remove Class")) {
            // Class data is required for adding and removing
            if (data == null) {
                throw new IncorrectActionException("Cannot " + action + ". Missing data.");
            }

            ModuleWrapper newModule = new ModuleWrapper(data);

            // Validate that the module ends after it starts
            if (newModule.getStartTime().isAfter(newModule.getEndTime()) || newModule.getStartTime().equals(newModule.getEndTime())) {
                return "Start time must be before end time.";
            }

            if (action.equals("Add Class")) {
                response = addClass(newModule);
            } else if (action.equals("Remove Class")) {
                response = removeClass(newModule);
            }
        } else if (action.equals("Early Lectures")) {
            // Create a task using javafx.concurrent to decouple computation
            EarlyLecturesTask task = new EarlyLecturesTask(schedule);
            task.setOnSucceeded(e -> {
                // Update the status
                earlyLectureStatus = "Finished";
            });

            // Run the task
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            executorService.execute(task);
            executorService.shutdown();

            earlyLectureStatus = "Started";
            response = "Early lectures task started";
        } else {
            throw new IncorrectActionException("Unknown action.");
        }
        return response;
    }

    /**
     * Displays the created schedule in the terminal and returns a JSON string of the schedule to be sent to the client
     */
    private String displaySchedule() {
        System.out.println("SCHEDULE:");
        System.out.print("Day         Start       End         Name        Room Number\n");
        System.out.println("-------------------------------------------------------------------");
        for (DayOfWeek day : DayOfWeek.values()) {
            for (ModuleWrapper module : schedule.get(day.toString())) {
                System.out.printf("%-12s%-12s%-12s%-12s%-12s\n", module.getDayOfWeek(), module.getStartTime(), module.getEndTime(), module.getName(), module.getRoomNumber());
            }
        }
        System.out.println();
        return JSONObject.toJSONString(schedule);
    }

    /**
     * Adds a module to the schedule, checking for clashes before adding
     *
     * @param newModule The module to add
     * @return The response message
     */
    private String addClass(ModuleWrapper newModule) {
        String result = "";
        boolean canAdd = true;
        CopyOnWriteArrayList<ModuleWrapper> moduleArray = schedule.get(newModule.getDayOfWeek());

        // Check each existing module to see if this new module already exists or clashes with an existing module
        for (ModuleWrapper moduleWrapper : moduleArray) {
            if (moduleWrapper.equals(newModule)) {
                result = "Cannot add class. Class already exists.";
                canAdd = false;
                break;
            } else if (moduleWrapper.overlaps((newModule))) {
                result = "Cannot add class. Class overlaps with existing class.";
                canAdd = false;
                break;
            }
        }

        if (canAdd) {
            moduleArray.add(newModule);

            // sort the array by day then time
            moduleArray.sort((o1, o2) -> {

                if (o1.equals(o2)) {
                    return 0;
                }

                int dayCompare = DayOfWeek.valueOf(o1.getDayOfWeek()).compareTo(DayOfWeek.valueOf(o2.getDayOfWeek()));
                if (dayCompare > 0) {
                    return 1;
                } else if (dayCompare < 0) {
                    return -1;
                } else {
                    if (o1.getStartTime().isBefore(o2.getStartTime())) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });

            result = newModule.getName() + " at " + newModule.getStartTime() + " successfully added.";
        }
        return result;
    }

    /**
     * Removes an existing module from the schedule
     *
     * @param newModule The module to remove
     * @return The response message
     */
    private String removeClass(ModuleWrapper newModule) {
        // Find the class and delete it if it exists
        String response = "Cannot remove class. Class doesn't exist";
        CopyOnWriteArrayList<ModuleWrapper> moduleArray = schedule.get(newModule.getDayOfWeek());
        for (int i = 0; i < moduleArray.size(); i++) {
            if (moduleArray.get(i).equals(newModule)) {
                moduleArray.remove(i);
                response = "Time slot freed on " + newModule.getDayOfWeek() + " from " + newModule.getStartTime() + " to " + newModule.getEndTime() + " in " + newModule.getRoomNumber() + ".";
                break;
            }
        }
        return response;
    }
}
