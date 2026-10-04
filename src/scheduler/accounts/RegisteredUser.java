package scheduler.accounts;

public abstract class RegisteredUser {
	// Attributes are protected to protect data integrity; getter methods provide
	// controlled access.
	private String email;
	private String password;
	private String userName;
	protected String accountType;
	private long organizationId;

	// constructor
	public RegisteredUser(String email, String password, String accountType, String userName,  long organizationId) {

		this.email = email;
		this.password = password;
		this.accountType = accountType;
		this.userName = userName;
		this.organizationId = organizationId;
	}
	// getter methods
	
	public String getPassword() {
		return password; }

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
