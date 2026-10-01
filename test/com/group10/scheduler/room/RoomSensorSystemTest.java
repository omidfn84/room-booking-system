package com.group10.scheduler.room;

import static org.junit.Assert.*;
import org.junit.*;

public class RoomSensorSystemTest{

    private RoomSensorSystem sensor;

    @Before
    public void setUp (){
        sensor= new RoomSensorSystem ("R100-sensor");
    }

    @Test
    public void sensorId (){
        assertEquals ("R100-sensor", sensor.getSensorId ());
    }

    @Test
    public void emptyRoom (){
        assertFalse (sensor.detectOccupancy ());
    }

    @Test
    public void validBadge (){
        assertTrue (sensor.scanIDBadge ("badge-123"));
    }

    @Test
    public void badgeSetsOccupied (){
        sensor.scanIDBadge ("badge-123");
        assertTrue (sensor.detectOccupancy ());
    }

    @Test
    public void nullBadge (){
        assertFalse (sensor.scanIDBadge (null));
    }

    @Test
    public void nullBadgeKeepsEmpty (){
        sensor.scanIDBadge (null);
        assertFalse (sensor.detectOccupancy ());
    }

    @Test
    public void blankBadge (){
        assertFalse (sensor.scanIDBadge ("   "));
    }

    @Test
    public void emptyBadge (){
        assertFalse (sensor.scanIDBadge (""));
    }

    @Test
    public void changeSensorId (){
        sensor.setSensorId ("R200-sensor");
        assertEquals ("R200-sensor", sensor.getSensorId ());
    }

    @Test
    public void setOccupied (){
        sensor.setOccupied (true);
        assertTrue (sensor.getOccupied ());
        sensor.setOccupied (false);
        assertFalse (sensor.getOccupied ());
    }

    @Test
    public void occupancyMatches (){
        sensor.scanIDBadge ("badge-1");
        assertEquals (sensor.detectOccupancy (), sensor.getOccupied ());
    }
}

