# Online Complaint Management System (Java)

This project ships as **two complete, independent builds** in one repository, matching the two tracks in the original spec:

| Build | Location | What it is |
|---|---|---|
| **Console App** | `src/`, `data/`, `logs/`, `outputs/` | Core Java, OOP, file-handling console application (Option B — recommended for students) |
| **REST API + Dashboard** | `backend/`, `dashboard/` | Spring Boot JSON API with a visual web dashboard (placement/demo-ready) |

Pick whichever you need, or use both — they don't share code or data and can be demoed independently. `docs/` and the screenshots checklist apply mainly to the console build; see `README.md` inside `backend/` for the API build's own run instructions.

---

## Quick start

**Console version:**
```bash
javac -d bin src/model/*.java src/exception/*.java src/utility/*.java src/repository/*.java src/service/*.java src/main/*.java
java -cp bin main.Main
```

**API + Dashboard version:**
```bash
cd backend
mvn spring-boot:run
```
Then open `dashboard/index.html` in your browser. Demo logins: `admin@example.com` / `admin` (admin), `user@example.com` / `user` (student).

---

## Console App details

## 1. Project Overview

This project digitizes the manual complaint-handling process used by customer support desks, colleges, housing societies, and government grievance portals. Instead of complaints being tracked on paper or scattered emails, every complaint gets a unique ID, a defined lifecycle, and an auditable history from submission to closure.

## 2. Problem Statement

Manual complaint handling is slow and unaccountable:
- No single source of truth for complaint status
- No way to prioritize urgent issues over minor ones
- No record of who resolved what, or how
- Users have no visibility into progress on their own complaints

This system solves that with structured registration, role-based actions, and a defined status lifecycle.

## 3. Industry Relevance

Ticket-based systems following this same pattern are used in:
- Customer support / IT service desks
- College and university grievance cells
- Housing society maintenance requests
- Government grievance portals
- Telecom and banking complaint centers
- HR / employee support systems

Technically, this project demonstrates REST-style layered thinking (even without a web framework), input validation, role-based access control, and clean separation of concerns — all directly transferable to backend developer, Java developer, and enterprise software roles.

## 4. Java Concepts Used

| Concept | Where it's used |
|---|---|
| Classes & Objects | `User`, `Complaint` model classes |
| Encapsulation | Private fields with getters/setters throughout `model/` |
| Enums | `ComplaintStatus`, `ComplaintPriority`, `ComplaintCategory`, `User.Role` |
| Collections (List) | In-memory caches in `UserService` and `ComplaintService` |
| Constructors | Overloaded constructors in `Complaint` (new vs. rebuilt-from-file) |
| Exception Handling | Custom `ComplaintSystemException`, try/catch across all flows |
| File Handling | `FileManager` reads/writes plain text files under `data/` |
| Date & Time | `LocalDateTime` for created/updated timestamps |
| Input Validation | `AppUtils` (email format, password length, title/description length) |
| Role-Based Access | `User.Role` checked in `Main` to route to user/admin dashboards and to restrict actions like closing a complaint |
| Streams / Lambdas | Filtering and searching complaints in `ComplaintService` |

## 5. Complaint Lifecycle

```
OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED
                                         ↘ (any active stage) → REJECTED
```

- A user submits a complaint → status starts at **OPEN**
- Admin assigns it to themselves → **ASSIGNED**
- Admin can move it to **IN_PROGRESS**
- Admin adds a resolution note → automatically becomes **RESOLVED**
- The original user (and only that user) can then **CLOSE** it
- A complaint can never move backward to OPEN, and a CLOSED complaint is final

See `docs/architecture.md` for the full data flow and class relationship diagram.

## 6. Folder Structure

```
Online-Complaint-Management-System-Java/
│
├── src/                      ← CONSOLE APP source
│   ├── model/        User, Complaint, ComplaintStatus, ComplaintPriority, ComplaintCategory
│   ├── service/       UserService, ComplaintService (business logic)
│   ├── repository/    FileManager (all file I/O)
│   ├── utility/        AppUtils (validation + ID generation)
│   ├── exception/     ComplaintSystemException (custom exception)
│   └── main/           Main (console UI, user & admin dashboards)
├── data/              users.txt, complaints.txt, sequence.txt (generated at runtime)
├── logs/               app.log (generated at runtime)
├── outputs/           sample-console-output.txt (a full simulated run)
├── screenshots/       add your own screenshots here (see docs/screenshots-checklist.md)
├── docs/               architecture, testing, simulation, GitHub strategy, proof plan, interview prep
│
├── backend/                  ← API APP: Spring Boot REST backend
│   ├── pom.xml
│   ├── README.md            (its own run instructions + API reference)
│   └── src/main/java/demo/ComplaintApp.java
├── dashboard/                ← API APP: visual web dashboard (open index.html in a browser)
│   └── index.html
│
├── README.md                 (this file)
└── .gitignore
```

