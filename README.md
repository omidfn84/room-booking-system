# Room Booking System

A full-stack desktop application for booking conference rooms, built in Java with a Swing GUI and a custom CSV-based persistence layer. Originally developed as a 6-person team project for a software design course; this repository reflects the layers I was individually responsible for — booking, room management, the Facade entry point, the GUI, and the automated test suite.

**6 design patterns** · **322 JUnit tests, 97.9% line coverage** · **Pure Java + Swing, no external frameworks**

<!-- Demo GIF — record a 20–30s walkthrough: register → search rooms → book → check in → admin manage rooms -->
<!-- ![Demo](docs/demo.gif) -->

---

## What it does

- Account registration with role-based pricing (student / faculty / staff / partner, each at a different hourly rate)
- Search and book available rooms for a given time window
- Deposit-based booking with check-in verification through a simulated room sensor system
- Edit, cancel, and extend bookings, with time-window business rules enforced at the state level
- Three payment methods — credit card, debit card, institutional billing
- Admin panel for adding, enabling, disabling, and closing rooms, plus administrator account generation
- All data persisted to CSV files and correctly reloaded on restart

## Design patterns

| Pattern | Where | What it solves |
|---|---|---|
| **Factory Method** | `RegisteredUserFactory` | Builds the correct user subclass (Student/Faculty/Staff/Partner) from an account-type string — callers never instantiate a concrete user class directly |
| **Singleton** | `ChiefEventCoordinator` | Guarantees a single authority exists to generate administrator accounts |
| **Facade** | `SchedulerFacade` | One entry point between the GUI and every subsystem — the GUI never talks to the domain managers directly |
| **State** | `Booking` + `ConcreteStates` | Each booking status (Confirmed / CheckedIn / Cancelled / Completed / Expired) is its own class enforcing its own transition rules |
| **Strategy** | `Payment` + `ConcreteStrategies` | Each payment method validates and processes itself; `Payment` never branches on which one it holds |
| **Adapter** | `CsvXxxRepository` classes | Wraps a third-party CSV library behind clean repository interfaces, so the domain layer never imports CSV-specific code |

## Tech stack

- **Java** with **Swing** for the GUI
- **JUnit 4**, with a fully self-contained, in-memory test suite — no test touches the real filesystem
- **JaCoCo / EclEmma** for coverage measurement
- **CSV files** as the persistence layer, accessed only through the hand-written Adapter layer above

## Testing

- **322 automated tests, 97.9% line coverage / 91.4% branch coverage**
- Full lifecycle coverage of the booking state machine and every payment strategy's validation rules
- The Swing panels themselves are under test, including modal dialog interactions driven programmatically rather than skipped
- See `test/` for the core suite and `test-ai/README.md` for notes on the AI-assisted portion of test generation

## Project structure

```
src/com/group10/scheduler/
  accounts/      → user hierarchy, account management, admin/chief coordinator
  booking/       → Booking, Payment, the State pattern, the Strategy pattern
  room/          → Room, RoomManager, simulated sensor system
  persistence/   → repository interfaces + CSV adapter implementations
  facade/        → SchedulerFacade — the single entry point
  gui/           → controllers bridging the GUI and the Facade
  panels/        → Swing screens (login, booking, admin) + MainUI entry point

test/      → core JUnit test suite
test-ai/   → AI-assisted test suite, measured separately (see its own README)
lib/       → third-party dependency (javacsv.jar, included)
```

## Running it locally

1. Clone the repo — `javacsv.jar` is already included in `lib/`
2. Open in your IDE of choice (tested in Eclipse and IntelliJ) and mark `src` as a sources root and `test` / `test-ai` as test sources roots
3. Add JUnit 4 to the project's libraries if your IDE doesn't resolve it automatically
4. Run `src/com/group10/scheduler/panels/MainUI.java` as a Java application

The app creates a `data/` folder on first launch and persists every record there as CSV files.

## Background
