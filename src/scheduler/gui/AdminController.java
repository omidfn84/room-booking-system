package scheduler.gui;

import scheduler.accounts.Administrator;
import scheduler.facade.SchedulerFacade;
import scheduler.room.Room;
import scheduler.room.RoomStatus;

import java.util.List;

/**
 * AdminController — presentation logic for the MANAGEMENT side of the GUI,
 * serving two actors (GUIController serves the third actor, registered users):
 *
 *   CHIEF EVENT COORDINATOR (Req2): one person; generates administrator
 *   accounts. Needs no admin session, generating admins IS the chief's act,
 *   and the Singleton behind the facade guarantees there is only one chief.
 *
 *   ADMINISTRATOR (Req6, Req7): manages rooms. Must first "log in" by
 *   presenting a valid admin id (validated against the chief's registry via
 *   the facade); room operations then use that session and return false when
 *   no admin is logged in.
 *
 * Depends on SchedulerFacade only.
 */
public class AdminController {
    private final SchedulerFacade facade;
    private String currentAdminId;

    public AdminController(SchedulerFacade facade) {
        this.facade = facade;
    }

    // ==================== CHIEF section (Req2) ====================

    public Administrator onGenerateAdminClicked(String adminId, String name, String email) {
        return facade.generateAdministratorAccount(adminId, name, email);
    }

    // ================= ADMINISTRATOR section (Req6, Req7) =================

    /** Admin "login": present a valid admin id once, like the user side's login. */
    public boolean onAdminLoginClicked(String adminId) {
        if (facade.isAdministrator(adminId)) {
            currentAdminId = adminId;
            return true;
        }
        return false;
    }

    public void onAdminLogoutClicked() {
        currentAdminId = null;
    }

    public boolean isAdminLoggedIn() {
        return currentAdminId != null;
    }

    public String getCurrentAdminId() {
        return currentAdminId;
    }

    /** Req7: the admin supplies the room's id, capacity, and building/room location. */
    public boolean onAddRoomClicked(String roomId, int capacity, String building, String roomNumber) {
        if (currentAdminId == null) return false;
        return facade.addRoom(currentAdminId, new Room(roomId, capacity, building, roomNumber, RoomStatus.AVAILABLE));
    }

    public boolean onEnableRoomClicked(String roomId) {
        return currentAdminId != null && facade.enableRoom(currentAdminId, roomId);
    }

    public boolean onDisableRoomClicked(String roomId) {
        return currentAdminId != null && facade.disableRoom(currentAdminId, roomId);
    }

    public boolean onCloseRoomClicked(String roomId) {
        return currentAdminId != null && facade.closeRoom(currentAdminId, roomId);
    }

    public List<Room> getAllRooms() {
        return facade.getAllRooms();
    }
}