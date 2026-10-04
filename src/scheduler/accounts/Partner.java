package scheduler.accounts;

public class Partner extends RegisteredUser {
	private static final double HOURLY_RATE = 50.0;

	// constructor
	public Partner(String email, String passwordHash, String accountType, String userName, long organizationId) {
		super(email, passwordHash, accountType, userName,organizationId);
	}

	@Override
	public double getHourlyRate() {
		return HOURLY_RATE;
	}
}