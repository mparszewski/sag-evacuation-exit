package com.infrastructure;

import com.enums.Coordinates;
import com.enums.FireRelation;
import com.enums.InfrastructureElement;
import com.google.common.collect.ImmutableSet;
import com.models.Point;
import io.vavr.collection.Stream;
import lombok.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.enums.Coordinates.X;
import static com.enums.Coordinates.Y;
import static com.enums.FireRelation.*;
import static com.enums.InfrastructureElement.*;
import static com.google.common.collect.Lists.newArrayList;
import static com.infrastructure.Fire.getFire;
import static com.utility.YamlParser.parseFromYaml;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode
public class Building implements PointListing {

    private static Building building;

    public static Building getBuilding() {
        if (building == null) {
            building = parseFromYaml("src/main/resources/building.yml");
        }
        return building;
    }

    @Getter(NONE)
    private int endX;

    @Getter(NONE)
    private int endY;

    private List<Room> rooms;
    private List<Door> doors;
    private List<Obstruction> obstructions;
    private List<Point> agents;

    public void updatePoint(Point oldPoint, Point newPoint) {
        setAgents(agents.stream().map(point -> {
            if (point.equals(oldPoint)) {
                return newPoint;
            } else {
                return point;
            }
        }).collect(toList()));
    }

    public Point getStartPoint() {
        return new Point(0, 0);
    }

    public Point getEndPoint() {
        return new Point(endX, endY);
    }

    public boolean isPointAvailable(Point point) {
        InfrastructureElement element = getElementAtPoint(point);
        return FLOOR == element || WINDOW == element || DOOR == element;
    }

    public boolean isHumanThere(Point point) {
        return getElementAtPoint(point) == HUMAN;
    }

    public FireRelation checkIfOnFire(Point humanPoint) {
        if (getFire().getAllPoints().contains(humanPoint)) {
            return ON_FIRE;
        } else if (Stream.ofAll(getFire().getAllPoints()).exists(point -> arePointsNearby(point, humanPoint))) {
            return NEAR_FIRE;
        } else {
            return FAR_FROM_FIRE;
        }
    }

    public InfrastructureElement getElementAtPoint(Point point) {
        for (Door door : doors) {
            if (door.getPoints().contains(point)) {
                return DOOR;
            }
        }
        if (getPoints().contains(point)) {
            return WALL;
        }
        if (getAgents().contains(point)) {
            return HUMAN;
        }
        if (getFire().getPoints().contains(point)) {
            return FIRE;
        }
        for (Obstruction obstruction : obstructions) {
            if (obstruction.getPoints().contains(point)) {
                return OBSTRUCTION;
            }
        }
        for (Room room : rooms) {
            if (room.getPoints().contains(point)) {
                return FLOOR;
            }
        }
        return WALL;
    }

    private boolean arePointsNearby(Point point1, Point point2) {
        return Math.abs(point1.getX() - point2.getX()) <= 1
                && Math.abs(point1.getY() - point2.getY()) <= 1;
    }


    @Override
    public List<Point> getPoints() {
        List<Point> points = newArrayList();
        Point startPoint = getStartPoint();
        for (int x = startPoint.getX(); x <= endX; x++) {
            points.add(new Point(x, startPoint.getY()));
            points.add(new Point(x, endY));
        }
        for (int y = startPoint.getY(); y <= endY; y++) {
            points.add(new Point(startPoint.getX(), y));
            points.add(new Point(endX, y));
        }
        return ImmutableSet.copyOf(points).asList();
    }

    public Point getRandomAvailablePoint() {
        Random random = new Random();
        Room room = rooms.get(random.nextInt(rooms.size()));
        return room.getPoints().get(random.nextInt(room.getPoints().size()));
    }

    public int getNumberOfDoors(Room room) {
        return getDoorIdsInRoom(room).size();
    }

    public List<DoorDistance> getVisibleDoors(Point point, double radius) {
        return getDoorsWithDistancesFromPoint(point).stream()
                .filter(doorDistance -> doorDistance.getDistance() < radius)
                .collect(toList());
    }

    public List<DoorDistance> getDoorsWithDistancesFromPoint(Point point) {
        return rooms.stream()
                .filter(room -> room.getPoints().contains(point))
                .limit(1)
                .map(this::getDoorIdsInRoom)
                .flatMap(Collection::stream)
                .map(this::getDoorById)
                .map(door -> new DoorDistance(door, countDistance(point, door.getDoorCenter())))
                .sorted(comparing(DoorDistance::getDistance))
                .collect(toList());
    }

    public DoorDistance getClosestDoorDistance(Point point) {
        return getDoorsWithDistancesFromPoint(point).get(0);
    }

    public DoorDistance getDoorDistance(Point point, int doorId) {
        return getDoorsWithDistancesFromPoint(point).stream()
                .filter(doorDistance -> doorDistance.getDoor().getId() == doorId)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public Room getRoomByPoint(Point point) {
        return rooms.stream()
                .filter(room -> room.getPoints().contains(point))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public Transfer getDoorTransferInGivenRoom(Door door, int roomId) {
        return door.getTransfers().stream()
                .filter(transfer -> transfer.getFrom() == roomId)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public Room getRoomById(int id) {
        return rooms.stream()
                .filter(room -> id == room.getId())
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    public Door getDoorById(int id) {
        return doors.stream()
                .filter(door -> id == door.getId())
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    private List<Integer> getDoorIdsInRoom(Room room) {
        return doors.stream()
                .filter(door -> door.getTransfers().stream()
                        .map(Transfer::getFrom)
                        .anyMatch(fromId -> fromId == room.getId()))
                .map(Door::getId)
                .collect(toList());
    }

    public Door getDoorByPoint(Point point) {
        return doors.stream()
                .filter(door -> door.getPoints().contains(point))
                .findAny()
                .orElse(null);
    }

    private double countDistance(Point a, Map<Coordinates, Double> doorCenter) {
        return sqrt(pow((double) a.getX() - doorCenter.get(X), 2) + pow((double) a.getY() - doorCenter.get(Y), 2));
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public static class DoorDistance {
        private Door door;
        private Double distance;
    }
}
