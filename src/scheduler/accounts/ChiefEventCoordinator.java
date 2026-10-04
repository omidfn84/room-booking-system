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

	// The Singleton guarantees there is ONE chief; this credential proves the
	// person using it IS the chief. Only a PasswordHasher hash is kept.
	private String credentialHash;

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

//Set once at start-up by the composition root; it cannot be changed afterwards
	public synchronized void configureCredential(String chiefPassword) {
		if (credentialHash != null) {
			throw new IllegalStateException("The chief password is already set.");
		}
		if (chiefPassword == null || chiefPassword.isBlank()) {
			throw new IllegalArgumentException("The chief password cannot be empty.");
		}
		credentialHash = PasswordHasher.hash(chiefPassword);
	}

	public boolean isCredentialConfigured() {
		return credentialHash != null;
	}

//Only the chief should create administrators, so the chief password is required
	public Administrator generateAdministratorAccount(String chiefPassword, String adminId, String name, String email, RoomManager roomManager) {
		if (roomManager == null) {
			throw new IllegalStateException("RoomManager is null.");
		}
		if (credentialHash == null) {
			throw new IllegalStateException("The chief password has not been configured.");
		}
		if (!PasswordHasher.verify(chiefPassword, credentialHash)) {
			throw new SecurityException("Incorrect chief password.");
		}
		if (adminId == null || adminId.isBlank() || admins.containsKey(adminId)) {
			return null; //admin ids must be present and unique
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