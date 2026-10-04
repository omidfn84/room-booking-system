package scheduler.accounts;

import static org.junit.Assert.*;
import org.junit.*;
public class AccountManagementTest{
    private FakeUserRepository repo;
    private AccountManagement accountManagement;
    @Before
    public void setUp (){
        repo= new FakeUserRepository ();
        accountManagement= new AccountManagement (repo);
    }
    @Test
    public void createStudentAccount (){
        RegisteredUser u= accountManagement.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");
        assertNotNull (u);
        assertTrue (u instanceof Student);
        assertEquals (1, repo.saveCallCount);
    }
    @Test
    public void createPartnerAccount (){
        RegisteredUser u= accountManagement.createAccount ("dave@partner.com", "Passw0rd!", "PARTNER", "Dave", "123456789");
        assertNotNull (u);
        assertTrue (u instanceof Partner);
    }
    @Test (expected= IllegalArgumentException.class)
    public void universityEmailError (){
        accountManagement.createAccount ("alice@gmail.com", "Passw0rd!", "STUDENT", "Alice", "123456789");
    }
    @Test (expected= IllegalArgumentException.class)
    public void duplicateEmailError (){
        accountManagement.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");
        accountManagement.createAccount ("alice@yorku.ca", "Different1!", "STAFF", "Alice2", "987654321");
    }
    @Test (expected= IllegalArgumentException.class)
    public void emailError (){
        accountManagement.createAccount ("not-an-email", "Passw0rd!", "STUDENT", "Alice", "123456789");
    }
    @Test (expected= IllegalArgumentException.class)
    public void shortPassword (){
        accountManagement.createAccount ("alice@yorku.ca", "P0w!", "STUDENT", "Alice", "123456789");
    }
    @Test (expected= IllegalArgumentException.class)
    public void missingUppercase (){
        accountManagement.createAccount ("alice@yorku.ca", "passw0rd!", "STUDENT", "Alice", "123456789");
    }
    @Test (expected= IllegalArgumentException.class)
    public void missingLowercase (){
        accountManagement.createAccount ("alice@yorku.ca", "PASSW0RD!", "STUDENT", "Alice", "123456789");
    }
    @Test (expected= IllegalArgumentException.class)
    public void missingDigit (){
        accountManagement.createAccount ("alice@yorku.ca", "Password!", "STUDENT", "Alice", "123456789");
    }
    @Test (expected= IllegalArgumentException.class)
    public void missingSymbolError (){
        accountManagement.createAccount ("alice@yorku.ca", "Passw0rd", "STUDENT", "Alice", "123456789");
    }
    @Test (expected= IllegalArgumentException.class)
    public void blankNameError (){
        accountManagement.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "  ", "123456789");
    }
    @Test (expected= IllegalArgumentException.class)
    public void wrongIdLength (){
        accountManagement.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "1234");
    }
    @Test (expected= IllegalArgumentException.class)
    public void nonNumericIdError (){
        accountManagement.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "12345678A");
    }
    @Test (expected= IllegalArgumentException.class)
    public void unsupportedType (){
        accountManagement.createAccount ("alice@yorku.ca", "Passw0rd!", "ALUMNI", "Alice", "123456789");
    }
    @Test
    public void existingUser (){
        accountManagement.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");
        RegisteredUser found= accountManagement.findByEmail ("ALICE@YORKU.CA");
        assertNotNull (found);
        assertEquals ("alice@yorku.ca", found.getEmail ());
    }
    @Test
    public void missingUser (){
        assertNull (accountManagement.findByEmail ("nobody@yorku.ca"));
    }
    @Test
    public void blankEmail (){
        assertNull (accountManagement.findByEmail (""));
    }
    @Test
    public void loadUsersOnConstruction (){
        FakeUserRepository preloaded= new FakeUserRepository (){
            @Override
            public java.util.List <RegisteredUser> loadUsers (){
                java.util.List <RegisteredUser> list= new java.util.ArrayList <>();
                list.add (new Student ("existing@yorku.ca", "Passw0rd!", "STUDENT", "Existing", 111111111L));
                return list;
            }
        };
        AccountManagement am= new AccountManagement (preloaded);
        assertNotNull (am.findByEmail ("existing@yorku.ca"));
    }
}

