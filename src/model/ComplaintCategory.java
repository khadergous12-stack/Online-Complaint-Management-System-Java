package model;

/**
 * Represents the category a complaint belongs to.
 * Used to route complaints to the right admin/department and to
 * enable filtering and reporting.
 */
public enum ComplaintCategory {
    TECHNICAL,
    BILLING,
    SERVICE,
    PRODUCT,
    INFRASTRUCTURE,
    OTHER
}
