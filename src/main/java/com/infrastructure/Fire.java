package com.infrastructure;

import com.enums.Direction;
import com.google.common.collect.Lists;
import com.models.Point;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.infrastructure.Building.getBuilding;
import static java.lang.Math.random;
import static java.lang.Math.round;
import static lombok.AccessLevel.NONE;

@Getter
@Setter
public class Fire implements PointListing {

    private Point startPoint;
    private List<Point> allPoints;

    @Getter(NONE)
    private final Consumer<Point> RANDOM_SPREAD = point -> {
        Stream.of(Direction.values())
                .filter(d -> random() > 0.5)
                .forEach(direction -> addPoint(point, direction));
    };

    private Fire() {
        Building building = getBuilding();
        do {
            int startX = (int) round(building.getEndPoint().getX() * random());
            int startY = (int) round(building.getEndPoint().getY() * random());
            startPoint = new Point(startX, startY);
        }
        while (!getBuilding().isPointAvailable(startPoint));
        allPoints = Lists.newArrayList();
    }

    public static Fire fire;


    public static Fire getFire() {
        if (fire == null) {
            fire = new Fire();
        }
        return fire;
    }

    @Override
    public List<Point> getPoints() {
        return allPoints;
    }

    private void addPoint(Point point, Direction direction) {
        Point newPoint;
        switch (direction) {
            case UP -> newPoint = new Point(point.getX() - 1, point.getY());
            case DOWN -> newPoint = new Point(point.getX() + 1, point.getY());
            case LEFT -> newPoint = new Point(point.getX(), point.getY() - 1);
            case RIGHT -> newPoint = new Point(point.getX() - 1, point.getY() + 1);
            default -> newPoint = point;
        }
        if (getBuilding().isPointAvailable(newPoint)) {
            allPoints.add(newPoint);
        }
    }

    public void spreadRandomly() {
        allPoints.forEach(RANDOM_SPREAD);
    }
}
