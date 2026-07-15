# Architecture

## 1. Layered Architecture

```
┌──────────────────────────────────────────────────────┐
│                       main/                            │
│   Main.java — console UI, routes to User/Admin menus   │
└───────────────────────┬──────────────────────────────┘
                         │ calls
┌───────────────────────▼──────────────────────────────┐
│                     service/                            │
│   UserService.java        ComplaintService.java         │
│   - register/login         - submit/assign/update       │
│   - lookup by email/id     - resolve/close/feedback      │
│                             - filter/search               │
└───────────────────────┬──────────────────────────────┘
                         │ uses
┌───────────────────────▼──────────────────────────────┐
│                   repository/                            │
│   FileManager.java                                       │
│   - load/save users.txt, complaints.txt                 │
│   - sequence.txt (complaint ID counter)                 │
│   - app.log (audit log)                                 │
└───────────────────────┬──────────────────────────────┘
                         │ persists
┌───────────────────────▼──────────────────────────────┐
│                       data/                              │
│         users.txt   complaints.txt   sequence.txt        │
└──────────────────────────────────────────────────────┘

        model/                       utility/          exception/
  User, Complaint,             AppUtils.java       ComplaintSystemException.java
  ComplaintStatus,             (validation, ID     (single custom exception used
  ComplaintPriority,           generation)          across all layers)
  ComplaintCategory
  (used by every layer above)
```

## 2. Module Responsibilities

**User Module** (via `Main` + `UserService`)
- register, login
- submit complaint, view own complaints
- track status by ID
- give feedback on a resolved complaint
- close own complaint (only when Resolved)

**Admin Module** (via `Main` + `ComplaintService`)
- view all complaints
- filter by status / category / priority
- assign complaint to self
- update status (with lifecycle validation)
- add resolution note (auto-moves to Resolved)

**Data Layer** (`repository.FileManager`)
- users.txt — one line per user, pipe-delimited
- complaints.txt — one line per complaint, rewritten in full on every update
- sequence.txt — running counter for complaint ID generation
- logs/app.log — timestamped audit trail of every state-changing action

## 3. Class Relationships

```
Main ── uses ──> UserService ── uses ──> FileManager
Main ── uses ──> ComplaintService ── uses ──> FileManager
UserService ── produces/consumes ──> User
ComplaintService ── produces/consumes ──> Complaint
Complaint ── has-a ──> ComplaintStatus, ComplaintPriority, ComplaintCategory
User ── has-a ──> User.Role (nested enum: USER, ADMIN)
All service/repository methods ── throw ──> ComplaintSystemException
```

## 4. Complaint Lifecycle (State Machine)

```
        submit()
           │
           ▼
        OPEN ───────assign()────────▶ ASSIGNED
                                          │
                                   updateStatus()
                                          ▼
                                   IN_PROGRESS
                                          │
                                addResolution()
                                          ▼
                                     RESOLVED
                                          │
                                  closeComplaint()
                                    (owner only)
                                          ▼
                                       CLOSED  (terminal)

  updateStatus() to REJECTED is allowed from any non-terminal state.
  Rule enforced in ComplaintService.validateTransition():
    - cannot re-open a CLOSED complaint
    - cannot move backward to OPEN
    - CLOSED requires the complaint to currently be RESOLVED
```

## 5. Data Flow — Submitting and Resolving a Complaint

```
User (console input)
   │
   ▼
Main.submitComplaintFlow()
   │
   ▼
ComplaintService.submitComplaint()
   │  - validates title/description (AppUtils)
   │  - generates ID (AppUtils.generateComplaintId)
   │  - creates Complaint object, status = OPEN
   ▼
FileManager.saveAllComplaints() + saveSequence()
   │
   ▼
complaints.txt updated, sequence.txt updated, app.log entry written
   │
   ▼
Complaint ID returned to console and shown to user

... later ...

Admin (console input)
   │
   ▼
Main.resolveFlow()
   │
   ▼
ComplaintService.addResolution()
   │  - sets resolutionNote, status = RESOLVED
   ▼
FileManager.saveAllComplaints()
   │
   ▼
User tracks status by ID → sees RESOLVED → closes it themselves
```
