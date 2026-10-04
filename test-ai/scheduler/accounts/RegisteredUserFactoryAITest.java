package scheduler.accounts;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class RegisteredUserFactoryAITest {

	private RegisteredUserFactory factory;

	@Before
	public void setUp() {
		factory = new RegisteredUserFactory();
	}

	@Test
	public void createsStudentForStudentAccountType() {
		RegisteredUser user = factory.createUser(
				"student@yorku.ca",
				"Strong1!",
				"STUDENT",
				"Student User",
				123456789L);

		assertTrue(user instanceof Student);
		assertEquals(20.0, user.getHourlyRate(), 0.001);
	}

	@Test
	public void createsStaffForStaffAccountType() {
		RegisteredUser user = factory.createUser(
				"staff@yorku.ca",
				"Strong1!",
				"STAFF",
				"Staff User",
				123456789L);

		assertTrue(user instanceof Staff);
		assertEquals(40.0, user.getHourlyRate(), 0.001);
	}

	@Test
	public void createsFacultyForFacultyAccountType() {
		RegisteredUser user = factory.createUser(
				"faculty@yorku.ca",
				"Strong1!",
				"FACULTY",
				"Faculty User",
				123456789L);

		assertTrue(user instanceof Faculty);
		assertEquals(30.0, user.getHourlyRate(), 0.001);
	}

	@Test
	public void createsPartnerForPartnerAccountType() {
		RegisteredUser user = factory.createUser(
				"partner@gmail.com",
				"Strong1!",
				"PARTNER",
				"Partner User",
				123456789L);

		assertTrue(user instanceof Partner);
		assertEquals(50.0, user.getHourlyRate(), 0.001);
	}

	@Test
	public void accountTypeIsCaseInsensitiveAndTrimmed() {
		RegisteredUser user = factory.createUser(
				"student@yorku.ca",
				"Strong1!",
				"  student  ",
				"Student User",
				123456789L);

		assertTrue(user instanceof Student);
	}

	@Test
	public void createdUserContainsProvidedInformation() {
		RegisteredUser user = factory.createUser(
				"student@yorku.ca",
				"Strong1!",
				"STUDENT",
				"Student User",
				123456789L);

		assertEquals("student@yorku.ca", user.getEmail());
		assertEquals("Strong1!", user.getPassword());
		assertEquals("STUDENT", user.getAccountType());
		assertEquals("Student User", user.getUserName());
		assertEquals(123456789L, user.getOrganizationId());
	}

	@Test
	public void unsupportedAccountTypeThrowsException() {
		try {
			factory.createUser(
					"user@example.com",
					"Strong1!",
					"ADMIN",
					"User",
					123456789L);

			fail("Expected IllegalArgumentException");

		} catch (IllegalArgumentException exception) {
			assertEquals(
					"Unsupported account type: ADMIN",
					exception.getMessage());
		}
	}
}
