package com.infrastructure;

import com.models.Point;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Obstruction implements PointListing {
    private int startX;
    private int startY;
    private int endX;
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
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                points.add(new Point(x, y));
            }
        }
        return points;
    }
}
