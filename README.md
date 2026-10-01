# YorkU Conference Room Scheduler — Deliverable 2

A Java GUI desktop application for managing conference room bookings at York University. Users can register accounts, search and book available rooms, and manage their bookings. Administrators can manage rooms. All data is persisted to CSV files.

---

## How to set up in Eclipse

1. Clone the repository or unzip the project folder somewhere on your machine (not inside your Eclipse workspace folder).
2. Place `javacsv.jar` inside the `lib/` folder in the project root.
3. In Eclipse: `File → Import → General → Existing Projects into Workspace`
4. Select root directory → browse to the project folder → the project appears checked → **Finish**.
5. Wait for the build to finish (progress bar, bottom right).
6. Right-click `src/com/group10/scheduler/panels/MainUI.java` → **Run As → Java Application** to launch the GUI.

**If you see "Could not find or load main class":** open `Window → Show View → Problems`. The most common cause is a missing `javacsv.jar` in `lib/` — add it and do `Project → Clean...`.

---

## Running the application

The app creates a `data/` folder in the project root on first run and generates four CSV files there:

- `users.csv` — registered accounts
- `rooms.csv` — room inventory
- `bookings.csv` — all bookings
- `payments.csv` — payment records

Data persists between runs. Delete the CSV files to reset to a clean state.

### Login credentials for testing

Register a new account through the GUI (Login/Register tab) or run any of the demo files which pre-populate sample data.

---

## Project structure

```
src/
  com.group10.scheduler.accounts     → User hierarchy (RegisteredUser, Student, Faculty,
                                        Staff, Partner), RegisteredUserFactory,
                                        AccountManagement, Administrator,
                                        ChiefEventCoordinator
  com.group10.scheduler.booking      → Booking, BookingManager, Payment,
                                        BookingState interface, ConcreteStates,
                                        PaymentStrategy interface, ConcreteStrategies,
                                        BookingStatus, PaymentMethod, PaymentStatus
  com.group10.scheduler.room         → Room, RoomManager, RoomSensorSystem, RoomStatus
  com.group10.scheduler.facade       → SchedulerFacade
  com.group10.scheduler.persistence  → Repository interfaces (RoomRepository,
                                        BookingRepository, UserRepository, PaymentRepository)
  com.group10.scheduler.persistence.csv → CsvRoomRepository, CsvBookingRepository,
                                           CsvUserRepository, CsvPaymentRepository
  com.group10.scheduler.gui          → GUIController, AdminController
  com.group10.scheduler.panels       → MainUI, LoginPanel, BookingPanel, AdminPanel
  com.group10.scheduler.demo         → Req1Demo through Req10Demo, SmokeTest

lib/           → javacsv.jar (add manually — not committed to the repo)
data/          → CSV files created at runtime
demo-data/     → pre-populated CSV files used by the demo classes (one folder per requirement)
```

---

## Design patterns

Six design patterns are implemented in this project:

| Pattern | Where | What it does |
|---|---|---|
| **Factory Method** | `RegisteredUserFactory` | Creates the correct user subclass (Student/Faculty/Staff/Partner) based on account type. Callers never write `new Student()` directly. |
| **Singleton** | `ChiefEventCoordinator` | Guarantees exactly one chief event coordinator exists in the system. Only the chief can generate administrator accounts (Req2). |
| **Facade** | `SchedulerFacade` | Single entry point between the GUI and all subsystems. `GUIController` and `AdminController` only talk to the Facade — never to managers directly. |
| **State** | `Booking` + `ConcreteStates` | Each booking status (Confirmed, CheckedIn, Cancelled, Completed, Expired) is its own class enforcing its own transition rules. No if/else chains in `Booking`. |
| **Strategy** | `Payment` + `ConcreteStrategies` | Each payment method (CreditCard, DebitCard, InstitutionalBilling) is its own class. `Payment` calls `strategy.pay()` without knowing which method is being used. |
| **Adapter** | `CsvXxxRepository` classes | Wraps the third-party `javacsv` library (CsvReader/CsvWriter) behind clean `Repository` interfaces. Domain classes never import `com.csvreader`. |

---

## Requirements coverage

Each requirement from the project spec is covered as follows:

