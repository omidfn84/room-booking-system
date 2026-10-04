package scheduler.gui;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import scheduler.accounts.Administrator;
import scheduler.aisupport.AIFixture;
import scheduler.room.RoomStatus;

/**
 * AdminController serves two actors with different gating:
 *
 *   CHIEF (Req2) - generating admins needs no session; being able to call it
 *   IS the chief's authority, and the Singleton guarantees a single chief.
 *
 *   ADMINISTRATOR (Req6, Req7) - every room operation needs an admin session
 *   opened with an id the chief actually issued.
 *
 * The security-relevant assertion running through these tests is that no room
 * operation succeeds without a session, including after a logout.
 */
public class AdminControllerAITest {

    private AIFixture fx;
    private AdminController controller;

    @Before
    public void setUp() {
        fx = new AIFixture();
        controller = new AdminController(fx.facade);
    }

    /** Generates an admin with a fresh id and logs in as it. */
    private String loginAsNewAdmin() {
        String id = AIFixture.uniqueAdminId();
        assertNotNull(controller.onGenerateAdminClicked(AIFixture.chiefPassword(), id, "Ada", "ada@yorku.ca"));
        assertTrue(controller.onAdminLoginClicked(id));
        return id;
    }

    // ==================== Req2: chief section ====================

    @Test
    public void onGenerateAdminClicked_createsAnAdministrator() {
        String id = AIFixture.uniqueAdminId();

        Administrator admin = controller.onGenerateAdminClicked(AIFixture.chiefPassword(), id, "Ada", "ada@yorku.ca");

        assertNotNull(admin);
        assertEquals(id, admin.getAdminId());
        assertEquals("Ada", admin.getName());
        assertEquals("ada@yorku.ca", admin.getEmail());
    }

    @Test
    public void onGenerateAdminClicked_returnsNullForADuplicateId() {
        // AdminPanel shows "Admin ID already exists." on null, so the null is
        // the contract rather than an exception.
        String id = AIFixture.uniqueAdminId();
        controller.onGenerateAdminClicked(AIFixture.chiefPassword(), id, "Ada", "ada@yorku.ca");

        assertNull(controller.onGenerateAdminClicked(AIFixture.chiefPassword(), id, "Imposter", "imposter@yorku.ca"));
    }

    @Test
    public void onGenerateAdminClicked_doesNotRequireAnAdminSession() {
        // Generating admins is the chief's act, not an administrator's.
        assertFalse(controller.isAdminLoggedIn());

        assertNotNull(controller.onGenerateAdminClicked(AIFixture.chiefPassword(), AIFixture.uniqueAdminId(), "Ada", "ada@yorku.ca"));
    }

    // ==================== admin session ====================

    @Test
    public void aFreshControllerHasNoAdminSession() {
        assertFalse(controller.isAdminLoggedIn());
        assertNull(controller.getCurrentAdminId());
    }

    @Test
    public void onAdminLoginClicked_opensASessionForAnIdTheChiefIssued() {
        String id = AIFixture.uniqueAdminId();
        controller.onGenerateAdminClicked(AIFixture.chiefPassword(), id, "Ada", "ada@yorku.ca");

        assertTrue(controller.onAdminLoginClicked(id));
        assertTrue(controller.isAdminLoggedIn());
        assertEquals(id, controller.getCurrentAdminId());
    }

    @Test
    public void onAdminLoginClicked_refusesAnIdTheChiefNeverIssued() {
        assertFalse(controller.onAdminLoginClicked("NEVER-ISSUED"));
        assertFalse(controller.isAdminLoggedIn());
        assertNull(controller.getCurrentAdminId());
    }

    @Test
    public void onAdminLoginClicked_leavesAnExistingSessionIntactWhenItFails() {
        // A failed second login must not silently log the current admin out.
        String id = loginAsNewAdmin();

        assertFalse(controller.onAdminLoginClicked("NEVER-ISSUED"));

        assertTrue(controller.isAdminLoggedIn());
        assertEquals(id, controller.getCurrentAdminId());
    }

    @Test
    public void onAdminLogoutClicked_closesTheSession() {
        loginAsNewAdmin();

        controller.onAdminLogoutClicked();

        assertFalse(controller.isAdminLoggedIn());
        assertNull(controller.getCurrentAdminId());
    }

