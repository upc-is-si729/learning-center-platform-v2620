package com.acme.center.platform.learning.domain.model.valueobjects;

/**
 * Enumeration representing the enrollment status.
 *
 * <p>
 * This enumeration is used to track the lifecycle of an enrollment.
 * </p>
 */
public enum EnrollmentStatus {
    /**
     * The enrollment has been requested but not yet confirmed.
     */
    REQUESTED,
    /**
     * The enrollment has been confirmed and the student can proceed with the course.
     */
    CONFIRMED,
    /**
     * The enrollment request has been rejected.
     */
    REJECTED,
    /**
     * The enrollment has been cancelled by the student or the system.
     */
    CANCELED
}
