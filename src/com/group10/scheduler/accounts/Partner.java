package com.group10.scheduler.accounts;

public class Partner extends RegisteredUser {
	private static final double HOURLY_RATE = 50.0;

	// constructor
	public Partner(String email, String password, String accountType, String userName, long organizationId) {
		super(email, password, accountType, userName,organizationId);
	}

	@Override
	public double getHourlyRate() {
		return HOURLY_RATE;
	}
}