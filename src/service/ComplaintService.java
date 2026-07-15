package service;

import exception.ComplaintSystemException;
import model.*;
import repository.FileManager;
import utility.AppUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Core business logic for the complaint life cycle:
 * submit -> assign -> update status -> resolve -> feedback -> close.
 */
public class ComplaintService {

    private final FileManager fileManager;
    private final List<Complaint> complaints;
    private int sequence;

    public ComplaintService(FileManager fileManager) throws ComplaintSystemException {
        this.fileManager = fileManager;
        this.complaints = new ArrayList<>(fileManager.loadComplaints());
        this.sequence = fileManager.loadSequence();
    }

    public Complaint submitComplaint(int userId, String title, String description,
                                      ComplaintCategory category, ComplaintPriority priority)
            throws ComplaintSystemException {
        if (!AppUtils.isValidTitle(title)) {
            throw new ComplaintSystemException("Title must be at least 3 characters.");
        }
        if (!AppUtils.isValidDescription(description)) {
            throw new ComplaintSystemException("Description must be at least 10 characters.");
        }
        sequence++;
        String id = AppUtils.generateComplaintId(sequence);
        Complaint complaint = new Complaint(id, userId, title.trim(), description.trim(), category, priority);
        complaints.add(complaint);
        persist();
        fileManager.log("COMPLAINT_CREATED: " + id + " by user " + userId);
        return complaint;
    }

    public List<Complaint> getComplaintsForUser(int userId) {
        return complaints.stream()
                .filter(c -> c.getUserId() == userId)
                .collect(Collectors.toList());
    }

    public List<Complaint> getAllComplaints() {
        return new ArrayList<>(complaints);
    }

    public Complaint findById(String complaintId) throws ComplaintSystemException {
        return complaints.stream()
                .filter(c -> c.getComplaintId().equalsIgnoreCase(complaintId))
                .findFirst()
                .orElseThrow(() -> new ComplaintSystemException("No complaint found with ID: " + complaintId));
    }

    public Complaint assignComplaint(String complaintId, String adminName) throws ComplaintSystemException {
        Complaint c = findById(complaintId);
        if (c.getStatus() == ComplaintStatus.CLOSED || c.getStatus() == ComplaintStatus.REJECTED) {
            throw new ComplaintSystemException("Cannot assign a complaint that is already " + c.getStatus());
        }
        c.setAssignedTo(adminName);
        c.setStatus(ComplaintStatus.ASSIGNED);
        persist();
        fileManager.log("COMPLAINT_ASSIGNED: " + complaintId + " -> " + adminName);
        return c;
    }

    public Complaint updateStatus(String complaintId, ComplaintStatus newStatus) throws ComplaintSystemException {
        Complaint c = findById(complaintId);
        validateTransition(c.getStatus(), newStatus);
        c.setStatus(newStatus);
        persist();
        fileManager.log("STATUS_UPDATED: " + complaintId + " -> " + newStatus);
        return c;
    }

    public Complaint addResolution(String complaintId, String note) throws ComplaintSystemException {
        if (!AppUtils.isNotBlank(note)) {
            throw new ComplaintSystemException("Resolution note cannot be empty.");
        }
        Complaint c = findById(complaintId);
        c.setResolutionNote(note.trim());
        c.setStatus(ComplaintStatus.RESOLVED);
        persist();
        fileManager.log("COMPLAINT_RESOLVED: " + complaintId);
        return c;
    }

    public Complaint closeComplaint(String complaintId, int requestingUserId) throws ComplaintSystemException {
        Complaint c = findById(complaintId);
        if (c.getUserId() != requestingUserId) {
            throw new ComplaintSystemException("You can only close your own complaints.");
        }
        if (c.getStatus() != ComplaintStatus.RESOLVED) {
            throw new ComplaintSystemException("Only a Resolved complaint can be closed. Current status: " + c.getStatus());
        }
        c.setStatus(ComplaintStatus.CLOSED);
        persist();
        fileManager.log("COMPLAINT_CLOSED: " + complaintId);
        return c;
    }

    public Complaint addFeedback(String complaintId, int requestingUserId, String feedback) throws ComplaintSystemException {
        Complaint c = findById(complaintId);
        if (c.getUserId() != requestingUserId) {
            throw new ComplaintSystemException("You can only give feedback on your own complaints.");
        }
        if (!AppUtils.isNotBlank(feedback)) {
            throw new ComplaintSystemException("Feedback cannot be empty.");
        }
        c.setFeedback(feedback.trim());
        persist();
        fileManager.log("FEEDBACK_ADDED: " + complaintId);
        return c;
    }

    public List<Complaint> filter(ComplaintStatus status, ComplaintCategory category, ComplaintPriority priority) {
        return complaints.stream()
                .filter(c -> status == null || c.getStatus() == status)
                .filter(c -> category == null || c.getCategory() == category)
                .filter(c -> priority == null || c.getPriority() == priority)
                .collect(Collectors.toList());
    }

    private void validateTransition(ComplaintStatus from, ComplaintStatus to) throws ComplaintSystemException {
        if (from == ComplaintStatus.CLOSED) {
            throw new ComplaintSystemException("Complaint is already Closed and cannot be changed.");
        }
        if (to == ComplaintStatus.OPEN) {
            throw new ComplaintSystemException("A complaint cannot be moved back to Open.");
        }
        if (to == ComplaintStatus.CLOSED && from != ComplaintStatus.RESOLVED) {
            throw new ComplaintSystemException("A complaint must be Resolved before it can be Closed.");
        }
    }

    private void persist() throws ComplaintSystemException {
        fileManager.saveAllComplaints(complaints);
        fileManager.saveSequence(sequence);
    }
}
