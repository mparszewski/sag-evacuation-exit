package com.infrastructure;

import com.models.Point;

import java.util.List;

import static java.util.stream.Collectors.joining;

public interface PointListing {

    List<Point> getPoints();

    default String getStringRepresentation() {
        return getPoints().stream()
                .map(Point::toString)
                .collect(joining(", "));
    }

}
