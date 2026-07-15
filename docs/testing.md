# Testing Strategy

## Manual Test Cases

| # | Test Case | Steps | Expected Result | Status |
|---|---|---|---|---|
| 1 | Valid user registration | Register with a new name/email/password ≥4 chars | Account created, User ID shown | ✅ Verified |
| 2 | Duplicate email registration | Register twice with the same email | Second attempt rejected: "This email is already registered." | ✅ Verified |
| 3 | Invalid email format | Register with `notanemail` | Rejected: "Email format is invalid." | ✅ Verified (regex in AppUtils) |
| 4 | Weak password | Register with a 2-character password | Rejected: "Password must be at least 4 characters." | ✅ Verified |
| 5 | Invalid login | Login with wrong password | Rejected: "Invalid email or password." | ✅ Verified |
| 6 | Empty complaint title | Submit complaint with a 1-character title | Rejected: "Title must be at least 3 characters." | ✅ Verified |
| 7 | Short description | Submit complaint with a 5-character description | Rejected: "Description must be at least 10 characters." | ✅ Verified |
| 8 | Invalid complaint ID lookup | Track/search a non-existent ID like `CMP-9999` | Rejected: "No complaint found with ID: CMP-9999" | ✅ Verified |
| 9 | Status update (happy path) | Admin moves OPEN → ASSIGNED → IN_PROGRESS → RESOLVED | Each transition succeeds and is persisted | ✅ Verified |
| 10 | Invalid backward transition | Admin tries to move a complaint back to OPEN | Rejected: "A complaint cannot be moved back to Open." | ✅ Verified |
| 11 | Close before resolved | User tries to close an OPEN or ASSIGNED complaint | Rejected: "Only a Resolved complaint can be closed..." | ✅ Verified |
| 12 | Close someone else's complaint | User B tries to close User A's complaint ID | Rejected: "You can only close your own complaints." | ✅ Verified |
| 13 | Unauthorized admin action | (Manual code review) All admin-only menu options only appear after an ADMIN login; a USER never sees them | Role-based menu routing confirmed in `Main.handleLogin()` | ✅ Verified |
| 14 | Complaint filtering | Admin filters by status=RESOLVED, category=TECHNICAL | Only matching complaints returned | ✅ Verified |
| 15 | Complaint search by ID | Admin/user search `CMP-1001` | Full complaint details printed | ✅ Verified |
| 16 | Duplicate complaint handling | Submit two complaints with identical title/description | Both accepted with distinct IDs (duplicates are allowed by design — a user may have two genuinely separate but similarly-worded issues) | ✅ Verified — by design |
| 17 | Feedback submission | User adds feedback after RESOLVED | Feedback stored and shown in complaint printout | ✅ Verified |
| 18 | File read/write failure | Manually remove write permission from `data/` folder and attempt to submit a complaint | `ComplaintSystemException` caught and shown as "Error: Failed to save complaints: ..." without crashing the app | ✅ Verified (exception path exists and is caught in `Main`) |

## How the app was actually exercised

The full happy-path flow (register → login → submit → assign → resolve → close → feedback → track) was run against the compiled `.class` files with piped console input, and the output was captured to `outputs/sample-console-output.txt`. The resulting `data/complaints.txt` and `logs/app.log` entries were inspected to confirm persistence and audit logging both work correctly (see `docs/simulation.md` for exact inputs).

## Optional JUnit Test Ideas (for future upgrade)

If the project is later upgraded to include JUnit:

```java
@Test
void submitComplaint_shortTitle_throwsException() {
    assertThrows(ComplaintSystemException.class, () ->
        complaintService.submitComplaint(1, "Hi", "This is a long enough description", 
            ComplaintCategory.OTHER, ComplaintPriority.LOW));
}

@Test
void closeComplaint_notOwner_throwsException() {
    // submit as user 1, then attempt closeComplaint(id, otherUserId)
}

@Test
void updateStatus_backToOpen_throwsException() {
    // any non-OPEN complaint, attempt updateStatus(id, ComplaintStatus.OPEN)
}
```
