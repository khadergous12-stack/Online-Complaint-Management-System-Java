package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single complaint raised by a user and tracked
 * through its life cycle by the admin.
 */
public class Complaint {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String complaintId;
    private int userId;
    private String title;
    private String description;
    private ComplaintCategory category;
    private ComplaintPriority priority;
    private ComplaintStatus status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String assignedTo;      // admin name/id handling the complaint, may be empty
    private String resolutionNote;  // filled by admin when resolving
    private String feedback;        // filled by user after resolution

    public Complaint(String complaintId, int userId, String title, String description,
                      ComplaintCategory category, ComplaintPriority priority) {
        this.complaintId = complaintId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = ComplaintStatus.OPEN;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = this.createdDate;
        this.assignedTo = "";
        this.resolutionNote = "";
        this.feedback = "";
    }

    // Full constructor used when rebuilding from file
    public Complaint(String complaintId, int userId, String title, String description,
                      ComplaintCategory category, ComplaintPriority priority, ComplaintStatus status,
                      LocalDateTime createdDate, LocalDateTime updatedDate,
                      String assignedTo, String resolutionNote, String feedback) {
        this.complaintId = complaintId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = status;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.assignedTo = assignedTo;
        this.resolutionNote = resolutionNote;
        this.feedback = feedback;
    }

    public String getComplaintId() { return complaintId; }
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; touch(); }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; touch(); }
    public ComplaintCategory getCategory() { return category; }
    public void setCategory(ComplaintCategory category) { this.category = category; touch(); }
    public ComplaintPriority getPriority() { return priority; }
    public void setPriority(ComplaintPriority priority) { this.priority = priority; touch(); }
    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; touch(); }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; touch(); }
    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; touch(); }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; touch(); }

    private void touch() {
        this.updatedDate = LocalDateTime.now();
    }

    /**
     * Serializes this complaint into a single pipe-delimited line.
     * Format:
     * complaintId|userId|title|description|category|priority|status|createdDate|updatedDate|assignedTo|resolutionNote|feedback
     *
     * Newlines inside title/description/notes are replaced with a
     * placeholder token so a complaint always stays on one line.
     */
    public String toFileString() {
        return String.join("|",
                complaintId,
                String.valueOf(userId),
                escape(title),
                escape(description),
                category.name(),
                priority.name(),
                status.name(),
                createdDate.format(FMT),
                updatedDate.format(FMT),
                escape(assignedTo),
                escape(resolutionNote),
                escape(feedback)
        );
    }

    public static Complaint fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        return new Complaint(
                p[0],
                Integer.parseInt(p[1]),
                unescape(p[2]),
                unescape(p[3]),
                ComplaintCategory.valueOf(p[4]),
                ComplaintPriority.valueOf(p[5]),
                ComplaintStatus.valueOf(p[6]),
                LocalDateTime.parse(p[7], FMT),
                LocalDateTime.parse(p[8], FMT),
                unescape(p[9]),
                unescape(p[10]),
                unescape(p[11])
        );
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("|", "&#124;").replace("\n", " ").replace("\r", " ");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("&#124;", "|");
    }

    @Override
    public String toString() {
        return "\n--------------------------------------------------\n" +
                "Complaint ID   : " + complaintId + "\n" +
                "Title          : " + title + "\n" +
                "Description    : " + description + "\n" +
                "Category       : " + category + "\n" +
                "Priority       : " + priority + "\n" +
                "Status         : " + status + "\n" +
                "Created On     : " + createdDate.format(FMT) + "\n" +
                "Last Updated   : " + updatedDate.format(FMT) + "\n" +
                "Assigned To    : " + (assignedTo.isEmpty() ? "Not yet assigned" : assignedTo) + "\n" +
                "Resolution     : " + (resolutionNote.isEmpty() ? "-" : resolutionNote) + "\n" +
                "Feedback       : " + (feedback.isEmpty() ? "-" : feedback) +
                "\n--------------------------------------------------";
    }
}
