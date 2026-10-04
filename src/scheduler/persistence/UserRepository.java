package scheduler.persistence;

import java.util.List;

import scheduler.accounts.RegisteredUser;

public interface UserRepository {
    List<RegisteredUser> loadUsers();
    void saveUsers(List<RegisteredUser> users);
}
