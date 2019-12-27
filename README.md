# Simulator for Oblivious Robots

This project features a simulator for oblivious point robots.

It contains:

- Framework code that can be used to implement new robot control algorithms.
- A headless, event-based simulator that can be used to simulate a number of robots interacting with eachother.
- A GUI that can be used to visualise a simulation, including replaying the simulation through time, editing the timeline with new events, etc...

## Usage

This project can be built through the Gradle build system through the command `gradle jar`, which will produce a jar-file under `build/libs`.

This library can then be linked into a project of one's choice. It then contains the following classes that are immediately relevant:

- `Simulator` which represents a single simulation. To use it, construct an object, passing it an array of `Robot` instances, a set of configuration settings (such as whether robots can detect multilicity), and the scheduler to be used. The simulation can be controlled through the various methods in this class.

- `Robot` which represents a single robot during a simulation. Robots take a starting position within the simulation as well as:
    - An `Algorithm` that determines their behavior,which is an abstract class that features a function that maps from a set of positions (that represent what the robot sees) to a single position which tells the simulator where the robot wishes to move.

    - `PositionTransformation` which optionally distorts or transforms the way the robot perceives the world. For instance, this allows one to test an algorithm's robustness against robots not sharing a common frame of reference.
    
- `Scheduler` which determines when robots perceive the world around them, when they move and, optionally, when they stop moving.
    
    4 Schedulers are provided:
    
    -   `FSyncScheduler`, which implements a fully-synchronous scheduler which guarantees that all robots look at the world at the same time and all stop moving at the same time.
    
    -   `SSyncScheduler`, which implements a semi-synchronous scheduler that behaves like the fully-synchronous scheduler, but without all robots being activated each cycle. 
    
    -   `AsyncScheduler` which implements a scheduler where robots can be activated and moved at unpredictable, unsynchronized times of unpredictable duration.
    
    -   `FileScheduler` which plays a pre-written schedule from a CSV file.
    
New schedulers can be written by extending the `Scheduler` abstract class.

- `GUI` which implements an optional graphical interface through which the simulation can be evaluated. It can be started by calling the static method `runGUI`, passing in a `Simulation` object to be visualized and interacted with.
    
## Simulation model

A simulation is, for each robot, an ordered sequence of events. Each event has a time at which it occurs as well as a type. The type must be one of `START_COMPUTE, START_MOVING, END_MOVING`, where the type of each subsequent event must be the logical successor of the one before, starting with `START_COMPUTE`, where the robot will receive a snapshot of the robots at that time and the algorithm is evaluated, `START_MOVING` where the robot will start moving towards the target location determined by the algorithm, and `END_MOVING` where the robot's movement stops. After this, the cycle restarts with a `START_COMPUTE` step.

Up to the precision of floating-point numbers, the simulation representation effectively has continuous-time semantics, where events can be scheduled at arbitrary times without being restricted by any kind of fixed time step.

Also, note that this representation does not have dedicated "Look", "Compute" and "Move" periods. Instead, "Look" and "Compute" are represented as a single, instantaneous `START_COMPUTE` event. Any time spent on computations is then modeled by the time between the `START_COMPUTE` and `START_MOVING` events. The actual time spent on evaluating the algorithm does not affect the outcome of the simulation, which is deterministic as long as the algorithm, scheduler and other user-defined components are.