package scheduler.accounts;

public class RegisteredUserFactory {

	public RegisteredUser createUser(String email, String password, String accountType, String userName, long organizationId) {

		if (accountType.trim().equalsIgnoreCase("STUDENT")) {
			return new Student(email, password, accountType, userName,organizationId);
		}

		if (accountType.trim().equalsIgnoreCase("STAFF")) {
			return new Staff(email, password, accountType, userName,organizationId);
		}

		if (accountType.trim().equalsIgnoreCase("FACULTY")) {
			return new Faculty(email, password, accountType, userName,organizationId);
		}

		if (accountType.trim().equalsIgnoreCase("PARTNER")) {
			return new Partner(email, password, accountType, userName,organizationId);
		}

		throw new IllegalArgumentException("Unsupported account type: " + accountType);
	}
}
