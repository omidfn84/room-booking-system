package scheduler.persistence.csv;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import scheduler.accounts.Faculty;
import scheduler.accounts.Partner;
import scheduler.accounts.RegisteredUser;
import scheduler.accounts.RegisteredUserFactory;
import scheduler.accounts.Staff;
import scheduler.accounts.Student;

public class CsvUserRepositoryAITest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private File csvFile;
	private CsvUserRepository repository;

	@Before
	public void setUp() throws Exception {
		csvFile = temporaryFolder.newFile("users.csv");
		repository = new CsvUserRepository(
				csvFile.getAbsolutePath(),
				new RegisteredUserFactory());
	}

	@Test
	public void loadUsersFromMissingFileReturnsEmptyList() {
		File missingFile =
				new File(temporaryFolder.getRoot(), "missing-users.csv");

		CsvUserRepository missingRepository =
				new CsvUserRepository(
						missingFile.getAbsolutePath(),
						new RegisteredUserFactory());

		List<RegisteredUser> users =
				missingRepository.loadUsers();

		assertNotNull(users);
		assertTrue(users.isEmpty());
	}

	@Test
	public void saveAndLoadStudentPreservesInformation() {
		RegisteredUser student = new Student(
				"student@yorku.ca",
				"Strong1!",
				"STUDENT",
				"Student User",
				123456789L);

		repository.saveUsers(Arrays.asList(student));

		List<RegisteredUser> loadedUsers =
				repository.loadUsers();

		assertEquals(1, loadedUsers.size());

		RegisteredUser loaded = loadedUsers.get(0);

		assertTrue(loaded instanceof Student);
		assertEquals("student@yorku.ca", loaded.getEmail());
		assertEquals("Strong1!", loaded.getPasswordHash());
		assertEquals("STUDENT", loaded.getAccountType());
		assertEquals("Student User", loaded.getUserName());
		assertEquals(123456789L, loaded.getOrganizationId());
	}

	@Test
	public void saveAndLoadStaffCreatesStaffObject() {
		RegisteredUser staff = new Staff(
				"staff@yorku.ca",
				"Strong1!",
				"STAFF",
				"Staff User",
				123456789L);

		repository.saveUsers(Arrays.asList(staff));

		RegisteredUser loaded =
				repository.loadUsers().get(0);

		assertTrue(loaded instanceof Staff);
		assertEquals(40.0, loaded.getHourlyRate(), 0.001);
	}

	@Test
	public void saveAndLoadFacultyCreatesFacultyObject() {
		RegisteredUser faculty = new Faculty(
				"faculty@yorku.ca",
				"Strong1!",
				"FACULTY",
				"Faculty User",
				123456789L);

		repository.saveUsers(Arrays.asList(faculty));

		RegisteredUser loaded =
				repository.loadUsers().get(0);

		assertTrue(loaded instanceof Faculty);
		assertEquals(30.0, loaded.getHourlyRate(), 0.001);
	}

	@Test
	public void saveAndLoadPartnerCreatesPartnerObject() {
		RegisteredUser partner = new Partner(
				"partner@gmail.com",
				"Strong1!",
				"PARTNER",
				"Partner User",
				987654321L);

		repository.saveUsers(Arrays.asList(partner));

		RegisteredUser loaded =
				repository.loadUsers().get(0);

		assertTrue(loaded instanceof Partner);
		assertEquals(50.0, loaded.getHourlyRate(), 0.001);
	}

	@Test
	public void saveAndLoadMultipleUsersPreservesOrder() {
		List<RegisteredUser> users = Arrays.asList(
				new Student(
						"student@yorku.ca",
						"Strong1!",
						"STUDENT",
						"Student",
						111111111L),

				new Staff(
						"staff@yorku.ca",
						"Strong1!",
						"STAFF",
						"Staff",
						222222222L),

				new Partner(
						"partner@gmail.com",
						"Strong1!",
						"PARTNER",
						"Partner",
						333333333L));

		repository.saveUsers(users);

		List<RegisteredUser> loaded =
				repository.loadUsers();

		assertEquals(3, loaded.size());
		assertEquals("student@yorku.ca", loaded.get(0).getEmail());
		assertEquals("staff@yorku.ca", loaded.get(1).getEmail());
		assertEquals("partner@gmail.com", loaded.get(2).getEmail());
	}

	@Test
	public void saveEmptyListCreatesLoadableEmptyFile() {
		repository.saveUsers(new ArrayList<>());

		List<RegisteredUser> loaded =
				repository.loadUsers();

		assertNotNull(loaded);
		assertTrue(loaded.isEmpty());
	}

	@Test
	public void saveUsersWritesExpectedHeader() throws Exception {
		repository.saveUsers(new ArrayList<>());

		String firstLine =
				Files.readAllLines(csvFile.toPath()).get(0);

		assertEquals(
				"accountType,email,password,userName,organizationId",
				firstLine);
	}

	@Test
	public void secondSaveOverwritesPreviouslySavedUsers() {
		RegisteredUser firstUser = new Student(
				"first@yorku.ca",
				"Strong1!",
				"STUDENT",
				"First User",
				111111111L);

		RegisteredUser secondUser = new Partner(
				"second@gmail.com",
				"Strong1!",
				"PARTNER",
				"Second User",
				222222222L);

		repository.saveUsers(Arrays.asList(firstUser));
		repository.saveUsers(Arrays.asList(secondUser));

		List<RegisteredUser> loaded =
				repository.loadUsers();

		assertEquals(1, loaded.size());
		assertEquals("second@gmail.com", loaded.get(0).getEmail());
	}

	@Test
	public void malformedOrganizationIdIsLoadedAsZero()
			throws Exception {

		String csv =
				"accountType,email,password,userName,organizationId\n"
				+ "STUDENT,student@yorku.ca,Strong1!,Student User,invalid\n";

		Files.writeString(csvFile.toPath(), csv);

		RegisteredUser loaded =
				repository.loadUsers().get(0);

		assertEquals(0L, loaded.getOrganizationId());
	}

	@Test
	public void blankOrganizationIdIsLoadedAsZero()
			throws Exception {

		String csv =
				"accountType,email,password,userName,organizationId\n"
				+ "PARTNER,partner@gmail.com,Strong1!,Partner User,\n";

		Files.writeString(csvFile.toPath(), csv);

		RegisteredUser loaded =
				repository.loadUsers().get(0);

		assertEquals(0L, loaded.getOrganizationId());
	}

	@Test(expected = IllegalArgumentException.class)
	public void unsupportedAccountTypeCausesException()
			throws Exception {

		String csv =
				"accountType,email,password,userName,organizationId\n"
				+ "ADMIN,admin@example.com,Strong1!,Admin User,123456789\n";

		Files.writeString(csvFile.toPath(), csv);

		repository.loadUsers();
	}

	@Test
	public void commasInsideUserNameArePreserved() {
		RegisteredUser user = new Partner(
				"partner@gmail.com",
				"Strong1!",
				"PARTNER",
				"Company, Incorporated",
				123456789L);

		repository.saveUsers(Arrays.asList(user));

		RegisteredUser loaded =
				repository.loadUsers().get(0);

		assertEquals(
				"Company, Incorporated",
				loaded.getUserName());
	}
}
