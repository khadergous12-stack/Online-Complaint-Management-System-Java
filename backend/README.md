# ComplaintDesk — REST API + Visual Dashboard

This is the placement-ready version of the Online Complaint Management System: a Spring Boot REST API backend with a real visual dashboard frontend, instead of a console UI.

It's a second, standalone build alongside the plain-Java console version — use whichever fits the deliverable you need. This one is better for demos, screenshots, and a "product" feel; the console version is better for demonstrating raw core-Java/OOP fundamentals.

---

## What's inside

```
Online-Complaint-Management-System-Java-API/
├── backend/
│   ├── pom.xml
│   └── src/main/java/demo/ComplaintApp.java   ← single-file Spring Boot REST API
└── dashboard/
    └── index.html                              ← single-file visual dashboard (HTML/CSS/JS)
```

The backend is in-memory (no database) — data resets every time you restart it, and it seeds two demo accounts plus two demo tickets automatically so the dashboard looks populated on first load.

## How to run (you'll need Maven + JDK 17+, both available in a normal VS Code setup)

**1. Start the backend** (from the `backend/` folder):
```bash
cd backend
mvn spring-boot:run
```
Wait for `Started ComplaintApp` in the logs — it runs on `http://localhost:8080`.

**2. Open the dashboard**
Just double-click `dashboard/index.html`, or in VS Code right-click it → **Open with Live Server** (if you have that extension). It talks to the API at `http://localhost:8080/api` — CORS is already enabled in the backend for local dev, so opening the file directly works fine too.

**3. Log in**
Demo accounts (seeded automatically):
| Role | Email | Password |
|---|---|---|
| Admin | `admin@example.com` | `admin` |
| Student | `user@example.com` | `user` |

Or register a new student account from the login screen.

## What the dashboard does

**As a student (USER role):**
- Raise a new ticket (title, description, category, priority)
- See "My Tickets" as cards with a live lifecycle tracker (Open → In Progress → Resolved → Closed)
- Open a ticket to read/add comments
- Close your own ticket once it's Resolved

**As an admin (ADMIN role):**
- See all tickets with sidebar counts per status (click to filter)
- Search tickets by keyword
- Assign a ticket to yourself (moves it to In Progress)
- Mark a ticket Resolved, or force-close it
- Comment on any ticket

## A note on verification

I compiled and ran the **console version** end-to-end in a sandboxed environment to confirm it works. For this API version, Maven Central isn't reachable from that same sandbox, so I couldn't do a live `mvn` build here — the backend code is a careful, conservative adaptation of the reference implementation already in your project spec (same endpoints, same validation rules, same lifecycle logic), with only CORS support and an "assign to me" default added on top. Please run `mvn spring-boot:run` on your end as the real verification step, and let me know if anything throws an error — I can fix it immediately from the stack trace.

## API endpoints (for reference / Postman testing)

| Method | Path | Who | Purpose |
|---|---|---|---|
| POST | `/api/users/register` | anyone | create a USER account |
| POST | `/api/auth/login` | anyone | returns a token + role |
| GET | `/api/complaints/mine` | logged-in user | list own tickets |
| POST | `/api/complaints` | logged-in user | create a ticket |
| GET | `/api/complaints/{id}` | logged-in user | ticket detail |
| POST | `/api/complaints/{id}/comments` | logged-in user | add a comment |
| PATCH | `/api/complaints/{id}/assign` | admin | assign (defaults to self) |
| PATCH | `/api/complaints/{id}/status` | admin or owning user | change status |
| GET | `/api/complaints/search` | admin | filter/search all tickets |

All authenticated requests need header `X-Auth: <token>` from the login response.
