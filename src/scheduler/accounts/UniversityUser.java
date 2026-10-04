package scheduler.accounts;
public abstract class UniversityUser extends RegisteredUser {

    public UniversityUser(
            String email,
            String password,
            String accountType,
            String userName,
            long organizationId) {

        super(email, password, accountType, userName,organizationId);
    }
}