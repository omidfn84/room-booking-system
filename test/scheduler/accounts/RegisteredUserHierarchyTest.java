package scheduler.accounts;

import static org.junit.Assert.*;
import org.junit.Test;
/**
 * Tests the registered user types and their hourly rates.
 */
public class RegisteredUserHierarchyTest{
    @Test
    public void studentRate (){
        Student s= new Student ("a@yorku.ca", "Passw0rd!", "STUDENT", "Alice", 123456789L);
        assertEquals (20.0, s.getHourlyRate (), 0.0001);
    }
    @Test
    public void facultyRate (){
        Faculty f= new Faculty ("b@yorku.ca", "Passw0rd!", "FACULTY", "Bob", 123456789L);
        assertEquals (30.0, f.getHourlyRate (), 0.0001);
    }
    @Test
    public void staffRate (){
        Staff st= new Staff ("c@yorku.ca", "Passw0rd!", "STAFF", "Carol", 123456789L);
        assertEquals (40.0, st.getHourlyRate (), 0.0001);
    }
    @Test
    public void partnerRate (){
        Partner p= new Partner ("d@partner.com", "Passw0rd!", "PARTNER", "Dave", 123456789L);
        assertEquals (50.0, p.getHourlyRate (), 0.0001);
    }
    @Test
    public void constructorFields (){
        Student s= new Student ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice Smith", 987654321L);
        assertEquals ("alice@yorku.ca", s.getEmail ());
        assertEquals ("Passw0rd!", s.getPasswordHash ());
        assertEquals ("STUDENT", s.getAccountType ());
        assertEquals ("Alice Smith", s.getUserName ());
        assertEquals (987654321L, s.getOrganizationId ());
    }
    @Test
    public void changeAccountType (){
        Student s= new Student ("a@yorku.ca", "Passw0rd!", "STUDENT", "Alice", 123456789L);
        s.setAccountType ("STAFF");
        assertEquals ("STAFF", s.getAccountType ());
    }
    @Test
    public void changeOrganizationId (){
        Student s= new Student ("a@yorku.ca", "Passw0rd!", "STUDENT", "Alice", 123456789L);
        s.setOrganizationId (111111111L);
        assertEquals (111111111L, s.getOrganizationId ());
    }
    @Test
    public void userToString (){
        Student s= new Student ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", 123456789L);
        String result= s.toString ();
        assertTrue (result.contains ("STUDENT"));
        assertTrue (result.contains ("Alice"));
        assertTrue (result.contains ("alice@yorku.ca"));
        assertTrue (result.contains ("20.0"));
    }
    @Test
    public void differentUserRates (){
        RegisteredUser student= new Student ("a@yorku.ca", "Passw0rd!", "STUDENT", "A", 111111111L);
        RegisteredUser partner= new Partner ("b@partner.com", "Passw0rd!", "PARTNER", "B", 222222222L);
        assertNotEquals (student.getHourlyRate (), partner.getHourlyRate (), 0.0001);
    }
    @Test
    public void universityUserType (){
        RegisteredUser s= new Student ("a@yorku.ca", "Passw0rd!", "STUDENT", "A", 111111111L);
        assertTrue (s instanceof UniversityUser);
        assertTrue (s instanceof RegisteredUser);
    }
    @Test
    public void partnerUserType (){
        RegisteredUser p= new Partner ("b@partner.com", "Passw0rd!", "PARTNER", "B", 222222222L);
        assertFalse (p instanceof UniversityUser);
    }
}
