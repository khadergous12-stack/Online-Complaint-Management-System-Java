# Virtual Simulation Walkthrough

This is the exact end-to-end run used to validate the project (also saved as raw console output in `outputs/sample-console-output.txt`).

## Scenario
A student ("Gous Master") registers, submits a technical complaint about hostel wifi, the admin assigns and resolves it, and the student closes it and leaves feedback.

## Step-by-step inputs and expected outputs

| # | Actor | Menu path | Sample input | Expected output |
|---|---|---|---|---|
| 1 | User | Main menu → 1 (Register) | Name: `Gous Master`, Email: `gous@example.com`, Password: `pass123` | `Registration successful. Your User ID is 1002. Please login.` |
| 2 | User | Main menu → 2 (Login) | `gous@example.com` / `pass123` | `Login successful. Welcome, Gous Master (USER)` → routes to User Dashboard |
| 3 | User | Dashboard → 1 (Submit complaint) | Title: `Wifi not working in hostel block A`, Description: `The wifi router in hostel block A has been down since morning affecting 40 students`, Category: `1` (TECHNICAL), Priority: `1` (LOW) | `Complaint submitted successfully! Your Complaint ID is: CMP-1001` |
| 4 | User | Dashboard → 6 (Logout) | — | `Logged out.` |
| 5 | Admin | Main menu → 2 (Login) | `admin@complaints.local` / `admin123` (seeded account) | `Login successful. Welcome, Admin (ADMIN)` → routes to Admin Dashboard |
| 6 | Admin | Dashboard → 3 (Assign to myself) | Complaint ID: `CMP-1001` | `Complaint assigned to Admin.` (status → ASSIGNED) |
| 7 | Admin | Dashboard → 5 (Add resolution) | Complaint ID: `CMP-1001`, Note: `Router was rebooted and firmware updated, issue fixed` | `Complaint marked as Resolved.` (status → RESOLVED) |
| 8 | Admin | Dashboard → 7 (Logout) | — | `Logged out.` |
| 9 | User | Main menu → 2 (Login) | `gous@example.com` / `pass123` | User Dashboard |
| 10 | User | Dashboard → 5 (Close complaint) | Complaint ID: `CMP-1001` | `Complaint closed successfully.` (status → CLOSED) |
| 11 | User | Dashboard → 4 (Give feedback) | Complaint ID: `CMP-1001`, Feedback: `Issue was resolved quickly, thank you` | `Feedback recorded. Thank you!` |
| 12 | User | Dashboard → 3 (Track by ID) | `CMP-1001` | Full complaint printout with all fields, status CLOSED |

## Final complaint record (as persisted in `data/complaints.txt`)

```
CMP-1001|1002|Wifi not working in hostel block A|The wifi router in hostel block A has been down since morning affecting 40 students|TECHNICAL|LOW|CLOSED|2026-07-14 18:11:24|2026-07-14 18:11:24|Admin|Router was rebooted and firmware updated, issue fixed|Issue was resolved quickly, thank you
```

## Corresponding audit log (`logs/app.log`)

```
USER_CREATED: admin@complaints.local
USER_CREATED: gous@example.com
LOGIN: gous@example.com (USER)
COMPLAINT_CREATED: CMP-1001 by user 1002
LOGIN: admin@complaints.local (ADMIN)
COMPLAINT_ASSIGNED: CMP-1001 -> Admin
COMPLAINT_RESOLVED: CMP-1001
LOGIN: gous@example.com (USER)
COMPLAINT_CLOSED: CMP-1001
FEEDBACK_ADDED: CMP-1001
```

## What to capture as screenshots/proof

- Terminal showing the registration + login prompts and success messages
- Terminal showing the generated Complaint ID after submission
- Terminal showing the admin dashboard with the complaint listed as OPEN, then ASSIGNED
- Terminal showing the resolution note being added and status becoming RESOLVED
- Terminal showing the user closing the complaint and leaving feedback
- Contents of `data/complaints.txt` after the run (`cat data/complaints.txt`)
- Contents of `logs/app.log` after the run (`cat logs/app.log`)

See `docs/screenshots-checklist.md` for the full list.
