# Interview Preparation

## 1. Explain your project.
I built a console-based Online Complaint Management System in core Java. Users can register, log in, submit complaints with a category and priority, and track their status. Admins can view all complaints, assign them, move them through a defined lifecycle (Open → Assigned → In Progress → Resolved → Closed), add resolution notes, and the original user can then close the complaint and leave feedback. I used a layered architecture — model, service, repository, utility, and exception packages — with file-based persistence so the whole system runs without an external database.

## 2. What problem does this project solve?
It replaces unorganized, manual complaint tracking (paper logs, scattered emails) with a structured system where every complaint has a unique ID, a defined status, and a full audit trail from submission to resolution.

## 3. Which Java concepts did you use, and why?
I used encapsulation (private fields with getters/setters) to protect object state, enums for fixed sets of values like status/priority/category (safer than raw strings), a custom checked exception for consistent error handling, file I/O for persistence, `LocalDateTime` for timestamps, and streams/lambdas for filtering complaint lists. Each concept was chosen because it maps to a real constraint — e.g., enums stop invalid status values from ever entering the system.

## 4. Walk me through your class structure.
`model` holds the data classes (`User`, `Complaint`) and enums. `service` holds business logic (`UserService` for auth, `ComplaintService` for the complaint lifecycle). `repository` (`FileManager`) is the only class that touches the file system, so persistence is isolated from business logic. `utility` (`AppUtils`) holds stateless helpers like validation and ID generation. `main` (`Main`) is purely the console UI — it never talks to files directly, only to the service layer.

## 5. How did you generate complaint IDs?
Each new complaint increments a persisted sequence counter (`sequence.txt`) and formats it as `CMP-1001`, `CMP-1002`, etc. Persisting the counter (not just using `list.size()+1`) means IDs stay unique and sequential even after the app is restarted or complaints are deleted.

## 6. How does the complaint status workflow work, and how did you enforce valid transitions?
A complaint starts at OPEN. Admin actions move it to ASSIGNED, IN_PROGRESS, and (via adding a resolution note) RESOLVED. Only the complaint's original owner can close a RESOLVED complaint, moving it to CLOSED. I enforce this with a `validateTransition()` method in `ComplaintService` that throws a `ComplaintSystemException` for illegal moves — like trying to go back to OPEN, or closing something that isn't RESOLVED yet.

## 7. How did you implement role-based access?
Every `User` has a `Role` enum (`USER` or `ADMIN`). After login, `Main` checks the role and routes to either the user dashboard or the admin dashboard — a regular user never even sees the admin menu options. Ownership checks are enforced separately in the service layer (e.g. `closeComplaint()` verifies the requesting user actually owns that complaint), so access control isn't just a UI-level illusion.

## 8. How did you store complaint data, and what would you improve?
I used plain pipe-delimited text files (`users.txt`, `complaints.txt`) managed entirely through `FileManager`, so no other class touches the file system directly. The main limitation is no concurrent-write safety and no relational querying. The natural next step is swapping `FileManager` for a JDBC/MySQL-backed repository — because the service layer only depends on the repository's method signatures, that swap wouldn't require changing any business logic.

## 9. What challenges did you face, and how did you solve them?
Two things stood out: (1) keeping complaint IDs unique and stable across restarts, which I solved by persisting the sequence counter to its own file rather than deriving it from the in-memory list size; and (2) making sure invalid status transitions (like re-opening a closed complaint) were actually blocked in code, not just described in documentation — I centralized that logic in one `validateTransition()` method so every status-changing method goes through the same rules.

## 10. How would you scale or extend this project?
Add JDBC/MySQL for real persistence and concurrent access, add a Swing/JavaFX or web front end, add email notifications on status changes, add SLA tracking with auto-escalation for overdue high-priority complaints, and add basic analytics (complaints by category/priority over time). Because the service layer is already decoupled from both the UI (`Main`) and the storage (`FileManager`), each of these can be added incrementally without rewriting the core logic.
