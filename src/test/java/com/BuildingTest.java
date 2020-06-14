package com;

import com.infrastructure.Building;
import com.infrastructure.Fire;
import com.models.Point;
import org.junit.Before;
import org.junit.Test;

import static com.enums.InfrastructureElement.*;
import static com.infrastructure.Building.getBuilding;
import static com.infrastructure.Fire.getFire;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BuildingTest {

    private Building building;

    @Before
    public void setUp() throws Exception {
        building = getBuilding();
    }

    @Test
    public void shouldPointsToFloor() {
        Point floor1 = new Point(12, 5);
        Point floor2 = new Point(6, 1);
        Point floor3 = new Point(3, 8);
        assertEquals(FLOOR, building.getElementAtPoint(floor1));
        assertEquals(FLOOR, building.getElementAtPoint(floor2));
        assertEquals(FLOOR, building.getElementAtPoint(floor3));
    }

    @Test
    public void shouldPointsToWall() {
        Point wall = new Point(5, 17);
        assertEquals(WALL, building.getElementAtPoint(wall));
    }

    @Test
    public void shouldPointsToDoor() {
        Point door1 = new Point(9, 0);
        Point door2 = new Point(10, 0);
        Point door3 = new Point(11, 0);
        assertEquals(WALL, building.getElementAtPoint(door1));
        assertEquals(WALL, building.getElementAtPoint(door2));
        assertEquals(WALL, building.getElementAtPoint(door3));
    }

    @Test
    public void shouldPointsToObstruction() {
        Point obstruction1 = new Point(3, 3);
        Point obstruction2 = new Point(3, 5);
        Point obstruction3 = new Point(3, 7);
        assertEquals(OBSTRUCTION, building.getElementAtPoint(obstruction1));
        assertEquals(OBSTRUCTION, building.getElementAtPoint(obstruction2));
        assertEquals(OBSTRUCTION, building.getElementAtPoint(obstruction3));
    }

    @Test
    public void testFireSpread() {
        Fire fire = getFire();
        Point firePoint = fire.getStartPoint();
        assertEquals(FIRE, building.getElementAtPoint(firePoint));
        assertEquals(1, fire.getPoints().size());
        fire.spreadRandomly();
        fire.spreadRandomly();
        fire.spreadRandomly();
        assertTrue(fire.getPoints().size() > 1);
    }

}
