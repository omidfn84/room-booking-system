# Room Booking System

[![CI](https://github.com/omidfn84/room-booking-system/actions/workflows/ci.yml/badge.svg)](https://github.com/omidfn84/room-booking-system/actions/workflows/ci.yml)

A desktop application for booking conference rooms, built in Java with a Swing GUI and SQLite persistence. It started as a 6-person team project for a software design course, and I've since extended it (SQLite migration, CI). My own contributions are listed [below](#my-contributions).

**6 design patterns** · **783 automated tests, 97.1% line coverage** · **Swing GUI, SQLite, CI on every push**

---

## What it does

- Account registration with role-based pricing (student / faculty / staff / partner, each at a different hourly rate)
- Search and book available rooms for a given time window
- Deposit-based booking with check-in verification through a simulated room sensor system
- Edit, cancel, and extend bookings, with time-window rules enforced by the booking's state
- Three payment methods: credit card, debit card, institutional billing
- Admin panel for adding, enabling, disabling, and closing rooms, plus administrator account generation
- All data stored in an SQLite database and reloaded on restart (data from older CSV-based versions is imported automatically on first launch)

## Architecture

```
Swing panels → Controllers → SchedulerFacade → Managers (accounts / bookings / rooms)
                                                        │
                                              Repository interfaces
                                                        │
                                  SQLite adapters  (CSV adapters kept for legacy import)
```

Each layer only talks to the one below it. The GUI never touches the managers, and the managers never touch SQL or CSV code directly.

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
- **Known limits (it is a course project):** administrator accounts live in memory and log in with their admin ID alone, and the database file itself is not encrypted.

## Tech stack

- **Java 21+** with **Swing**
- **SQLite** via `sqlite-jdbc`, accessed only through the Adapter layer
- **JUnit 4** for tests, **JaCoCo** for coverage
- **GitHub Actions** CI: compiles and runs the full suite on Java 21 and 23 for every push and pull request

## Testing

Two suites, run together by CI and measured against all production code in `src/` (1,471 lines):

| Suite | Tests | Line coverage | Branch coverage |
|---|---|---|---|
| Core (`test/`), hand-written | 361 | 88.4% | 76.2% |
| AI-assisted (`test-ai/`) | 422 | 88.2% | 86.6% |
| **Combined** | **783** | **97.1%** | **91.5%** |

- The booking state machine and every payment strategy's validation rules are covered case by case
- Domain and GUI tests run against in-memory repository fakes; the persistence adapters are tested against temporary files and databases; the `MainUI` start-up tests run the real wiring end to end
- The Swing panels are tested too, including modal dialogs, which are answered programmatically by a small helper instead of being skipped. Some of these tests open real windows, so CI runs them under a virtual display (`xvfb`)
- See `test-ai/README.md` for how the AI-assisted suite was produced and checked

## My contributions

- **Swing GUI:** the login/register, booking, and admin panels, and the controllers connecting them to the Facade
- **Persistence layer:** the repository adapters (originally CSV), then the migration to SQLite with a one-time import of existing CSV data
- **CI:** the GitHub Actions workflow and `scripts/test.sh`, which runs the same build and test command locally and in CI
- **AI-assisted tests** for the booking, room, facade, GUI, and panels packages, including the helper that drives modal `JOptionPane` dialogs
- **Project structure and build setup**

The rest of the system (the account hierarchy and factory, the booking and payment rules, the Facade) was built by teammates.

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

test/      → core JUnit suite
test-ai/   → AI-assisted JUnit suite
lib/       → sqlite-jdbc.jar, javacsv.jar (included)
scripts/   → test.sh
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

To run the whole test suite (this is exactly what CI does):

```bash
scripts/test.sh
```

On Windows, run the commands from Git Bash or WSL. The classpath separator differs in plain `cmd`.