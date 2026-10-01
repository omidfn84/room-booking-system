# AI-Assistant Generated Test Suite (`test-ai/`)

Deliverable 3, Task 3. This folder holds the test cases produced with an
AI assistant, kept separate from the manually written suite in `test/` and the
Randoop suite in `test/randoopTests/` so that each can be measured on its own.

## Scope

This suite covers the packages assigned to this team member:

| Package | Covered here |
|---|---|
| `com.group10.scheduler.booking` | yes |
| `com.group10.scheduler.room` | yes |
| `com.group10.scheduler.facade` | yes |
| `com.group10.scheduler.gui` | yes |
| `com.group10.scheduler.panels` | yes |
| `com.group10.scheduler.accounts` | no — other team member |
| `com.group10.scheduler.persistence` / `.csv` | no — other team member |
| `com.group10.scheduler.demo` | no — demo drivers, not under test |

## Results

322 tests, all passing. Measured with JaCoCo over the five packages above.

| Package | Line coverage |
|---|---|
| `booking` | 98.1% (368/375) |
| `room` | 100% (89/89) |
| `facade` | 100% (32/32) |
| `gui` | 100% (38/38) |
| `panels` | 96.8% (395/408) |
| **Total** | **97.9% line (922/942), 91.4% branch (371/406)** |

Every class is at or above 95%, comfortably clearing the 80% requirement.
Each test class contains at least 14 tests, above the 10-per-class minimum.

| Test class | Tests |
|---|---|
| `booking/BookingAITest` | 18 |
| `booking/BookingManagerAITest` | 46 |
| `booking/ConcreteStatesAITest` | 21 |
| `booking/ConcreteStrategiesAITest` | 18 |
| `booking/PaymentAITest` | 16 |
| `room/RoomAITest` | 15 |
| `room/RoomManagerAITest` | 18 |
| `room/RoomSensorSystemAITest` | 14 |
| `facade/SchedulerFacadeAITest` | 20 |
| `gui/GUIControllerAITest` | 20 |
| `gui/AdminControllerAITest` | 19 |
| `panels/AdminPanelAITest` | 28 |
| `panels/BookingPanelAITest` | 32 |
| `panels/LoginPanelAITest` | 20 |
| `panels/MainUIAITest` | 17 |

## How to run in Eclipse

`test-ai` is registered as a source folder in `.classpath`, so Eclipse picks it
up automatically after a refresh (F5).

- **Run everything:** right-click the `test-ai` folder → `Run As → JUnit Test`
- **Coverage screenshot:** right-click `test-ai` → `Coverage As → JUnit Test`
  (requires the EclEmma plugin, which ships with recent Eclipse). The Coverage
  view then shows the per-package percentages used in the report.

A pre-generated JaCoCo HTML report is also checked in at
`coverage-reports/ai-assistant/index.html` if you would rather screenshot that.

## Note on the GUI tests

`MainUIAITest` and the dialog-driven tests inside `BookingPanelAITest` need a
display, because `MainUI` extends `JFrame` and four `BookingPanel` handlers
open a modal `JOptionPane`. They are guarded with JUnit's `Assume`, so:

- run from Eclipse on a normal desktop → they execute
- run on a headless server → they skip cleanly instead of failing

`AIDialogs` is the helper that makes the modal handlers testable: it arms a
background watcher before the button is clicked, then fills in and dismisses
the dialog from the Event Dispatch Thread. This is what allowed
`BookingPanel` to reach 97% rather than the ~59% the manual suite reaches,
since the manual suite deliberately skips those four handlers.

## Support classes

- `aisupport/AIFakes` — in-memory doubles for the four repository interfaces,
  so no test touches a real CSV file
- `aisupport/AIFixture` — builds a fully wired system (real managers, fake
  repositories), plus time helpers and unique admin ids for the
  `ChiefEventCoordinator` Singleton
- `aisupport/AIDialogs` — auto-dismisses modal Swing dialogs

These are declared here rather than reused from `test/` so this suite compiles
and runs completely independently of the manual one.
