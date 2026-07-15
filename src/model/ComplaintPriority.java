package model;

/**
 * Represents how urgently a complaint needs attention.
 * Higher priority complaints should typically be resolved faster.
 */
public enum ComplaintPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
