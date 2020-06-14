package com.infrastructure;

import com.enums.InfrastructureElement;
import com.google.common.collect.ImmutableSet;
import com.models.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static com.enums.InfrastructureElement.*;
import static com.google.common.collect.Lists.newArrayList;
import static com.infrastructure.Fire.getFire;
import static com.utility.YamlParser.parseFromYaml;
import static lombok.AccessLevel.NONE;

@Getter
@Setter
@AllArgsConstructor
public class Building implements PointListing {

    private static Building building;

    private Building() {
    }

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
        return FLOOR.equals(element) || WINDOW.equals(element) || DOOR.equals(element);
    }

    private InfrastructureElement getElementAtPoint(Point point) {
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
        for (Room room : rooms) {
            if (room.getPoints().contains(point)) {
                return FLOOR;
            }
        }
        for (Obstruction obstruction : obstructions) {
            if (obstruction.getPoints().contains(point)) {
                return OBSTRUCTION;
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
}
