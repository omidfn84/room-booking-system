package scheduler.accounts;

import java.util.*;
import scheduler.persistence.UserRepository;
/**
 * Keeps user data in memory for the tests.
 */
public class FakeUserRepository implements UserRepository{
    public List <RegisteredUser> savedUsers= new ArrayList <>();
    public int saveCallCount= 0;
    @Override
    public List <RegisteredUser> loadUsers (){
        return new ArrayList <>();
    }
    @Override
    public void saveUsers (List <RegisteredUser> users){
        savedUsers= new ArrayList <>(users);
        saveCallCount++;
    }
}
