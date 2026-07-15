package model;

/**
 * Represents the life cycle stage of a complaint.
 *
 * Lifecycle:
 * OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED -> CLOSED
 *                                 -> REJECTED (any stage, admin decision)
 */
public enum ComplaintStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    REJECTED
}
