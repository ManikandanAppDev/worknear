package com.worknear.api.admin.dto;

import java.math.BigDecimal;

public record DashboardResponse(
        BigDecimal revenueThisMonth,
        BigDecimal grossBookingValueThisMonth,
        long totalBookings,
        long bookingsThisWeek,
        long activeProfessionals,
        long pendingVerifications,
        long openDisputes,
        long pendingPayouts
) {}
