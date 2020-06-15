package com.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Point {
    private int x;
    private int y;

    public Point(Point point) {
        this.setX(point.getX());
        this.setY(point.getY());
    }

    public Point up() {
        Point newPoint = new Point(this);
        newPoint.setY(newPoint.getY() + 1);
        return newPoint;
    }

    public Point down() {
        Point newPoint = new Point(this);
        newPoint.setY(newPoint.getY() - 1);
        return newPoint;
    }

    public Point right() {
        Point newPoint = new Point(this);
        newPoint.setX(newPoint.getX() + 1);
        return newPoint;
    }

    public Point left() {
        Point newPoint = new Point(this);
        newPoint.setX(newPoint.getX() - 1);
        return newPoint;
    }

    @Override
    public String toString() {
        return "(" + x +", " + y +")";
    }
}
