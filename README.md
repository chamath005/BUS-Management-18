# Bus Management System (Unified Full-Stack Application)

## Architecture Overview
- **One Entry Point (`src/Main.java`)**: Starts a unified HTTP server on `http://localhost:8080`, loads `shared-data/`, serves the unified Login Page (`web/index.html`), and dynamically mounts whichever role modules are present (`Owner`, `Driver`, `Conductor`, `Passenger`).
- **One Login Page (`web/index.html`)**: Authenticates all 4 roles (Owner, Driver, Conductor, Passenger), supports new account registration, and redirects automatically to each role's workspace:
  - Owner -> `/owner.html` (`/api/owner/*`)
  - Driver -> `/driver.html` (`/api/driver/*`)
  - Conductor -> `/conductor.html` (`/api/conductor/*`)
  - Passenger -> `/passenger.html` (`/api/passenger/*`)
- **Pre-Populated `shared-data/` Folder**: Included directly inside every ZIP with all 20 `.txt` files ready to run immediately.

## Demo Accounts
| Role | Email / Username | Password |
| :--- | :--- | :--- |
| **Bus Owner** | `test@gmail.com` | `1234` |
| **Driver** | `kamal@gmail.com` | `12345` |
| **Conductor** | `gimhan@example.com` | `demo123` |
| **Passenger** | `passenger@gmail.com` | `1234` |

## Git Team Workflow (Zero Merge Conflicts)
Each of the 4 module ZIPs (`1-OwnerModule.zip`, `2-DriverModule.zip`, `3-ConductorModule.zip`, `4-PassengerModule.zip`) contains:
1. The exact same shared core files (`src/Main.java`, `src/common/*`, `web/index.html`, `web/style.css`, `run-busmanager.bat`, `BusManagementSystem.iml`, `shared-data/*.txt`).
2. Only that team member's isolated package (`src/owner/`, `src/driver/`, `src/conductor/`, or `src/passenger/`) and dashboard files (`web/<role>.html`).

When all 4 branches are merged in Git (`git merge branch-owner branch-driver branch-conductor branch-passenger`), all 4 modules combine cleanly with **zero merge conflicts** into the complete working application.

## How to Run
- **Windows**: Extract the ZIP and double-click `run-busmanager.bat`.
- **IntelliJ IDEA**: Open the extracted `BusManagementSystem` folder and run `src/Main.java` (JDK 17+).
