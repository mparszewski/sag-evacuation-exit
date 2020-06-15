package com.infrastructure;

import com.enums.Coordinates;
import com.enums.Direction;
import com.enums.TransferType;
import com.google.common.collect.Maps;
import com.models.Point;
import io.vavr.API;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.enums.Coordinates.*;
import static com.enums.Direction.*;
import static com.enums.Direction.*;
import static com.enums.TransferType.EXIT_SIGNED;
import static com.enums.TransferType.EXIT_UNSIGNED;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.*;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static lombok.AccessLevel.NONE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Door implements PointListing {

    private int id;

    @Getter(NONE)
    private int startX;

    @Getter(NONE)
    private int startY;

    @Getter(NONE)
    private Direction direction;

    private int capacity;
    private List<Transfer> transfers;

    public Point getStartPoint() {
        return new Point(startX, startY);
    }

    public Point getEndPoint() {
        return API.Match(direction).of(
                Case($(UP), new Point(startX, startY - capacity + 1)),
                Case($(DOWN), new Point(startX, startY + capacity - 1)),
                Case($(LEFT), new Point(startX - capacity + 1, startY)),
                Case($(RIGHT), new Point(startX + capacity - 1, startY)),
                Case($(), new Point(startX, startY))
        );
    }

    public boolean isExitDoor() {
        TransferType type = this.getTransfers().get(0).getType();
        return type == EXIT_SIGNED || type == EXIT_UNSIGNED;
    }

    @Override
    public List<Point> getPoints() {
        Point endPoint = getEndPoint();
        List<Point> points = newArrayList();
        for (int x = startX; x <= endPoint.getX(); x++) {
            for (int y = startY; y <= endPoint.getY(); y++) {
                points.add(new Point(x, y));
            }
        }
        return points;
    }

    public Map<Coordinates, Double> getDoorCenter() {
        Map<Coordinates, Double> doorCenterMap = newHashMap();
        Point endPoint = getEndPoint();
        if (direction == UP || direction == DOWN) {
            doorCenterMap.put(X, (double) startX);
            doorCenterMap.put(Y, mean(startY, endPoint.getY()));
        } else if (direction == LEFT || direction == RIGHT) {
            doorCenterMap.put(X, mean(startX, endPoint.getX()));
            doorCenterMap.put(Y, (double) startY);
        }
        return doorCenterMap;
    }

    private double mean(int i, int j) {
        return ((double) (i + j)) / 2;
    }
}
