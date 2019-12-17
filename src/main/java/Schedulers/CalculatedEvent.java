package Schedulers;

import Algorithms.BasicPositionTransformation;
import Algorithms.GoToCoG;
import Algorithms.Robot;
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
     * The current position of the robot
     */
    public Vector[] positions;

    /**
     * The position the robot wants to go to. If the robot is not moving,
     * this will be the same as {@link CalculatedEvent#positions}
     */
    public Vector[] goals;

    public CalculatedEvent(List<Event> events, Vector[] positions, Vector[] goals) {
        if (events.isEmpty()) {
            throw new IllegalArgumentException("Events cannot be empty");
        }
        this.events = events;
        this.positions = positions;
        this.goals = goals;
    }

    public static void toFile(String absolutePath, List<CalculatedEvent> calculatedEvents, Robot[] robots) {
        try {
            FileWriter fileWriter = new FileWriter(absolutePath);
            System.out.println(calculatedEvents.get(0).positions.length);
            fileWriter.write(Integer.toString(calculatedEvents.get(0).positions.length) + "\n");

            for (CalculatedEvent calculatedEvent: calculatedEvents) {
                StringBuilder positions = new StringBuilder();
                for (Vector pos: calculatedEvent.positions) {
                    positions.append(pos.x).append(",").append(pos.y).append(" ");
                }
                fileWriter.write(positions.toString().trim() + "\n");

                StringBuilder goals = new StringBuilder();
                for (Vector goal: calculatedEvent.goals) {
                    goals.append(goal.x).append(",").append(goal.y).append(" ");
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
            System.err.println("path cannot be opened: " + absolutePath);
        }
    }

    public static List<CalculatedEvent> fromFile(String absolutePath) {
        List<CalculatedEvent> calculatedEvents = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(absolutePath));
            int NROF_ROBOTS = Integer.parseInt(scanner.nextLine());
            while (scanner.hasNext()) {
                String positionsString = scanner.nextLine();
                String[] positionStrings = positionsString.split(" ");
                Vector[] positions = new Vector[NROF_ROBOTS];
                Robot[] robots = new Robot[NROF_ROBOTS];
                int i = 0;
                for (String positionString: positionStrings) {
                    String[] coordinatesString = positionString.split(",");
                    double x = Double.parseDouble(coordinatesString[0]);
                    double y = Double.parseDouble(coordinatesString[1]);
                    Vector position = new Vector(x, y);
                    positions[i] = position;
                    Robot robot = new Robot(new GoToCoG(), position, new BasicPositionTransformation());
                    robots[i] = robot;
                    i++;
                }

                String goalsString = scanner.nextLine();
                String[] goalStrings = goalsString.split(" ");
                Vector[] goals = new Vector[NROF_ROBOTS];
                int j = 0;
                for (String goalString: goalStrings) {
                    String[] coordinatesString = goalString.split(",");
                    double x = Double.parseDouble(coordinatesString[0]);
                    double y = Double.parseDouble(coordinatesString[1]);
                    Vector goal = new Vector(x, y);
                    goals[j] = goal;
                    j++;
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
                CalculatedEvent calculatedEvent = new CalculatedEvent(events, positions, goals);
                calculatedEvents.add(calculatedEvent);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println(String.format("File not found: %s", absolutePath));
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
        return new CalculatedEvent(eventsCopy, positionsCopy, this.goals);
    }
}