| Req | Description | How it is met |
|---|---|---|
| Req1 | Account creation with unique email and strong password | `AccountManagement.createAccount()` validates email uniqueness and password strength (uppercase, lowercase, digit, symbol, min 8 chars). University accounts require a `@yorku.ca` email. |
| Req2 | Only the chief event coordinator can generate administrator accounts | `ChiefEventCoordinator` (Singleton) is the only class that can instantiate `Administrator`. The Facade routes all admin generation through it. |
| Req3 | Registered users can book available rooms with varying hourly rates | `BookingManager.bookRoom()` checks room availability and sets the deposit based on the user's hourly rate ($20/$30/$40/$50). |
| Req4 | One hour deposit charged upfront; forfeited if no check-in within 30 minutes | Deposit is charged at booking. `ConfirmedState.expire()` calls `forfeitDeposit()` if the window is missed. `Booking.calculateRemainingBalance()` applies the deposit when the user did check in. |
| Req5 | Room sensors detect occupancy and scan ID badges during check-in | `BookingManager.checkIn()` calls `room.getSensorSystem().detectOccupancy()` and `scanIDBadge()` before allowing check-in. |
| Req6 | Admins can add, enable, disable, and close rooms | `Administrator` delegates to `RoomManager`. The Facade verifies the admin ID via the Singleton chief before allowing any room operation. |
| Req7 | Rooms have unique IDs, capacity, and location details | `Room` holds `roomId`, `capacity`, `building`, `roomNumber`, and `status`. All fields are persisted to `rooms.csv`. |
| Req8 | Bookings can be edited or cancelled before the start time | `ConfirmedState.cancel()` and `edit()` check `LocalDateTime.now().isBefore(startTime)` and return false if the window has passed. |
| Req9 | Bookings can be extended before expiry if the room is available | `extendIfBeforeExpiry()` in `ConcreteStates` checks the current end time has not passed and the new end time is later. `BookingManager.extendBooking()` checks room availability for the extended window. |
| Req10 | Supports credit card, debit card, and institutional billing | Three `PaymentStrategy` implementations handle each method. The Facade builds the correct strategy from the payment fields provided by the GUI. |

---

## Mutation Testing with PIT

Mutation testing checks the *quality* of our tests. PIT injects small bugs ("mutants") into the compiled code one at a time and re-runs the test suite:

- Tests fail → mutant **KILLED** (good, our tests caught the bug)
- Tests pass → mutant **SURVIVED** (bad, we have a blind spot)

The **mutation score** is the percentage of mutants killed.

### Prerequisites

1. **Build the project in Eclipse first.** The scripts read compiled classes
   from `bin/`, not from source. Run *Project → Clean* and make sure there are
   no errors in the Problems view.
2. **All tests must pass.** PIT refuses to run against a red suite — it can't
   tell a killed mutant from an already-broken test.
3. The PIT jars must be present in `pit-lib/`. If the folder is missing:
   ```bash
   mkdir -p pit-lib
   V=1.25.5
   for a in pitest pitest-entry pitest-command-line; do
     wget -P pit-lib https://repo1.maven.org/maven2/org/pitest/$a/$V/$a-$V.jar
   done
   ```

### Running

From the project root:

```bash
chmod +x part1Mutation.sh part2Mutation.sh   # first time only

./part1Mutation.sh    # our manually written tests
./part2Mutation.sh    # the Randoop-generated tests
```

| Script | Test suite analysed | Output |
|---|---|---|
| `part1Mutation.sh` | `com.group10.scheduler.*Test` | `pit-reports/manual/` + `pit-manual.log` |
| `part2Mutation.sh` | `randoopTests.RegressionTest*` | `pit-reports/randoop/` + `pit-randoop.log` |

GUI classes (`panels`, `gui`) are excluded from mutation, as are test classes and test fakes.

### Reading the results 

At the end of each run, look at the **Statistics** block:
 
```
>> Line Coverage (for mutated classes only): 774/827 (94%)
>> Generated 509 mutations Killed 452 (89%)
>> Mutations with no coverage 9. Test strength 90%
```

To see the HTML result:
```bash
xdg-open pit-reports/manual/index.html
xdg-open pit-reports/randoop/index.html
```


## Dependencies

- Java 17 or later
- `javacsv.jar` (place in `lib/` — not included in the repository)
- No other external dependencies
