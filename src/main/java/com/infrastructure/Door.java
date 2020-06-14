package com.infrastructure;

import com.enums.Direction;
import com.models.Point;
import io.vavr.API;
import lombok.*;

import java.util.List;

import static com.enums.Direction.*;
import static com.google.common.collect.Lists.newArrayList;
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
