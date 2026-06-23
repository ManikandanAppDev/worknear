package com.worknear.api.user.domain;

/**
 * Top-level account role. A single phone number maps to one account; the
 * Professional app and Customer app are role-split entry points into the same
 * backend. ADMIN is provisioned out-of-band (seed / internal tooling).
 */
public enum Role {
    CUSTOMER,
    PROFESSIONAL,
    ADMIN
}
