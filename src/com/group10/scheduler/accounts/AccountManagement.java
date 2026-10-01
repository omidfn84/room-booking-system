package com.group10.scheduler.accounts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.group10.scheduler.persistence.UserRepository;

public class AccountManagement {
	private final Set<String> registeredEmails = new HashSet<>();
	private final List<RegisteredUser> registeredUsers = new ArrayList<>();
	// calling the factory method to create account
	private final RegisteredUserFactory factory;
	// Adapter pattern: users are persisted through the UserRepository target interface
	private final UserRepository userRepository;

	// constructor
	public AccountManagement(UserRepository userRepository) {
		factory = new RegisteredUserFactory();
		this.userRepository = userRepository;
		// rehydrate users that were validated when originally registered
		registeredUsers.addAll(userRepository.loadUsers());
		for (RegisteredUser user : registeredUsers) {
			registeredEmails.add(user.getEmail().toLowerCase());
		}
	}
	

	// creating an enum for account types with flexibility of adding more types
	public enum AccountType {

		STUDENT(true), STAFF(true), FACULTY(true),
		// partner is not a university account
		PARTNER(false);

		private final boolean universityVerificationRequired;

		AccountType(boolean universityVerificationRequired) {
			this.universityVerificationRequired = universityVerificationRequired;
		}

		public boolean requiresUniversityVerification() {
			return universityVerificationRequired;
		}
	}

	// validating email
	private final String emailFormat = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

	// diagram name: verifyUniqueEmail (checks format AND uniqueness)
	private void verifyUniqueEmail(String email) {

		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email cannot be empty.");
		}

		String normalizedEmail = email.trim().toLowerCase();

		if (!normalizedEmail.matches(emailFormat)) {
			throw new IllegalArgumentException("Email format is invalid.");
		}

		if (registeredEmails.contains(normalizedEmail)) {
			throw new IllegalArgumentException("This email is already registered.");
		}
	}

	// diagram name: validateStrongPassword
	private void validateStrongPassword(String password) {

		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("Password cannot be empty.");
		}

		if (password.length() < 8) {
			throw new IllegalArgumentException("Password must contain at least 8 characters.");
		}

		if (!password.matches(".*[A-Z].*")) {
			throw new IllegalArgumentException("Password must contain at least one uppercase letter.");
		}

		if (!password.matches(".*[a-z].*")) {
			throw new IllegalArgumentException("Password must contain at least one lowercase letter.");
		}

		if (!password.matches(".*\\d.*")) {
			throw new IllegalArgumentException("Password must contain at least one number.");
		}

		if (!password.matches(".*[^A-Za-z0-9].*")) {
			throw new IllegalArgumentException("Password must contain at least one symbol.");
		}
	}

	private void validateUserName(String userName) {
		if (userName == null || userName.isBlank()) {
			throw new IllegalArgumentException("Username cannot be empty.");
		}
	}

	// verify university accounts
	private void verifyUniversityAccount(String email) {

		if (!email.toLowerCase().endsWith("@yorku.ca")) {
			throw new IllegalArgumentException("University accounts must use a university email.");
		}
	}

	/*
	 * private void validateAccountType(String accountType) { if (accountType ==
	 * null || accountType.isBlank()) { throw new
	 * IllegalArgumentException("Account type cannot be empty."); }
	 * 
	 * if (!accountType.equalsIgnoreCase("STUDENT") &&
	 * !accountType.equalsIgnoreCase("STAFF") &&
	 * !accountType.equalsIgnoreCase("FACULTY") &&
	 * !accountType.equalsIgnoreCase("PARTNER")) {
	 * 
	 * throw new IllegalArgumentException("Unsupported account type: " +
	 * accountType); } }
	 */
	private AccountType validateAccountType(String accountType) {

		if (accountType == null || accountType.isBlank()) {
			throw new IllegalArgumentException("Account type cannot be empty.");
		}

		try {
			return AccountType.valueOf(accountType.trim().toUpperCase());

		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unsupported account type: " + accountType);
		}
	}
	private void validateOrganizationId(String organizationId) {

	    if (organizationId == null || organizationId.isBlank()) {
	        throw new IllegalArgumentException(
	            "Organization ID cannot be empty.");
	    }

	    if (!organizationId.matches("\\d{9}")) {
	        throw new IllegalArgumentException(
	            "Organization ID must contain exactly 9 digits.");
	    }
	}

	// checks everything
	public void validateAccount(String email, String password, String accountType, String userName, String organizationId ) {

		verifyUniqueEmail(email);
		validateStrongPassword(password);
		validateUserName(userName);
		validateOrganizationId(organizationId);

		AccountType type = validateAccountType(accountType);

		if (type.requiresUniversityVerification()) {
			verifyUniversityAccount(email);
		}
	}

	public RegisteredUser createAccount(String email, String password, String accountType, String userName, String organizationId) {

		validateAccount(email, password, accountType, userName, organizationId);
		// safe: validateOrganizationId guarantees exactly 9 digits
		long orgId = Long.parseLong(organizationId.trim());
		RegisteredUser user= factory.createUser(email.trim().toLowerCase(), password, accountType.trim().toUpperCase(),
				userName.trim(), orgId);
		   registeredUsers.add(user);
		   registeredEmails.add(email.trim().toLowerCase());
		   userRepository.saveUsers(registeredUsers);
		   return user;
	}
	//Finding the user by email
	public RegisteredUser findByEmail(String email) {

	    if (email == null || email.isBlank()) {
	        return null;
	    }

	    String normalizedEmail = email.trim().toLowerCase();

	    for (RegisteredUser user : registeredUsers) {
	        if (user.getEmail().equalsIgnoreCase(normalizedEmail)) {
	            return user;
	        }
	    }

	    return null;
	}
}