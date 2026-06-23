package com.worknear.api.dispute;

import com.worknear.api.booking.BookingRepository;
import com.worknear.api.booking.domain.Booking;
import com.worknear.api.common.exception.ForbiddenException;
import com.worknear.api.common.exception.NotFoundException;
import com.worknear.api.dispute.domain.Dispute;
import com.worknear.api.dispute.dto.CreateDisputeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public Dispute raise(UUID userId, CreateDisputeRequest req) {
        Booking booking = bookingRepository.findById(req.bookingId())
                .orElseThrow(() -> NotFoundException.of("Booking", req.bookingId()));
        boolean participant = booking.getCustomerId().equals(userId) || userId.equals(booking.getProfessionalId());
        if (!participant) {
            throw new ForbiddenException("You can only raise a dispute on your own booking");
        }
        Dispute dispute = new Dispute();
        dispute.setBookingId(req.bookingId());
        dispute.setRaisedBy(userId);
        dispute.setReason(req.reason());
        dispute.setDescription(req.description());
        return disputeRepository.save(dispute);
    }
}
