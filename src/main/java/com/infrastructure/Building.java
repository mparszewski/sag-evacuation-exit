package com.infrastructure;

import com.enums.InfrastructureElement;
import com.google.common.collect.ImmutableSet;
import com.models.Point;
import lombok.*;

import java.util.List;
import java.util.Random;

import static com.enums.InfrastructureElement.*;
import static com.google.common.collect.Lists.newArrayList;
import static com.infrastructure.Fire.getFire;
import static com.utility.YamlParser.parseFromYaml;
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

    public InfrastructureElement getElementAtPoint(Point point) {
        if (getPoints().contains(point)) {
            return WALL;
        }
        if (getFire().getPoints().contains(point)) {
            return FIRE;
        }
        for (Door door : doors) {
            if (door.getPoints().contains(point)) {
                return DOOR;
            }
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
}
