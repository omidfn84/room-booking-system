package com.group10.scheduler.persistence.csv;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;

import com.group10.scheduler.accounts.RegisteredUser;
import com.group10.scheduler.accounts.RegisteredUserFactory;
import com.group10.scheduler.accounts.Student;

public class CsvUserRepositoryTest{

    @Rule
    public TemporaryFolder tempFolder= new TemporaryFolder ();

    private final RegisteredUserFactory factory= new RegisteredUserFactory ();

    @Test
    public void missingFileIsEmpty () throws Exception{
        File file= new File (tempFolder.getRoot (), "does-not-exist.csv");
        CsvUserRepository repo= new CsvUserRepository (file.getAbsolutePath (), factory);
        assertTrue (repo.loadUsers ().isEmpty ());
    }

    @Test
    public void userSaveAndLoad () throws Exception{
        File file= tempFolder.newFile ("users.csv");
        CsvUserRepository repo= new CsvUserRepository (file.getAbsolutePath (), factory);

        RegisteredUser user= new Student ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", 123456789L);
        repo.saveUsers (List.of (user));

        List <RegisteredUser> loaded= repo.loadUsers ();
        assertEquals (1, loaded.size ());
        RegisteredUser reloaded= loaded.get (0);
        assertEquals ("alice@yorku.ca", reloaded.getEmail ());
        assertEquals ("Passw0rd!", reloaded.getPassword ());
        assertEquals ("Alice", reloaded.getUserName ());
        assertEquals (123456789L, reloaded.getOrganizationId ());
        assertTrue (reloaded instanceof Student);
    }

    @Test
    public void userTypeReloaded () throws Exception{
        File file= tempFolder.newFile ("users.csv");
        CsvUserRepository repo= new CsvUserRepository (file.getAbsolutePath (), factory);

        repo.saveUsers (List.of (
                new Student ("s@yorku.ca", "Passw0rd!", "STUDENT", "S", 111111111L),
                factory.createUser ("f@yorku.ca", "Passw0rd!", "FACULTY", "F", 222222222L),
                factory.createUser ("p@partner.com", "Passw0rd!", "PARTNER", "P", 333333333L)
        ));

        List <RegisteredUser> loaded= repo.loadUsers ();
        assertEquals ("STUDENT", loaded.get (0).getAccountType ());
        assertEquals ("FACULTY", loaded.get (1).getAccountType ());
        assertEquals ("PARTNER", loaded.get (2).getAccountType ());
    }

    @Test
    public void saveOverwritesUsers () throws Exception{
        File file= tempFolder.newFile ("users.csv");
        CsvUserRepository repo= new CsvUserRepository (file.getAbsolutePath (), factory);

        repo.saveUsers (List.of (new Student ("a@yorku.ca", "Passw0rd!", "STUDENT", "A", 111111111L)));
        repo.saveUsers (List.of (new Student ("b@yorku.ca", "Passw0rd!", "STUDENT", "B", 222222222L)));

        List <RegisteredUser> loaded= repo.loadUsers ();
        assertEquals (1, loaded.size ());
        assertEquals ("b@yorku.ca", loaded.get (0).getEmail ());
    }

    @Test
    public void emptyUserList () throws Exception{
        File file= tempFolder.newFile ("users.csv");
        CsvUserRepository repo= new CsvUserRepository (file.getAbsolutePath (), factory);
        repo.saveUsers (List.of ());
        assertTrue (repo.loadUsers ().isEmpty ());
    }

    @Test
    public void badOrganizationIdBecomesZero () throws Exception{
        File file= tempFolder.newFile ("users.csv");
        try (java.io.FileWriter fw= new java.io.FileWriter (file, false)){
            fw.write ("accountType,email,password,userName,organizationId\n");
            fw.write ("STUDENT,alice@yorku.ca,Passw0rd!,Alice,NOT_A_NUMBER\n");
        }
        CsvUserRepository repo= new CsvUserRepository (file.getAbsolutePath (), factory);
        List <RegisteredUser> loaded= repo.loadUsers ();
        assertEquals (1, loaded.size ());
        assertEquals (0L, loaded.get (0).getOrganizationId ());
    }
    @Test
    public void facultyData () throws Exception{
        File file= tempFolder.newFile ("faculty.csv");
        CsvUserRepository repo= new CsvUserRepository (file.getAbsolutePath (), factory);
        RegisteredUser user= factory.createUser ("faculty@yorku.ca", "Passw0rd!", "FACULTY", "Frank", 123456789L);
        repo.saveUsers (List.of (user));
        RegisteredUser loaded= repo.loadUsers ().get (0);
        assertEquals ("FACULTY", loaded.getAccountType ());
        assertEquals (30.0, loaded.getHourlyRate (), 0.0);
    }
    @Test
    public void partnerData () throws Exception{
        File file= tempFolder.newFile ("partner.csv");
        CsvUserRepository repo= new CsvUserRepository (file.getAbsolutePath (), factory);
        RegisteredUser user= factory.createUser ("partner@gmail.com", "Passw0rd!", "PARTNER", "Paul", 987654321L);
        repo.saveUsers (List.of (user));
        RegisteredUser loaded= repo.loadUsers ().get (0);
        assertEquals ("partner@gmail.com", loaded.getEmail ());
        assertEquals ("Paul", loaded.getUserName ());
        assertEquals (987654321L, loaded.getOrganizationId ());
    }
    @Test
    public void usersInSameOrder () throws Exception{
        File file= tempFolder.newFile ("users-order.csv");
        CsvUserRepository repo= new CsvUserRepository (file.getAbsolutePath (), factory);
        RegisteredUser first= factory.createUser ("first@yorku.ca", "Passw0rd!", "STUDENT", "First", 111111111L);
        RegisteredUser second= factory.createUser ("second@yorku.ca", "Passw0rd!", "STAFF", "Second", 222222222L);
        repo.saveUsers (List.of (first, second));
        List <RegisteredUser> loaded= repo.loadUsers ();
        assertEquals ("first@yorku.ca", loaded.get (0).getEmail ());
        assertEquals ("second@yorku.ca", loaded.get (1).getEmail ());
    }
    @Test
    public void blankOrganizationId () throws Exception{
        File file= tempFolder.newFile ("blank-id.csv");
        try (java.io.FileWriter writer= new java.io.FileWriter (file)){
            writer.write ("accountType,email,password,userName,organizationId\n");
            writer.write ("STUDENT,student@yorku.ca,Passw0rd!,Student,\n");
        }
        CsvUserRepository repo= new CsvUserRepository (file.getAbsolutePath (), factory);
        List <RegisteredUser> loaded= repo.loadUsers ();
        assertEquals (1, loaded.size ());
        assertEquals (0L, loaded.get (0).getOrganizationId ());
    }
}