## 7. Features

**Mandatory (all implemented):**
User registration & login, admin login, complaint submission, unique complaint ID generation, category & priority assignment, view complaint details, track status, update status, add resolution note, close complaint, safe exit.

**Recommended (all implemented):**
Full status set (Open/Assigned/In Progress/Resolved/Closed/Rejected), four priority levels, six categories, complaint history via file persistence, search by ID, filter by status/category/priority, file-based persistence, user feedback.

**Not implemented (documented as future work — see §11):**
JDBC/MySQL, GUI (Swing/JavaFX), email notifications, escalation, analytics dashboard, SLA tracking, AI-based categorization.

## 8. How to Run

### A. Command line
```bash
cd Online-Complaint-Management-System-Java
javac -d bin $(find src -name "*.java")
java -cp bin main.Main
```

### B. IntelliJ IDEA
1. Open the project folder as a new project (choose the folder itself, not `src`).
2. Right-click `src` → Mark Directory as → Sources Root.
3. Right-click `Main.java` inside `src/main` → Run 'Main.main()'.

### C. Eclipse
1. File → New → Java Project → uncheck "Use default location" if needed, point to this folder.
2. Right-click project → Build Path → Use src as source folder (it already matches the package layout).
3. Right-click `Main.java` → Run As → Java Application.

## 9. Sample Menu

```
1. Register
2. Login
3. Exit
```

After login as a **user**:
```
---- USER DASHBOARD ----
1. Submit a new complaint
2. View my complaints
3. Track complaint status by ID
4. Give feedback on a resolved complaint
5. Close a resolved complaint
6. Logout
```

After login as **admin** (seeded account: `admin@complaints.local` / `admin123`):
```
---- ADMIN DASHBOARD ----
1. View all complaints
2. Filter complaints (status/category/priority)
3. Assign a complaint to myself
4. Update complaint status
5. Add resolution note (marks Resolved)
6. Search complaint by ID
7. Logout
```

## 10. Sample Output

A full recorded run (register → login → submit → assign → resolve → close → feedback) is saved at
[`outputs/sample-console-output.txt`](outputs/sample-console-output.txt). Full simulation walkthrough with exact inputs is in [`docs/simulation.md`](docs/simulation.md).

## 11. Limitations & Future Improvements

**Current limitations:**
- Data is stored in plain text files, not a real database (no concurrent-write safety)
- Console-only interface, no GUI
- Passwords are stored as plain text (fine for a learning project, not production)
- No email/SMS notifications

**Planned improvements:**
- JDBC + MySQL for real persistence
- Swing or JavaFX admin dashboard
- Email notification on status change
- SLA tracking and auto-escalation for overdue complaints
- Analytics dashboard (complaints by category/priority over time)
- AI-based automatic complaint categorization

## 12. Learning Outcomes

Through this project I practiced:
- Designing a layered architecture (model / service / repository / utility / exception / main)
- Enforcing a real state-machine (complaint lifecycle) in code, not just in prose
- File-based persistence and safe serialization/deserialization
- Writing custom exceptions and validating input consistently
- Structuring a project so it is testable, GitHub-ready, and interview-explainable

## 13. Author

**Gous** — Java project built as part of an IoT/software placement-portfolio program under the mentorship of **Umesh Yadav sir**, through EDC, IIT Delhi in association with the Indian Institute of Placement.

## 14. Related Documentation

- [`docs/architecture.md`](docs/architecture.md) — architecture diagram, class relationships, data flow
- [`docs/simulation.md`](docs/simulation.md) — full virtual walkthrough with exact sample inputs/outputs
- [`docs/testing.md`](docs/testing.md) — manual test cases and expected results
- [`docs/github-strategy.md`](docs/github-strategy.md) — repo name, description, tags, commit strategy
- [`docs/proof-building-plan.md`](docs/proof-building-plan.md) — day-wise GitHub commit plan
- [`docs/screenshots-checklist.md`](docs/screenshots-checklist.md) — exactly what to capture
- [`docs/interview-preparation.md`](docs/interview-preparation.md) — 10 predicted questions with strong answers