    @Test
    public void anAdminCanSwitchToAnotherAdminId() {
        loginAsNewAdmin();
        String second = AIFixture.uniqueAdminId();
        controller.onGenerateAdminClicked(AIFixture.chiefPassword(), second, "Bob", "bob@yorku.ca");

        assertTrue(controller.onAdminLoginClicked(second));
        assertEquals(second, controller.getCurrentAdminId());
    }

    // ==================== Req6/Req7: room management needs a session ====================

    @Test
    public void onAddRoomClicked_addsTheRoomWhenLoggedIn() {
        loginAsNewAdmin();

        assertTrue(controller.onAddRoomClicked("R1", 25, "Lassonde", "101"));

        assertEquals(1, controller.getAllRooms().size());
        assertEquals("R1", controller.getAllRooms().get(0).getRoomId());
    }

    @Test
    public void onAddRoomClicked_storesEveryReq7DetailAndStartsAvailable() {
        loginAsNewAdmin();

        controller.onAddRoomClicked("R1", 25, "Lassonde", "101");

        var room = controller.getAllRooms().get(0);
        assertEquals(25, room.getCapacity());
        assertEquals("Lassonde", room.getBuilding());
        assertEquals("101", room.getRoomNumber());
        assertEquals("A newly added room must be bookable immediately", RoomStatus.AVAILABLE, room.getStatus());
    }

    @Test
    public void onAddRoomClicked_isRefusedWithoutAnAdminSession() {
        assertFalse(controller.isAdminLoggedIn());

        assertFalse(controller.onAddRoomClicked("R1", 25, "Lassonde", "101"));

        assertTrue("An unauthorised add must create nothing", controller.getAllRooms().isEmpty());
    }

    @Test
    public void onAddRoomClicked_refusesADuplicateRoomId() {
        loginAsNewAdmin();
        controller.onAddRoomClicked("R1", 25, "Lassonde", "101");

        assertFalse(controller.onAddRoomClicked("R1", 30, "Bergeron", "202"));
        assertEquals(1, controller.getAllRooms().size());
    }

    @Test
    public void enableDisableAndCloseAreAllRefusedWithoutAnAdminSession() {
        // Set the room up as an admin, then log out and try again.
        loginAsNewAdmin();
        controller.onAddRoomClicked("R1", 25, "Lassonde", "101");
        controller.onAdminLogoutClicked();

        assertFalse(controller.onEnableRoomClicked("R1"));
        assertFalse(controller.onDisableRoomClicked("R1"));
        assertFalse(controller.onCloseRoomClicked("R1"));

        assertEquals("The room must be untouched after unauthorised attempts",
                RoomStatus.AVAILABLE, controller.getAllRooms().get(0).getStatus());
    }

    @Test
    public void onDisableRoomClicked_disablesTheRoomWhenLoggedIn() {
        loginAsNewAdmin();
        controller.onAddRoomClicked("R1", 25, "Lassonde", "101");

        assertTrue(controller.onDisableRoomClicked("R1"));
        assertEquals(RoomStatus.DISABLED, controller.getAllRooms().get(0).getStatus());
    }

    @Test
    public void onEnableRoomClicked_bringsADisabledRoomBack() {
        loginAsNewAdmin();
        controller.onAddRoomClicked("R1", 25, "Lassonde", "101");
        controller.onDisableRoomClicked("R1");

        assertTrue(controller.onEnableRoomClicked("R1"));
        assertEquals(RoomStatus.AVAILABLE, controller.getAllRooms().get(0).getStatus());
    }

    @Test
    public void onCloseRoomClicked_putsTheRoomIntoMaintenance() {
        loginAsNewAdmin();
        controller.onAddRoomClicked("R1", 25, "Lassonde", "101");

        assertTrue(controller.onCloseRoomClicked("R1"));
        assertEquals(RoomStatus.MAINTENANCE, controller.getAllRooms().get(0).getStatus());
    }

    @Test
    public void roomOperationsReturnFalseForAnUnknownRoomIdEvenWhenLoggedIn() {
        loginAsNewAdmin();

        assertFalse(controller.onEnableRoomClicked("R-NOPE"));
        assertFalse(controller.onDisableRoomClicked("R-NOPE"));
        assertFalse(controller.onCloseRoomClicked("R-NOPE"));
    }

    @Test
    public void getAllRooms_isReadableWithoutAnAdminSession() {
        // The rooms table is populated on panel construction, before any login.
        assertNotNull(controller.getAllRooms());
        assertTrue(controller.getAllRooms().isEmpty());
    }
}
