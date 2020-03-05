package nl.tue.oblotsim.Schedulers;

import nl.tue.oblotsim.Simulator.Robot;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An event for the simulator. Will be used to store what happened in the simulator.
 * For every timestamp that there is an event, a calculatedEvent will be generated and stored.
 */
public class CalculatedEvent implements Serializable {

    private List<Event> events;

    /**
     * Get the timestamp of the CalculatedEvent.
     */
    public double getTimestamp() {
        return events.get(0).getT();
    }

    public CalculatedEvent(List<Event> events, Collection<Robot> snapshot) {
        // Defensive copy of the list of events.
        this.events = List.copyOf(events);
        this.snapshot = snapshot.stream().collect(Collectors.toMap(Robot::getId, robot -> robot));
    }

    private final Map<Integer, Robot> snapshot;

//
//    /**
//     * Puts all the events in the history into a file which could be loaded into the simulator at a later time
//     * @param file The file in which the events needs to be written
//     * @param calculatedEvents List of events that needs to be in the file
//     * @param robots Array of robots that are used in the events
//     */
//    public static void toFile(File file, List<CalculatedEvent> calculatedEvents, List<Robot> robots) {
//        try {
//            FileWriter fileWriter = new FileWriter(file);
//            System.out.println(calculatedEvents.get(0).positions.length);
//            fileWriter.write(Integer.toString(calculatedEvents.get(0).positions.length) + "\n");
//
//            for (CalculatedEvent calculatedEvent: calculatedEvents) {
//                StringBuilder positions = new StringBuilder();
//                for (Vector pos: calculatedEvent.positions) {
//                    positions.append(pos.x).append(",").append(pos.y).append(" ");
//                }
//                fileWriter.write(positions.toString().trim() + "\n");
//
//                StringBuilder goals = new StringBuilder();
//                for (RobotPath path: calculatedEvent.robotPaths) {
//                    goals.append(path.end.x).append(",").append(path.end.y).append(" ");
//                }
//                fileWriter.write(goals.toString().trim()+ "\n");
//
//                StringBuilder events = new StringBuilder();
//                fileWriter.write(calculatedEvent.events.get(0).t + "\n");
//                for (Event event: calculatedEvent.events) {
//                    int robotIndex = Arrays.asList(robots).indexOf(event.r);
//                    events.append(robotIndex).append(",").append(event.type).append(" ");
//                }
//                fileWriter.write(events.toString().trim()+ "\n\n");
//            }
//            fileWriter.flush();
//            fileWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.err.println("path cannot be opened: " + file.getPath());
//        }
//    }
//
//    /**
//     * puts the events from the file into a list of calculatedEvent objects
//     * @param file absolute path to the file to read from
//     * @return the event history in a List of CalculatedEvent objects
//     */
//    public static List<CalculatedEvent> fromFile(File file) {
//        List<CalculatedEvent> calculatedEvents = new ArrayList<>();
//        try {
//            Scanner scanner = new Scanner(file);
//            int NROF_ROBOTS = Integer.parseInt(scanner.nextLine());
//            while (scanner.hasNext()) {
//                String positionsString = scanner.nextLine();
//                String[] positionStrings = positionsString.split(" ");
//                Vector[] positions = new Vector[NROF_ROBOTS];
//                List<Robot> robots = new Robot[NROF_ROBOTS];
//                for (int i = 0; i < positionStrings.length; i++) {
//                    String[] coordinatesString = positionStrings[i].split(",");
//                    double x = Double.parseDouble(coordinatesString[0]);
//                    double y = Double.parseDouble(coordinatesString[1]);
//                    Vector position = new Vector(x, y);
//                    positions[i] = position;
//                    Robot robot = new Robot(i, new GoToCoG(), position, new RotationTransformation());
//                    robots[i] = robot;
//                }
//
//                String goalsString = scanner.nextLine();
//                String[] goalStrings = goalsString.split(" ");
//                Vector[] goals = new Vector[NROF_ROBOTS];
//                for (int i = 0; i < goalStrings.length; i++) {
//                    String[] coordinatesString = goalStrings[i].split(",");
//                    double x = Double.parseDouble(coordinatesString[0]);
//                    double y = Double.parseDouble(coordinatesString[1]);
//                    Vector goal = new Vector(x, y);
//                    goals[i] = goal;
//                }
//
//                double timestamp = Double.parseDouble(scanner.nextLine());
//
//                String eventsString = scanner.nextLine();
//                String[] eventStrings = eventsString.split(" ");
//                ArrayList<Event> events = new ArrayList<>();
//
//                for (String eventString: eventStrings) {
//                    String[] splitEventStrings = eventString.split(",");
//                    Robot robot = robots[Integer.parseInt(splitEventStrings[0])];
//                    EventType type = EventType.valueOf(splitEventStrings[1]);
//                    Event event = new Event(type, timestamp, robot);
//                    events.add(event);
//                }
//                scanner.nextLine();
//                RobotPath[] robotPaths = new RobotPath[positions.length];
//                for (int i = 0; i < positions.length; i++) {
//                    robotPaths[i] = new LinearPath(positions[i], goals[i]);
//                }
//                CalculatedEvent calculatedEvent = new CalculatedEvent(events, positions, robotPaths);
//                calculatedEvents.add(calculatedEvent);
//            }
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            System.err.println(String.format("File not found: %s", file.getPath()));
//            System.err.println("Does it exists and does it have read access?");
//        }catch (NumberFormatException e) {
//            System.err.println("wrong format in file" + e.getMessage());
//        }
//        return calculatedEvents;
//    }

    /**
     * Find out if this calculated event has an event for a specific robot.
     * @param robot the robot to look for
     * @return true if there is an event for this robot, false otherwise
     */
    public boolean containsRobot(Robot robot) {
        for (Event event : events) {
            if (event.getTargetId() == robot.getId()) {
                return true;
            }
        }

        return false;
    }


    /**
     * Immutable reference to list of events that occurred in this event.
     */
    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /**
     * Immutable map from robot ID to robot.
     */
    public Map<Integer, Robot> getSnapshot() {
        return Collections.unmodifiableMap(snapshot);
    }

    /**
     * @return Timestamp after which all robots that are moving after this event will have stopped moving.
     *         Will be the event's timestamp itself if they already stopped.
     */
    public double timeUntilAllStop() {
        return snapshot.values().stream().flatMapToDouble(robot -> robot.willStopBefore().stream()).max().orElse(getTimestamp());
    }
}
