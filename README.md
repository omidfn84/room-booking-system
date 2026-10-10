# Room Booking System

[![CI](https://github.com/omidfn84/room-booking-system/actions/workflows/ci.yml/badge.svg)](https://github.com/omidfn84/room-booking-system/actions/workflows/ci.yml)

A conference room booking system in Java with two front ends on one core: a Swing desktop app and a browser version with a JSON API. Data is stored in SQLite.

**6 design patterns** · **891 automated tests** · **Swing GUI and web version, SQLite, CI on every push**

<!-- Once the web version is deployed, replace the address and uncomment:
**Live demo:** https://YOUR-APP-ADDRESS (no sign-up needed: choose "Try the demo" on the first page)
-->
Live Demo:

https://github.com/user-attachments/assets/8be49672-d691-475f-a59a-cfe73daa4e31



---

## What it does

- Account registration with role-based pricing (student / faculty / staff / partner, each at a different hourly rate)
- Search and book available rooms for a given time window
- Deposit-based booking with check-in verification through a simulated room sensor system
- Edit, cancel, and extend bookings, with time-window rules enforced by the booking's state
- Three payment methods: credit card, debit card, institutional billing
- Admin panel for adding, enabling, disabling, and closing rooms, plus administrator account generation
- A browser version of the same features. Its first page offers two ways in: one-click demo accounts that need no sign-up, or logging in with your own account. Paying has its own screen
- All data stored in an SQLite database and reloaded on restart (data from older CSV-based versions is imported automatically on first launch)

## Architecture

```
Swing panels → Controllers ─┐
                            ├→ SchedulerFacade → Managers (accounts / bookings / rooms)
Browser → JSON API ─────────┘                              │
                                                 Repository interfaces
                                                           │
                                     SQLite adapters  (CSV adapters kept for legacy import)
```

Each layer only talks to the one below it. The GUI never touches the managers, and the managers never touch SQL or CSV code directly.

The web version was added without changing any existing source file. It is one new package (`scheduler.web`) that calls the same `SchedulerFacade` as the Swing controllers, plus three static files for the page.

## Design patterns

| Pattern | Where | What it solves |
|---|---|---|
| **Factory Method** | `RegisteredUserFactory` | Builds the correct user subclass (Student / Faculty / Staff / Partner) from an account-type string, so callers never instantiate a concrete user class directly |
| **Singleton** | `ChiefEventCoordinator` | Guarantees a single authority exists to generate administrator accounts |
| **Facade** | `SchedulerFacade` | One entry point between the GUI and every subsystem |
| **State** | `Booking` + `ConcreteStates` | Each booking status (Confirmed / CheckedIn / Cancelled / Completed / Expired) is its own class enforcing its own transition rules |
| **Strategy** | `Payment` + `ConcreteStrategies` | Each payment method validates and processes itself; `Payment` never branches on which one it holds |
| **Adapter** | `SqliteXxxRepository` / `CsvXxxRepository` | Wraps JDBC/SQLite (and the legacy CSV library) behind repository interfaces. Moving from CSV to SQL meant writing new adapters and changing the wiring in `MainUI.main()`; the domain classes were not touched |

## Security

- **Passwords are never stored in plain text.** `PasswordHasher` uses salted PBKDF2-HMAC-SHA512 (210,000 iterations, built into the JDK), and login compares hashes in constant time. A failed login takes the same time whether or not the email exists. Plain-text passwords left by older versions are hashed automatically on start-up.
- **Creating an administrator requires the chief password.** The Singleton guarantees there is one chief, and the password proves the person using it *is* the chief. It is set once at start-up from `SCHEDULER_CHIEF_PASSWORD`. If that is not set, a random one is generated and printed to the console. Only its hash is kept in memory, and it cannot be changed while the app runs.
- **Web sessions:** the browser holds only a random 256-bit token in an `HttpOnly`, `SameSite=Lax` cookie (also `Secure` behind https). Who the token belongs to stays on the server.
- **A user can only act on their own bookings.** Every booking request is checked against the signed-in user, and someone else's booking answers "not found", the same as an id that does not exist.
- **The public demo keeps no card details.** Demo accounts pay with made-up details filled in by the server. Someone using their own account types card details, which are checked for the right shape and then discarded: the server hands the made-up details to the payment code instead, so nothing typed is kept in memory or written to the database. The payment screen also tells people not to enter a real card.
- **Open sign-up on the demo site is bounded.** Log-in and sign-up attempts are capped per minute for the whole server, because checking a password is deliberately slow, and the number of new accounts is capped. The one-click demo accounts are not affected. Demo data lives in a temporary database that is deleted on shutdown.
- **The page cannot run injected content.** All text is inserted as text, never as HTML, and a Content-Security-Policy allows scripts and styles from the site itself only.
- **Known limits (it is a course project):** administrator accounts live in memory and log in with their admin ID alone (on the web, the only administrator is the demo one), the database file itself is not encrypted, and apart from the cap on log-in attempts the web version has no per-visitor rate limiting.

## Tech stack

- **Java 21+** with **Swing**
- **Web version:** the HTTP server built into the JDK (`com.sun.net.httpserver`) and plain HTML, CSS and JavaScript. No web framework, no build step, no new dependency
- **SQLite** via `sqlite-jdbc`, accessed only through the Adapter layer
- **JUnit 4** for tests, **JaCoCo** for coverage
- **GitHub Actions** CI: compiles and runs the full suite on Java 21 and 23 for every push and pull request

## Testing

All suites are run together by CI: **891 tests**.

| Suite | Tests | Line coverage | Branch coverage |
|---|---|---|---|
| Core (`test/`), hand-written | 361 | 88.4% | 76.2% |
| AI-assisted (`test-ai/`) | 422 | 88.2% | 86.6% |
| **Core + AI-assisted combined** | **783** | **97.1%** | **91.5%** |
| Web layer (`test/scheduler/web/`), AI-assisted | 108 | not measured yet | not measured yet |

The coverage figures were measured against the production code as it was before the web package was added (1,471 lines); they do not include `scheduler.web`.

- The booking state machine and every payment strategy's validation rules are covered case by case
- Domain and GUI tests run against in-memory repository fakes; the persistence adapters are tested against temporary files and databases; the `MainUI` start-up tests run the real wiring end to end
- The Swing panels are tested too, including modal dialogs, which are answered programmatically by a small helper instead of being skipped. Some of these tests open real windows, so CI runs them under a virtual display (`xvfb`)
- The web tests start a real server on a free port and drive it over HTTP like a browser: the full book → check in → pay flow, one user trying to change another's booking, parallel requests for the same room, malformed input, and attempts to read files outside the public folder
- See `test-ai/README.md` for how the AI-assisted suite was produced and checked


## Project structure

```
src/scheduler/
  accounts/      → user hierarchy, account management, admin / chief coordinator
  booking/       → Booking, Payment, the State and Strategy patterns
  room/          → Room, RoomManager, simulated sensor system
  persistence/   → repository interfaces
    sql/         → SQLite adapters (used by the app) + one-time CSV import
    csv/         → legacy CSV adapters (used only to import old data)
  facade/        → SchedulerFacade
  gui/           → controllers bridging the panels and the Facade
  panels/        → Swing screens + MainUI entry point
  web/           → JSON API, sessions, static files, demo data + WebMain entry point

web/public/  → the browser page: index.html, app.js, styles.css

test/      → core JUnit suite
test-ai/   → AI-assisted JUnit suite
lib/       → sqlite-jdbc.jar, javacsv.jar (included)
scripts/   → test.sh, run-web.sh
Dockerfile → container build of the web version, for hosting
```

## Running it

From a terminal, in the project folder:

```bash
mkdir -p build/classes                                    # output folder for compiled classes
javac -d build/classes -cp "lib/*" $(find src -name '*.java')   # compile all source files, with the jars in lib/ on the classpath
java --enable-native-access=ALL-UNNAMED -cp "build/classes:lib/*" scheduler.panels.MainUI   # launch the app (the flag silences a SQLite warning on newer Java)
```

Or open the folder in Eclipse or IntelliJ, mark `src` as the sources root, and run `MainUI.java`.

To generate administrators you need the chief password. Choose your own by setting an environment variable before launching:

```bash
export SCHEDULER_CHIEF_PASSWORD='choose-a-strong-password'
```

If it isn't set, the app prints a one-time chief password to the console at start-up.

The app creates `data/scheduler.db` on first launch. If an old `data/` folder still has `rooms.csv`, `users.csv`, `bookings.csv`, or `payments.csv`, they are imported once, automatically.

### The web version

```bash
scripts/run-web.sh        # compiles, then serves http://localhost:8080
```

It starts in **demo mode**: the first page offers "Try the demo" (one-click student, faculty, partner and administrator accounts) next to "Log in" (your own account, which you can create there). All data lives in a temporary database that is deleted when the server stops. Settings are environment variables:

| Variable | Default | Meaning |
|---|---|---|
| `PORT` | `8080` | Port to listen on (hosting platforms set this themselves) |
| `SCHEDULER_DEMO` | `true` | `false` turns demo mode off: no demo accounts, typed payment details are passed to the payment code, and data is kept in `data/scheduler.db` (the same file the desktop app uses, so run only one of the two at a time) |
| `SCHEDULER_TZ` | `America/Toronto` | The campus time zone. Booking times are wall-clock times in this zone, whatever zone the server runs in |

To host it, deploy the `Dockerfile` to any platform that runs containers.

To run the whole test suite (this is exactly what CI does):

```bash
scripts/test.sh
```

On Windows, run the commands from Git Bash or WSL. The classpath separator differs in plain `cmd`.
