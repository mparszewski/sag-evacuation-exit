package com.infrastructure;

import com.enums.Direction;
import com.models.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static lombok.AccessLevel.NONE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
        Point endPoint;
        switch (direction) {
            case UP -> endPoint = new Point(startX, startY - capacity + 1);
            case DOWN -> endPoint = new Point(startX, startY + capacity - 1);
            case LEFT -> endPoint = new Point(startX - capacity + 1, startY);
            case RIGHT -> endPoint = new Point(startX + capacity - 1, startY);
            default -> endPoint = new Point(startX, startY);
        }
        return endPoint;
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
}
