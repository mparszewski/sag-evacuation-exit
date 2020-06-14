package com.infrastructure;

import com.models.Point;
import lombok.*;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static lombok.AccessLevel.NONE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Room implements PointListing {

    private int id;

    @Getter(NONE)
    private int startX;

    @Getter(NONE)
    private int startY;

    @Getter(NONE)
    private int endX;

    @Getter(NONE)
    private int endY;

    public Point getStartPoint() {
        return new Point(startX, startY);
    }

    public Point getEndPoint() {
        return new Point(endX, endY);
    }

    @Override
    public List<Point> getPoints() {
        List<Point> points = newArrayList();
        for (int x = startX + 1; x < endX; x++) {
            for (int y = startY + 1; y < endY; y++) {
                points.add(new Point(x, y));
            }
        }
        return points;
    }
}
