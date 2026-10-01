package com.group10.scheduler.room;

public class RoomSensorSystem {
	
	private String sensorId;
    private boolean occupied=false;

    public RoomSensorSystem(String sensorId) {
        this.sensorId = sensorId;
    }
    
    // Simulates receiving occupancy data from a sensor

    public boolean detectOccupancy() {
        return occupied;
    }

    // Simulates scanning a badge
    public boolean scanIDBadge(String badgeId) {

        if (badgeId == null || badgeId.isBlank()) {
        	return false; 
        }
        this.occupied=true;

        return true;
    }
    public String getSensorId() {
    	return sensorId;
    }
    public void setSensorId(String sensorId) {
    	this.sensorId=sensorId;
    }
    public boolean getOccupied() {
    	return occupied;
    }
    public void setOccupied(boolean occupied) {
    	this.occupied=occupied;
    }
}