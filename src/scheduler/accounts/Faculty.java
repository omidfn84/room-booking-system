package scheduler.accounts;
public class Faculty extends UniversityUser {
	private static final double HOURLY_RATE=30.0;
	//default constructor

	public Faculty(String email, String passwordHash, String accountType, String userName, long organizationId) {
		super(email, passwordHash, accountType, userName, organizationId);
	}

	@Override
	public double getHourlyRate() {
		return HOURLY_RATE;
	}


}