package com.group10.scheduler.accounts;

import com.group10.scheduler.room.Room;
import com.group10.scheduler.room.RoomManager;

public class Administrator {
	private String adminId;
	private String name;
	private String email;
	private RoomManager roomManager;
	
	//package private Constructor only will be called by chief 
	//the SHARED RoomManager is injected: all admins manage the same room list
	Administrator(String adminId,String name,String email,RoomManager roomManager){
		this.adminId=adminId;
		this.name=name;
		this.email=email;
		this.roomManager = roomManager;
	}
	//setters and getters
	public String getAdminId() {
		return adminId;
	}
	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@Override
	public String toString() {
		return "Administrator " + adminId + " (" + name + ", " + email + ")";
	}
	
	//calling functions from room manager class (
	public boolean addRoom(Room r) {
		return roomManager.addRoom(r);
	}
	
	public boolean enableRoom(String roomId) {
	    return roomManager.enableRoom(roomId);
	}
	
	public boolean disableRoom(String roomId) {
	    return roomManager.disableRoom(roomId);
	}
	
	public boolean closeRoom(String roomId) {
	    return roomManager.closeRoom(roomId);
	}

}