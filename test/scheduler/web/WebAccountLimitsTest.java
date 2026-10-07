package scheduler.web;

import static org.junit.Assert.*;
import static scheduler.web.WebTestClient.json;

import org.junit.Test;

import scheduler.accounts.TestChief;
import scheduler.web.WebTestClient.Response;

/**
 * The two limits that keep an open sign-up from being abused on the public
 * demo site: a cap on password attempts per minute, and a cap on new accounts.
 * Each test starts its own small server with a low limit.
 */
public class WebAccountLimitsTest {

    private static WebApp start(int attemptsPerMinute, int maxNewAccounts) {
        WebApp.Config config = new WebApp.Config();
        config.port = 0;
        config.demo = true;
        config.chiefPassword = TestChief.password();
        config.passwordAttemptsPerMinute = attemptsPerMinute;
        config.maxNewAccountsInDemo = maxNewAccounts;
        return WebApp.start(config);
    }

    private static String registration(String email) {
        return json("email", email, "password", "Str0ng!Pass", "userName", "Someone", "accountType", "PARTNER", "organizationId", "123456789");
    }

    @Test
    public void logInAttemptsAreCappedPerMinuteButTheDemoStaysOpen() throws Exception {
        WebApp app = start(3, 200);
        try {
            WebTestClient client = new WebTestClient(app.port());
            String wrong = json("email", "nobody@example.com", "password", "Wrong!Pass1");
            assertEquals(401, client.post("/api/login", wrong).status);
            assertEquals(401, client.post("/api/login", wrong).status);
            assertEquals(401, client.post("/api/login", wrong).status);

            Response fourth = client.post("/api/login", wrong);
            assertEquals("the 4th attempt in a minute is turned away", 429, fourth.status);
            assertTrue(fourth.error(), fourth.error().contains("Wait a minute"));
            assertEquals("sign-up shares the same limit", 429, client.post("/api/register", registration("a@example.com")).status);
            assertEquals("another visitor is limited too: the cap is for the whole server", 429,
                    new WebTestClient(app.port()).post("/api/login", wrong).status);

            // the one-click demo involves no password, so it is never limited
            assertEquals(200, new WebTestClient(app.port()).post("/api/demo-login", json("account", "student")).status);
            assertEquals(200, new WebTestClient(app.port()).post("/api/demo-login", json("account", "admin")).status);
        } finally {
            app.stop();
        }
    }

    @Test
    public void signUpClosesOnceTheDemoSiteHasEnoughNewAccounts() throws Exception {
        WebApp app = start(100_000, 2);
        try {
            assertEquals(200, new WebTestClient(app.port()).post("/api/register", registration("one@example.com")).status);
            // a rejected sign-up (weak password) does not use up a place
            assertEquals(400, new WebTestClient(app.port()).post("/api/register",
                    json("email", "weak@example.com", "password", "short", "userName", "W", "accountType", "PARTNER", "organizationId", "123456789")).status);
            assertEquals(200, new WebTestClient(app.port()).post("/api/register", registration("two@example.com")).status);

            Response third = new WebTestClient(app.port()).post("/api/register", registration("three@example.com"));
            assertEquals(409, third.status);
            assertTrue(third.error(), third.error().contains("demo account"));

            // people who already have an account can still log in, and the demo still works
            assertEquals(200, new WebTestClient(app.port()).post("/api/login", json("email", "one@example.com", "password", "Str0ng!Pass")).status);
            assertEquals(200, new WebTestClient(app.port()).post("/api/demo-login", json("account", "partner")).status);
        } finally {
            app.stop();
        }
    }

    @Test
    public void fullModeHasNoCapOnNewAccounts() throws Exception {
        java.io.File dir = java.nio.file.Files.createTempDirectory("scheduler-limit-test-").toFile();
        WebApp.Config config = new WebApp.Config();
        config.port = 0;
        config.demo = false;
        config.dataDir = dir.getAbsolutePath();
        config.passwordAttemptsPerMinute = 100_000;
        config.maxNewAccountsInDemo = 1;                       // only applies to the demo site
        WebApp app = WebApp.start(config);
        try {
            assertEquals(200, new WebTestClient(app.port()).post("/api/register", registration("one@example.com")).status);
            assertEquals(200, new WebTestClient(app.port()).post("/api/register", registration("two@example.com")).status);
        } finally {
            app.stop();
            new java.io.File(dir, "scheduler.db").delete();    // remove the temporary database this test created
            dir.delete();
        }
    }
}
