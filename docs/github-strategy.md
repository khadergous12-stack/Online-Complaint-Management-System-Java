# GitHub Strategy

## Repository Name
`Online-Complaint-Management-System-Java`

## Repository Description
"A console-based Complaint Management System in Java demonstrating OOP, file handling, role-based access, and a full complaint lifecycle (Open → Assigned → In Progress → Resolved → Closed). Built as a placement-ready portfolio project."

## Suggested GitHub Topics/Tags
`java` `oop` `console-application` `complaint-management` `file-handling` `crud` `java-project` `student-project` `portfolio-project` `enums` `exception-handling`

## Folder Organization on GitHub
Keep the structure exactly as built:
```
src/ (model, service, repository, utility, exception, main)
data/  (only .gitkeep committed — real data files are gitignored)
logs/  (only .gitkeep committed)
outputs/  (sample-console-output.txt committed as proof)
screenshots/  (your captured screenshots)
docs/  (all documentation)
README.md
.gitignore
```

## What NOT to Upload
- Real user passwords or personal data — the shipped `data/` folder only contains `.gitkeep`; real `users.txt`/`complaints.txt` are gitignored and generated at runtime with the seeded demo admin account only
- IDE-specific files (`.idea/`, `.iml`, `.classpath`, `.project`) — already covered by `.gitignore`
- Compiled `.class` files / `bin/` folder — already covered by `.gitignore`

## Commit Strategy (meaningful, incremental commits)

| Order | Commit message |
|---|---|
| 1 | `chore: initialize project structure and .gitignore` |
| 2 | `feat: add model classes (User, Complaint, enums)` |
| 3 | `feat: add custom exception and utility validation helpers` |
| 4 | `feat: add FileManager for file-based persistence` |
| 5 | `feat: add UserService with registration and login` |
| 6 | `feat: add ComplaintService with full lifecycle logic` |
| 7 | `feat: add Main console UI with user and admin dashboards` |
| 8 | `test: run and verify full simulation, add sample output` |
| 9 | `docs: add architecture, testing, and simulation docs` |
| 10 | `docs: add README with setup, features, and learning outcomes` |
| 11 | `docs: add GitHub strategy, proof plan, and interview prep` |

See `docs/proof-building-plan.md` for the day-wise breakdown with what to commit and screenshot each day.
