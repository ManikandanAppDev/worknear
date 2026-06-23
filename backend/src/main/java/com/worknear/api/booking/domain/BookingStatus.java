package com.worknear.api.booking.domain;

import java.util.Set;

/**
 * Booking lifecycle. Allowed transitions are enforced in the service layer.
 */
public enum BookingStatus {
    PENDING,        // created, awaiting pro acceptance
    CONFIRMED,      // pro accepted
    ON_THE_WAY,     // pro travelling
    IN_PROGRESS,    // work started
    COMPLETED,      // work finished
    CANCELLED,      // cancelled by customer/admin
    REJECTED;       // declined by pro

    private static final java.util.Map<BookingStatus, Set<BookingStatus>> TRANSITIONS = java.util.Map.of(
            PENDING, Set.of(CONFIRMED, REJECTED, CANCELLED),
            CONFIRMED, Set.of(ON_THE_WAY, CANCELLED),
            ON_THE_WAY, Set.of(IN_PROGRESS, CANCELLED),
            IN_PROGRESS, Set.of(COMPLETED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of(),
            REJECTED, Set.of()
    );

    public boolean canTransitionTo(BookingStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED || this == REJECTED;
    }
}
