package com.infrastructure;

import com.enums.Direction;
import com.models.Point;
import io.vavr.API;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.enums.Direction.*;
import static com.infrastructure.Building.getBuilding;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static java.lang.Math.random;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.NONE;

@Getter
@Setter
public class Fire implements PointListing {

    private Point startPoint;
    private CopyOnWriteArrayList<Point> allPoints;

    private Fire() {
        startPoint = getBuilding().getRandomAvailablePoint();
        allPoints = new CopyOnWriteArrayList<>();
        allPoints.add(startPoint);
    }

    public static Fire fire;

    @Getter(NONE)
    private final Consumer<Point> RANDOM_SPREAD = point -> {
        Stream.of(Direction.values())
                .filter(d -> random() > 0.5)
                .forEach(direction -> addPoint(point, direction));
    };

    public static Fire getFire() {
        if (isNull(fire)) {
            fire = new Fire();
        }
        return fire;
    }

    @Override
    public List<Point> getPoints() {
        return allPoints;
    }

    private void addPoint(Point point, Direction direction) {
        Point newPoint = API.Match(direction).of(
                Case($(UP), new Point(point.getX() - 1, point.getY())),
                Case($(DOWN), new Point(point.getX() + 1, point.getY())),
                Case($(LEFT), new Point(point.getX(), point.getY() - 1)),
                Case($(RIGHT), new Point(point.getX() - 1, point.getY() + 1)),
                Case($(), point)
        );
        if (getBuilding().isAvailableForFire(newPoint)) {
            allPoints.add(newPoint);
        }
    }

    public void spreadRandomly() {
        allPoints.forEach(RANDOM_SPREAD);
    }
}
