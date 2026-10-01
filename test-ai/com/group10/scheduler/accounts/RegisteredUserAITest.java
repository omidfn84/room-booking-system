package com.group10.scheduler.accounts;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class RegisteredUserAITest {

	private RegisteredUser user;

	@Before
	public void setUp() {
		user = new Student(
				"student@yorku.ca",
				"Strong1!",
				"STUDENT",
				"Student User",
				123456789L);
	}

	@Test
	public void constructorStoresAllProvidedValues() {
		assertEquals("student@yorku.ca", user.getEmail());
		assertEquals("Strong1!", user.getPassword());
		assertEquals("STUDENT", user.getAccountType());
		assertEquals("Student User", user.getUserName());
		assertEquals(123456789L, user.getOrganizationId());
	}

	@Test
	public void setAccountTypeUpdatesAccountType() {
		user.setAccountType("STAFF");

		assertEquals("STAFF", user.getAccountType());
	}

	@Test
	public void setOrganizationIdUpdatesOrganizationId() {
		user.setOrganizationId(987654321L);

		assertEquals(987654321L, user.getOrganizationId());
	}

	@Test
	public void studentReturnsCorrectHourlyRate() {
		assertEquals(20.0, user.getHourlyRate(), 0.001);
	}

	@Test
	public void staffReturnsCorrectHourlyRate() {
		RegisteredUser staff = new Staff(
				"staff@yorku.ca",
				"Strong1!",
				"STAFF",
				"Staff User",
				123456789L);

		assertEquals(40.0, staff.getHourlyRate(), 0.001);
	}

	@Test
	public void facultyReturnsCorrectHourlyRate() {
		RegisteredUser faculty = new Faculty(
				"faculty@yorku.ca",
				"Strong1!",
				"FACULTY",
				"Faculty User",
				123456789L);

		assertEquals(30.0, faculty.getHourlyRate(), 0.001);
	}

	@Test
	public void partnerReturnsCorrectHourlyRate() {
		RegisteredUser partner = new Partner(
				"partner@gmail.com",
				"Strong1!",
				"PARTNER",
				"Partner User",
				123456789L);

		assertEquals(50.0, partner.getHourlyRate(), 0.001);
	}

	@Test
	public void toStringContainsUserInformation() {
		String result = user.toString();

		assertTrue(result.contains("STUDENT"));
		assertTrue(result.contains("Student User"));
		assertTrue(result.contains("student@yorku.ca"));
		assertTrue(result.contains("rate=$20.0/hr"));
		assertTrue(result.contains("orgId=123456789"));
	}

	@Test
	public void toStringReflectsUpdatedValues() {
		user.setAccountType("UPDATED");
		user.setOrganizationId(987654321L);

		String result = user.toString();

		assertTrue(result.contains("UPDATED"));
		assertTrue(result.contains("orgId=987654321"));
	}
}
