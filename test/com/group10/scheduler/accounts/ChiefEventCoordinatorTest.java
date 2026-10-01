package com.group10.scheduler.accounts;

import static org.junit.Assert.*;
import org.junit.Test;
import com.group10.scheduler.room.FakeRoomRepository;
import com.group10.scheduler.room.RoomManager;
/**
 * Tests administrator creation and the Singlaton instance.
 */
public class ChiefEventCoordinatorTest{
    private final RoomManager roomManager= new RoomManager (new FakeRoomRepository ());
    @Test
    public void sameCoordinatorInstance (){
        ChiefEventCoordinator a= ChiefEventCoordinator.getInstance ();
        ChiefEventCoordinator b= ChiefEventCoordinator.getInstance ();
        assertSame (a, b);
    }
    @Test
    public void generateAdministrator (){
        ChiefEventCoordinator chief= ChiefEventCoordinator.getInstance ();
        Administrator admin= chief.generateAdministratorAccount ("admin-unique-1", "Alice Admin", "alice.admin@yorku.ca", roomManager);
        assertNotNull (admin);
        assertEquals ("admin-unique-1", admin.getAdminId ());
    }
    @Test
    public void duplicateAdmin (){
        ChiefEventCoordinator chief= ChiefEventCoordinator.getInstance ();
        chief.generateAdministratorAccount ("admin-unique-2", "Bob", "bob@yorku.ca", roomManager);
        Administrator second= chief.generateAdministratorAccount ("admin-unique-2", "Bob2", "bob2@yorku.ca", roomManager);
        assertNull (second);
    }
    @Test (expected= IllegalStateException.class)
    public void nullRoomManager (){
        ChiefEventCoordinator chief= ChiefEventCoordinator.getInstance ();
        chief.generateAdministratorAccount ("admin-unique-3", "Carl", "carl@yorku.ca", null);
    }
    @Test
    public void nullAdmin (){
        ChiefEventCoordinator chief= ChiefEventCoordinator.getInstance ();
        assertNull (chief.generateAdministratorAccount (null, "Dan", "dan@yorku.ca", roomManager));
    }
    @Test
    public void createdAdministrator (){
        ChiefEventCoordinator chief= ChiefEventCoordinator.getInstance ();
        Administrator created= chief.generateAdministratorAccount ("admin-unique-4", "Eve", "eve@yorku.ca", roomManager);
        Administrator found= chief.findExistingAdministrator ("admin-unique-4");
        assertSame (created, found);
    }
    @Test
    public void unknownAdministrator (){
        ChiefEventCoordinator chief= ChiefEventCoordinator.getInstance ();
        assertNull (chief.findExistingAdministrator ("no-such-admin-id-xyz"));
    }
    @Test
    public void nullAdministrator (){
        ChiefEventCoordinator chief= ChiefEventCoordinator.getInstance ();
        assertNull (chief.findExistingAdministrator (null));
    }
    @Test
    public void generatedAdminIsOk (){
        ChiefEventCoordinator chief= ChiefEventCoordinator.getInstance ();
        Administrator admin= chief.generateAdministratorAccount ("admin-unique-5", "Frank", "frank@yorku.ca", roomManager);
        assertEquals ("Frank", admin.getName ());
        assertEquals ("frank@yorku.ca", admin.getEmail ());
    }
    @Test
    public void differentAdmins (){
        ChiefEventCoordinator chief= ChiefEventCoordinator.getInstance ();
        Administrator first= chief.generateAdministratorAccount ("admin-unique-6", "George", "george@yorku.ca", roomManager);
        Administrator second= chief.generateAdministratorAccount ("admin-unique-7", "Helen", "helen@yorku.ca", roomManager);
        assertNotSame (first, second);
    }
}

