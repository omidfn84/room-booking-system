package scheduler.web;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.TimeZone;

import scheduler.accounts.ChiefEventCoordinator;

/**
 *
 * Settings come from environment variables, because that is how hosting
 * platforms pass them in:
 *
 *   PORT                      port to listen on (default 8080; hosts set this themselves)
 *   SCHEDULER_DEMO            "false" switches demo mode off (default: on)
 *   SCHEDULER_TZ              the campus time zone (default America/Toronto)
 *   SCHEDULER_DATA_DIR        folder for scheduler.db when demo mode is off (default data)
 *   SCHEDULER_PUBLIC_DIR      folder with the page files (default web/public)
 *   SCHEDULER_CHIEF_PASSWORD  the chief's password (default: a random one per run)
 */
public final class WebMain {

    private WebMain() {}

    public static void main(String[] args) {
        // The domain compares booking times with LocalDateTime.now(), which uses the
        // JVM's time zone. A cloud server usually runs in UTC, so without this line
        // "10:00" would mean 10:00 UTC and check-in windows would be hours off.
        TimeZone.setDefault(TimeZone.getTimeZone(env("SCHEDULER_TZ", "America/Toronto")));

        // Ask the JDK's HTTP server to drop clients that take longer than 10 seconds
        // to send a request or to read the answer, so stalled connections cannot pile up.
        System.setProperty("sun.net.httpserver.maxReqTime", "10");
        System.setProperty("sun.net.httpserver.maxRspTime", "10");

        WebApp.Config config = new WebApp.Config();
        config.port = Integer.parseInt(env("PORT", "8080"));
        config.demo = !env("SCHEDULER_DEMO", "true").equalsIgnoreCase("false");
        config.dataDir = env("SCHEDULER_DATA_DIR", "data");
        config.publicDir = env("SCHEDULER_PUBLIC_DIR", "web/public");
        config.chiefPassword = configureChiefPassword();

        WebApp app = WebApp.start(config);
        // When the process is asked to stop (Ctrl+C, or the host shutting the container
        // down), stop the server cleanly; in demo mode this also deletes the temporary database.
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
        System.out.println("Room Booking System (" + (config.demo ? "demo mode" : "full mode") + ", times in "
                + TimeZone.getDefault().getID() + ") is running at http://localhost:" + app.port());
    }

    // Same rule as MainUI: the chief password is set once per JVM, from the
    // environment or, failing that, a random value that exists only for this run.
    private static String configureChiefPassword() {
        String password = System.getenv("SCHEDULER_CHIEF_PASSWORD");
        if (password == null || password.isBlank()) {
            byte[] bytes = new byte[18];
            new SecureRandom().nextBytes(bytes);
            password = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }
        ChiefEventCoordinator.getInstance().configureCredential(password);
        return password;
    }

    // Reads an environment variable, falling back to a default when it is unset or blank.
    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
