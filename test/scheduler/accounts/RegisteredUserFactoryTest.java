package scheduler.accounts;

import static org.junit.Assert.*;
import org.junit.Test;
public class RegisteredUserFactoryTest{
    private final RegisteredUserFactory factory= new RegisteredUserFactory ();
    @Test
    public void createStudent (){
        RegisteredUser u= factory.createUser ("a@yorku.ca", "Passw0rd!", "STUDENT", "Alice", 111111111L);
        assertTrue (u instanceof Student);
    }
    @Test
    public void createStaff (){
        RegisteredUser u= factory.createUser ("a@yorku.ca", "Passw0rd!", "STAFF", "Alice", 111111111L);
        assertTrue (u instanceof Staff);
    }
    @Test
    public void createFaculty (){
        RegisteredUser u= factory.createUser ("a@yorku.ca", "Passw0rd!", "FACULTY", "Alice", 111111111L);
        assertTrue (u instanceof Faculty);
    }
    @Test
    public void createPartner (){
        RegisteredUser u= factory.createUser ("a@partner.com", "Passw0rd!", "PARTNER", "Alice", 111111111L);
        assertTrue (u instanceof Partner);
    }
    @Test
    public void accountTypeCaseSensitive (){
        RegisteredUser u= factory.createUser ("a@yorku.ca", "Passw0rd!", "student", "Alice", 111111111L);
        assertTrue (u instanceof Student);
    }
    @Test
    public void accountTypeSpaces (){
        RegisteredUser u= factory.createUser ("a@yorku.ca", "Passw0rd!", "  STUDENT  ", "Alice", 111111111L);
        assertTrue (u instanceof Student);
    }
    @Test (expected= IllegalArgumentException.class)
    public void unsupportedUser (){
        factory.createUser ("a@yorku.ca", "Passw0rd!", "ALUMNI", "Alice", 111111111L);
    }
    @Test (expected= NullPointerException.class)
    public void nullUserType (){
        factory.createUser ("a@yorku.ca", "Passw0rd!", null, "Alice", 111111111L);
    }
    @Test
    public void userIsOk (){
        RegisteredUser u= factory.createUser ("bob@yorku.ca", "Passw0rd!", "FACULTY", "Bob", 222222222L);
        assertEquals ("bob@yorku.ca", u.getEmail ());
        assertEquals ("Bob", u.getUserName ());
        assertEquals (222222222L, u.getOrganizationId ());
    }
    @Test
    public void differentUsers (){
        RegisteredUser u1= factory.createUser ("a@yorku.ca", "Passw0rd!", "STUDENT", "A", 111111111L);
        RegisteredUser u2= factory.createUser ("b@yorku.ca", "Passw0rd!", "STUDENT", "B", 222222222L);
        assertNotSame (u1, u2);
    }
}
