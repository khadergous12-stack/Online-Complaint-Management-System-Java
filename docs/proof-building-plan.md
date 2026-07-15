# Day-wise GitHub Proof-Building Plan

| Day | Focus | Files to commit | Commit message | Proof to capture |
|---|---|---|---|---|
| 1 | Project setup and class design | `.gitignore`, `README.md` (draft), `src/model/*.java` | `chore: initialize project structure and .gitignore` + `feat: add model classes (User, Complaint, enums)` | Screenshot of folder structure in IDE |
| 2 | User registration and login | `src/service/UserService.java`, `src/exception/ComplaintSystemException.java`, `src/utility/AppUtils.java` | `feat: add UserService with registration and login` | Console screenshot: successful registration + login output |
| 3 | Complaint submission and ID generation | `src/service/ComplaintService.java` (submit portion) | `feat: add complaint submission with ID generation` | Console screenshot: complaint submitted, ID shown (e.g. `CMP-1001`) |
| 4 | Admin complaint handling | `src/main/Main.java` (admin dashboard) | `feat: add admin dashboard (view, assign, filter, search)` | Console screenshot: admin dashboard menu and complaint list |
| 5 | Status, priority, and resolution workflow | `ComplaintService.java` (assign/status/resolve methods) | `feat: add complaint lifecycle transitions and resolution` | Console screenshot: status changing OPEN → ASSIGNED → RESOLVED |
| 6 | File handling and complaint history | `src/repository/FileManager.java` | `feat: add FileManager for persistence and audit logging` | Screenshot of `data/complaints.txt` and `logs/app.log` contents |
| 7 | Testing and bug fixes | Any fixes found during testing | `test: run full simulation and fix validation edge cases` | `outputs/sample-console-output.txt` + `docs/testing.md` |
| 8 | README and GitHub documentation | `README.md` (final), `docs/*.md` | `docs: add README, architecture, GitHub strategy, interview prep` | Screenshot of the rendered README on GitHub |

## Tips
- Commit after each working feature, not all at once — this shows real incremental progress.
- Push after every 1–2 days rather than only at the end, so the commit timestamps look organic.
- Pin the repository on your GitHub profile once the README is polished.
- Link the repo in your LinkedIn post about this project (mentioning mentorship from Umesh Yadav sir, consistent with your other project posts).
