package Schedulers;

import Algorithms.GoToCoG;
import PositionTransformations.RotationTransformation;
import RobotPaths.LinearPath;
import RobotPaths.RobotPath;
import Simulator.Robot;
import Util.Vector;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * An event for the simulator. Will be used to store what happened in the simulator.
 * For every timestamp that there is an event, a calculatedEvent will be generated and stored.
 */
public class CalculatedEvent {
    /**
     * Timastamp of the event
     */

    /**
     * A list of events which all happen at the same timestamp.
     */
    public List<Event> events;

    /**
     * Get the timestamp of the CalculatedEvent.
     */
    public double getTimestamp() {
        return events.get(0).t;
    }

    /**
     * The current position of the robot
     */
    public Vector[] positions;

    /**
     * The paths the robots are currently following.
     */
    public RobotPath[] robotPaths;

    public CalculatedEvent(List<Event> events, Vector[] positions, RobotPath[] paths) {
        if (events.isEmpty()) {
            throw new IllegalArgumentException("Events cannot be empty");
        }
        this.events = events;
        this.positions = positions;
        this.robotPaths = paths;
    }

    /**
     * Puts all the events in the history into a file which could be loaded into the simulator at a later time
     * @param file The file in which the events needs to be written
     * @param calculatedEvents List of events that needs to be in the file
     * @param robots Array of robots that are used in the events
     */
    public static void toFile(File file, List<CalculatedEvent> calculatedEvents, Robot[] robots) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            System.out.println(calculatedEvents.get(0).positions.length);
            fileWriter.write(Integer.toString(calculatedEvents.get(0).positions.length) + "\n");

            for (CalculatedEvent calculatedEvent: calculatedEvents) {
                StringBuilder positions = new StringBuilder();
                for (Vector pos: calculatedEvent.positions) {
                    positions.append(pos.x).append(",").append(pos.y).append(" ");
                }
                fileWriter.write(positions.toString().trim() + "\n");

                StringBuilder goals = new StringBuilder();
                for (RobotPath path: calculatedEvent.robotPaths) {
                    goals.append(path.end.x).append(",").append(path.end.y).append(" ");
                }
                fileWriter.write(goals.toString().trim()+ "\n");

                StringBuilder events = new StringBuilder();
                fileWriter.write(calculatedEvent.events.get(0).t + "\n");
                for (Event event: calculatedEvent.events) {
                    int robotIndex = Arrays.asList(robots).indexOf(event.r);
                    events.append(robotIndex).append(",").append(event.type).append(" ");
                }
                fileWriter.write(events.toString().trim()+ "\n\n");
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("path cannot be opened: " + file.getPath());
        }
    }

    /**
     * puts the events from the file into a list of calculatedEvent objects
     * @param file absolute path to the file to read from
     * @return the event history in a List of CalculatedEvent objects
     */
    public static List<CalculatedEvent> fromFile(File file) {
        List<CalculatedEvent> calculatedEvents = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(file);
            int NROF_ROBOTS = Integer.parseInt(scanner.nextLine());
            while (scanner.hasNext()) {
                String positionsString = scanner.nextLine();
                String[] positionStrings = positionsString.split(" ");
                Vector[] positions = new Vector[NROF_ROBOTS];
                Robot[] robots = new Robot[NROF_ROBOTS];
                for (int i = 0; i < positionStrings.length; i++) {
                    String[] coordinatesString = positionStrings[i].split(",");
                    double x = Double.parseDouble(coordinatesString[0]);
                    double y = Double.parseDouble(coordinatesString[1]);
                    Vector position = new Vector(x, y);
                    positions[i] = position;
                    Robot robot = new Robot(i, new GoToCoG(), position, new RotationTransformation());
                    robots[i] = robot;
                }

                String goalsString = scanner.nextLine();
                String[] goalStrings = goalsString.split(" ");
                Vector[] goals = new Vector[NROF_ROBOTS];
                for (int i = 0; i < goalStrings.length; i++) {
                    String[] coordinatesString = goalStrings[i].split(",");
                    double x = Double.parseDouble(coordinatesString[0]);
                    double y = Double.parseDouble(coordinatesString[1]);
                    Vector goal = new Vector(x, y);
                    goals[i] = goal;
                }

                double timestamp = Double.parseDouble(scanner.nextLine());

                String eventsString = scanner.nextLine();
                String[] eventStrings = eventsString.split(" ");
                ArrayList<Event> events = new ArrayList<>();

                for (String eventString: eventStrings) {
                    String[] splitEventStrings = eventString.split(",");
                    Robot robot = robots[Integer.parseInt(splitEventStrings[0])];
                    EventType type = EventType.valueOf(splitEventStrings[1]);
                    Event event = new Event(type, timestamp, robot);
                    events.add(event);
                }
                scanner.nextLine();
                RobotPath[] robotPaths = new RobotPath[positions.length];
                for (int i = 0; i < positions.length; i++) {
                    robotPaths[i] = new LinearPath(positions[i], goals[i]);
                }
                CalculatedEvent calculatedEvent = new CalculatedEvent(events, positions, robotPaths);
                calculatedEvents.add(calculatedEvent);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println(String.format("File not found: %s", file.getPath()));
            System.err.println("Does it exists and does it have read access?");
        }catch (NumberFormatException e) {
            System.err.println("wrong format in file" + e.getMessage());
        }
        return calculatedEvents;
    }

    public CalculatedEvent copyDeep() {
        List<Event> eventsCopy = new ArrayList<>();
        for (Event event : this.events) {
            eventsCopy.add(event.copyEvent());
        }
        Vector[] positionsCopy = new Vector[this.positions.length];
        int i = 0;
        for (Vector vector : this.positions) {
            positionsCopy[i] = new Vector(this.positions[i]);
            i++;
        }
        return new CalculatedEvent(eventsCopy, positionsCopy, this.robotPaths);
    }
}
