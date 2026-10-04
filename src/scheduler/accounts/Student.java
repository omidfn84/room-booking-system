package scheduler.accounts;
public class Student extends UniversityUser {
	private static final double HOURLY_RATE = 20.0;
//constructor
	public Student(String email,String passwordHash,String accountType,String userName, long organizationId){
	   super(email, passwordHash, accountType, userName,organizationId);
	}
	
	@Override
	public double getHourlyRate() {
		return HOURLY_RATE;
	}
}