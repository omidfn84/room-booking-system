package scheduler.accounts;
public class Student extends UniversityUser {
	private static final double HOURLY_RATE = 20.0;
//constructor
	public Student(String email,String password,String accountType,String userName, long organizationId){
	   super(email, password, accountType, userName,organizationId);
	}
	
	@Override
	public double getHourlyRate() {
		return HOURLY_RATE;
	}
}