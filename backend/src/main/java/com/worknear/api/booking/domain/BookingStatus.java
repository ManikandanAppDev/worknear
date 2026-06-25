package com.worknear.api.booking.domain;

import java.util.Set;

/**
 * Booking lifecycle. Allowed transitions are enforced in the service layer.
 */
public enum BookingStatus {
    PENDING,        // created, awaiting pro acceptance
    CONFIRMED,      // pro accepted
    ON_THE_WAY,     // pro travelling
    ARRIVED,        // pro reached the customer location
    IN_PROGRESS,    // work started
    COMPLETED_PENDING_OTP, // pro marked work complete; waiting for customer OTP
    COMPLETED,      // OTP verified and payment released
    CANCELLED,      // cancelled by customer/admin
    REJECTED,       // declined by pro
    NO_SHOW;        // professional never arrived within the booked day; locked amount auto-released to customer

    private static final java.util.Map<BookingStatus, Set<BookingStatus>> TRANSITIONS = java.util.Map.ofEntries(
            java.util.Map.entry(PENDING, Set.of(CONFIRMED, REJECTED, CANCELLED, NO_SHOW)),
            java.util.Map.entry(CONFIRMED, Set.of(ON_THE_WAY, CANCELLED, NO_SHOW)),
            java.util.Map.entry(ON_THE_WAY, Set.of(ARRIVED, CANCELLED, NO_SHOW)),
            java.util.Map.entry(ARRIVED, Set.of(IN_PROGRESS, CANCELLED)),
            java.util.Map.entry(IN_PROGRESS, Set.of(COMPLETED_PENDING_OTP)),
            java.util.Map.entry(COMPLETED_PENDING_OTP, Set.of(COMPLETED)),
            java.util.Map.entry(COMPLETED, Set.of()),
            java.util.Map.entry(CANCELLED, Set.of()),
            java.util.Map.entry(REJECTED, Set.of()),
            java.util.Map.entry(NO_SHOW, Set.of())
    );

    public boolean canTransitionTo(BookingStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED || this == REJECTED || this == NO_SHOW;
    }
}
