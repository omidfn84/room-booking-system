package scheduler.accounts;
public abstract class UniversityUser extends RegisteredUser {

    public UniversityUser(
            String email,
            String passwordHash,
            String accountType,
            String userName,
            long organizationId) {

        super(email, passwordHash, accountType, userName,organizationId);
    }
}