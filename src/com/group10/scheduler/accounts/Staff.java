package com.group10.scheduler.accounts;

public class Staff extends UniversityUser {
	private static final double HOURLY_RATE = 40.0;

	// constructor
	public Staff(String email, String password, String accountType, String userName, long organizationId) {
		super(email, password, accountType, userName, organizationId);
	}

	@Override
	public double getHourlyRate() {
		return HOURLY_RATE;
	}
}