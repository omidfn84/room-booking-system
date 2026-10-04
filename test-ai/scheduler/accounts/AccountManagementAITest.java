package scheduler.accounts;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import scheduler.persistence.UserRepository;

public class AccountManagementAITest {

	private FakeUserRepository repository;
	private AccountManagement accountManagement;

	@Before
	public void setUp() {
		repository = new FakeUserRepository();
		accountManagement = new AccountManagement(repository);
	}

	@Test
	public void createAccountWithValidStudentInformationCreatesStudent() {
		RegisteredUser user = accountManagement.createAccount(
				"student@yorku.ca",
				"Strong1!",
				"STUDENT",
				"Melika",
				"123456789");

		assertNotNull(user);
		assertTrue(user instanceof Student);
		assertEquals("student@yorku.ca", user.getEmail());
		assertEquals("Strong1!", user.getPassword());
		assertEquals("STUDENT", user.getAccountType());
		assertEquals("Melika", user.getUserName());
		assertEquals(123456789L, user.getOrganizationId());
	}

	@Test
	public void createAccountRejectsUniversityEmailWithSurroundingSpaces() {
		assertIllegalArgument(
				() -> accountManagement.createAccount(
						"  STUDENT@YORKU.CA  ",
						"Strong1!",
						" student ",
						"  Melika  ",
						"123456789"),
				"University accounts must use a university email.");
	}

	@Test
	public void createPartnerAccountDoesNotRequireYorkEmail() {
		RegisteredUser user = accountManagement.createAccount(
				"partner@gmail.com",
				"Strong1!",
				"PARTNER",
				"External Partner",
				"123456789");

		assertNotNull(user);
		assertTrue(user instanceof Partner);
		assertEquals("partner@gmail.com", user.getEmail());
	}

	@Test
	public void universityAccountWithNonYorkEmailIsRejected() {
		assertIllegalArgument(
				() -> accountManagement.createAccount(
						"student@gmail.com",
						"Strong1!",
						"STUDENT",
						"Melika",
						"123456789"),
				"University accounts must use a university email.");
	}

	@Test
	public void invalidEmailFormatIsRejected() {
		assertIllegalArgument(
				() -> accountManagement.createAccount(
						"invalid-email",
						"Strong1!",
						"PARTNER",
						"Partner",
						"123456789"),
				"Email format is invalid.");
	}

	@Test
	public void blankEmailIsRejected() {
		assertIllegalArgument(
				() -> accountManagement.createAccount(
						" ",
						"Strong1!",
						"PARTNER",
						"Partner",
						"123456789"),
				"Email cannot be empty.");
	}

	@Test
	public void duplicateEmailIsRejectedIgnoringCaseAndSpaces() {
		accountManagement.createAccount(
				"student@yorku.ca",
				"Strong1!",
				"STUDENT",
				"First User",
				"123456789");

		assertIllegalArgument(
				() -> accountManagement.createAccount(
						"  STUDENT@YORKU.CA ",
						"Different1!",
						"STUDENT",
						"Second User",
						"987654321"),
				"This email is already registered.");
	}

	@Test
	public void passwordShorterThanEightCharactersIsRejected() {
		assertIllegalArgument(
				() -> createPartnerWithPassword("Ab1!"),
				"Password must contain at least 8 characters.");
	}

	@Test
	public void passwordWithoutUppercaseLetterIsRejected() {
		assertIllegalArgument(
				() -> createPartnerWithPassword("password1!"),
				"Password must contain at least one uppercase letter.");
	}

	@Test
	public void passwordWithoutLowercaseLetterIsRejected() {
		assertIllegalArgument(
				() -> createPartnerWithPassword("PASSWORD1!"),
				"Password must contain at least one lowercase letter.");
	}

	@Test
	public void passwordWithoutNumberIsRejected() {
		assertIllegalArgument(
				() -> createPartnerWithPassword("Password!"),
				"Password must contain at least one number.");
	}

	@Test
	public void passwordWithoutSymbolIsRejected() {
		assertIllegalArgument(
				() -> createPartnerWithPassword("Password1"),
				"Password must contain at least one symbol.");
	}

	@Test
	public void blankUserNameIsRejected() {
		assertIllegalArgument(
				() -> accountManagement.createAccount(
						"partner@gmail.com",
						"Strong1!",
						"PARTNER",
						" ",
						"123456789"),
				"Username cannot be empty.");
	}

