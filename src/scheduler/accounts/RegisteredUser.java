package scheduler.accounts;

public abstract class RegisteredUser {
	// Attributes are protected to protect data integrity; getter methods provide
	// controlled access.
	private String email;
	// never the plain-text password: a PasswordHasher hash (see AccountManagement)
	private String passwordHash;
	private String userName;
	protected String accountType;
	private long organizationId;

	// constructor
	public RegisteredUser(String email, String passwordHash, String accountType, String userName,  long organizationId) {

		this.email = email;
		this.passwordHash = passwordHash;
		this.accountType = accountType;
		this.userName = userName;
		this.organizationId = organizationId;
	}
	// getter methods
	
	public String getPasswordHash() {
		return passwordHash;
	}

	// package-private: only AccountManagement may replace a stored hash
	void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getEmail() {
		return email;
	}

	public String getUserName() {
		return userName;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(long organizationId) {
		this.organizationId = organizationId;
	}
	
	public abstract double getHourlyRate();
	
	@Override
	public String toString() {
	    return accountType + " | " + userName + " | " + email 
	        + " | rate=$" + getHourlyRate() + "/hr"
	        + " | orgId=" + organizationId;
	}
}
