package scheduler.accounts;

public class RegisteredUserFactory {

	public RegisteredUser createUser(String email, String passwordHash, String accountType, String userName, long organizationId) {

		if (accountType.trim().equalsIgnoreCase("STUDENT")) {
			return new Student(email, passwordHash, accountType, userName,organizationId);
		}

		if (accountType.trim().equalsIgnoreCase("STAFF")) {
			return new Staff(email, passwordHash, accountType, userName,organizationId);
		}

		if (accountType.trim().equalsIgnoreCase("FACULTY")) {
			return new Faculty(email, passwordHash, accountType, userName,organizationId);
		}

		if (accountType.trim().equalsIgnoreCase("PARTNER")) {
			return new Partner(email, passwordHash, accountType, userName,organizationId);
		}

		throw new IllegalArgumentException("Unsupported account type: " + accountType);
	}
}