	@Test
	public void invalidAccountTypeIsRejected() {
		assertIllegalArgument(
				() -> accountManagement.createAccount(
						"user@gmail.com",
						"Strong1!",
						"ADMIN",
						"User",
						"123456789"),
				"Unsupported account type: ADMIN");
	}

	@Test
	public void organizationIdWithFewerThanNineDigitsIsRejected() {
		assertIllegalArgument(
				() -> accountManagement.createAccount(
						"partner@gmail.com",
						"Strong1!",
						"PARTNER",
						"Partner",
						"12345"),
				"Organization ID must contain exactly 9 digits.");
	}

	@Test
	public void organizationIdContainingLettersIsRejected() {
		assertIllegalArgument(
				() -> accountManagement.createAccount(
						"partner@gmail.com",
						"Strong1!",
						"PARTNER",
						"Partner",
						"12345ABCD"),
				"Organization ID must contain exactly 9 digits.");
	}

	@Test
	public void createAccountSavesUpdatedUserListToRepository() {
		RegisteredUser user = accountManagement.createAccount(
				"student@yorku.ca",
				"Strong1!",
				"STUDENT",
				"Melika",
				"123456789");

		assertEquals(1, repository.saveCallCount);
		assertEquals(1, repository.savedUsers.size());
		assertSame(user, repository.savedUsers.get(0));
	}

	@Test
	public void findByEmailFindsUserIgnoringCaseAndSpaces() {
		RegisteredUser created = accountManagement.createAccount(
				"student@yorku.ca",
				"Strong1!",
				"STUDENT",
				"Melika",
				"123456789");

		RegisteredUser found =
				accountManagement.findByEmail("  STUDENT@YORKU.CA ");

		assertSame(created, found);
	}

	@Test
	public void findByEmailReturnsNullForMissingEmail() {
		assertNull(accountManagement.findByEmail("missing@yorku.ca"));
	}

	@Test
	public void findByEmailReturnsNullForNullOrBlankInput() {
		assertNull(accountManagement.findByEmail(null));
		assertNull(accountManagement.findByEmail(" "));
	}

	@Test
	public void constructorLoadsExistingUsersFromRepository() {
		RegisteredUser existingUser = new RegisteredUser(
				"existing@yorku.ca",
				"Strong1!",
				"STUDENT",
				"Existing User",
				123456789L) {

			@Override
			public double getHourlyRate() {
				return 0;
			}
		};

		FakeUserRepository preloadedRepository = new FakeUserRepository();
		preloadedRepository.usersToLoad.add(existingUser);

		AccountManagement loadedManagement =
				new AccountManagement(preloadedRepository);

		assertSame(existingUser,
				loadedManagement.findByEmail("EXISTING@YORKU.CA"));
	}

	@Test
	public void loadedEmailIsConsideredDuplicate() {
		RegisteredUser existingUser = new RegisteredUser(
				"existing@yorku.ca",
				"Strong1!",
				"STUDENT",
				"Existing User",
				123456789L) {

			@Override
			public double getHourlyRate() {
				return 0;
			}
		};

		FakeUserRepository preloadedRepository = new FakeUserRepository();
		preloadedRepository.usersToLoad.add(existingUser);

		AccountManagement loadedManagement =
				new AccountManagement(preloadedRepository);

		assertIllegalArgument(
				() -> loadedManagement.createAccount(
						"EXISTING@YORKU.CA",
						"Different1!",
						"STUDENT",
						"Another User",
						"987654321"),
				"This email is already registered.");
	}

	private void createPartnerWithPassword(String password) {
		accountManagement.createAccount(
				"partner@gmail.com",
				password,
				"PARTNER",
				"Partner",
				"123456789");
	}

	private void assertIllegalArgument(
			Runnable action, String expectedMessage) {

		try {
			action.run();
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException exception) {
			assertEquals(expectedMessage, exception.getMessage());
		}
	}

	private static class FakeUserRepository
			implements UserRepository {

		private final List<RegisteredUser> usersToLoad =
				new ArrayList<>();

		private List<RegisteredUser> savedUsers =
				new ArrayList<>();

		private int saveCallCount;

		@Override
		public List<RegisteredUser> loadUsers() {
			return new ArrayList<>(usersToLoad);
		}

		@Override
		public void saveUsers(List<RegisteredUser> users) {
			saveCallCount++;
			savedUsers = new ArrayList<>(users);
		}
	}
}
