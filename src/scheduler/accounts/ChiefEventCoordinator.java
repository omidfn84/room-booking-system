package scheduler.accounts;

import java.util.HashMap;
import java.util.Map;

import scheduler.room.RoomManager;

//singleton class 
public class ChiefEventCoordinator {
	private long validOrganizationId;
	private static ChiefEventCoordinator instance = null;

	//the chief generates AND keeps the administrators (Req2 is the chief's job).
	private final Map<String, Administrator> admins = new HashMap<>();

//Private Constructor
	private ChiefEventCoordinator() {

	}
//Accessing the singleton object
	public static ChiefEventCoordinator getInstance() {
	    if (instance == null) {
	        instance = new ChiefEventCoordinator();
	    }

	    return instance;
	}

//Only the chief should create administrators 
	public Administrator generateAdministratorAccount(String adminId,String name,String email, RoomManager roomManager) {
		if (roomManager == null) {
			throw new IllegalStateException("RoomManager is null.");
		}
		if (adminId == null || admins.containsKey(adminId)) {
			return null; //admin ids must be unique
		}
		Administrator admin = new Administrator(adminId,name,email,roomManager);
		admins.put(adminId, admin);
		return admin;
	}
//the chief is the single authority on who is an administrator
	public Administrator findExistingAdministrator(String adminId) {
		if (adminId == null) {
			return null;
		}
		return admins.get(adminId);
	}
}