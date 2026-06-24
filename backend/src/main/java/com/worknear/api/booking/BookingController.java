package com.worknear.api.booking;

import com.worknear.api.booking.domain.BookingPhotoType;
import com.worknear.api.booking.dto.BookingNoteRequest;
import com.worknear.api.booking.dto.BookingResponse;
import com.worknear.api.booking.dto.CancelBookingRequest;
import com.worknear.api.booking.dto.CancelPreviewResponse;
import com.worknear.api.booking.dto.CreateBookingRequest;
import com.worknear.api.booking.dto.RescheduleBookingRequest;
import com.worknear.api.booking.dto.UpdateStatusRequest;
import com.worknear.api.user.domain.Role;
import com.worknear.api.common.web.ApiResponse;
import com.worknear.api.common.web.PageResponse;
import com.worknear.api.security.CurrentUser;
import com.worknear.api.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "Bookings")
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Create a booking (customer)")
    @PostMapping
    public ApiResponse<BookingResponse> create(@CurrentUser UserPrincipal user,
                                               @Valid @RequestBody CreateBookingRequest request) {
        return ApiResponse.ok(bookingService.create(user.id(), request));
    }

    @Operation(summary = "Get a booking by id")
    @GetMapping("/{bookingId}")
    public ApiResponse<BookingResponse> get(@CurrentUser UserPrincipal user, @PathVariable UUID bookingId) {
        return ApiResponse.ok(bookingService.get(user.id(), bookingId));
    }

    @Operation(summary = "List my bookings as a customer (tab: UPCOMING|COMPLETED|CANCELLED)")
    @GetMapping("/customer")
    public ApiResponse<PageResponse<BookingResponse>> customerBookings(@CurrentUser UserPrincipal user,
                                                                       @RequestParam(required = false) String tab,
                                                                       Pageable pageable) {
        return ApiResponse.ok(bookingService.listForCustomer(user.id(), tab, pageable));
    }

    @Operation(summary = "List my jobs as a professional (tab: REQUESTS|ACTIVE|COMPLETED)")
    @GetMapping("/professional")
    public ApiResponse<PageResponse<BookingResponse>> professionalBookings(@CurrentUser UserPrincipal user,
                                                                           @RequestParam(required = false) String tab,
                                                                           Pageable pageable) {
        return ApiResponse.ok(bookingService.listForProfessional(user.id(), tab, pageable));
    }

    @Operation(summary = "Accept a job (professional)")
    @PostMapping("/{bookingId}/accept")
    public ApiResponse<BookingResponse> accept(@CurrentUser UserPrincipal user, @PathVariable UUID bookingId) {
        return ApiResponse.ok(bookingService.accept(user.id(), bookingId));
    }

    @Operation(summary = "Reject a job (professional)")
    @PostMapping("/{bookingId}/reject")
    public ApiResponse<BookingResponse> reject(@CurrentUser UserPrincipal user, @PathVariable UUID bookingId,
                                              @RequestBody(required = false) BookingNoteRequest request) {
        String note = request == null ? null : request.note();
        return ApiResponse.ok(bookingService.reject(user.id(), bookingId, note));
    }

    @Operation(summary = "Update job status (professional): ON_THE_WAY, IN_PROGRESS, COMPLETED")
    @PostMapping("/{bookingId}/status")
    public ApiResponse<BookingResponse> updateStatus(@CurrentUser UserPrincipal user, @PathVariable UUID bookingId,
                                                     @Valid @RequestBody UpdateStatusRequest request) {
        return ApiResponse.ok(bookingService.updateStatus(user.id(), bookingId, request.status(), request.note()));
    }

    @Operation(summary = "Preview cancellation fee and eligibility (customer)")
    @GetMapping("/{bookingId}/cancel-preview")
    public ApiResponse<CancelPreviewResponse> cancelPreview(@CurrentUser UserPrincipal user,
                                                              @PathVariable UUID bookingId) {
        return ApiResponse.ok(bookingService.cancelPreview(user.id(), bookingId));
    }

    @Operation(summary = "Cancel a booking (customer/professional/admin)")
    @PostMapping("/{bookingId}/cancel")
    public ApiResponse<BookingResponse> cancel(@CurrentUser UserPrincipal user, @PathVariable UUID bookingId,
                                               @Valid @RequestBody(required = false) CancelBookingRequest request) {
        if (user.role() == Role.CUSTOMER) {
            if (request == null || request.reasonCode() == null) {
                throw new com.worknear.api.common.exception.BadRequestException("Cancellation reason is required");
            }
            return ApiResponse.ok(bookingService.cancel(user.id(), bookingId, request));
        }
        String note = request != null ? request.comment() : null;
        return ApiResponse.ok(bookingService.cancelByProfessional(user.id(), bookingId, note));
    }

    @Operation(summary = "Reschedule a booking to a new date/time (customer/admin)")
    @PostMapping("/{bookingId}/reschedule")
    public ApiResponse<BookingResponse> reschedule(@CurrentUser UserPrincipal user, @PathVariable UUID bookingId,
                                                   @Valid @RequestBody RescheduleBookingRequest request) {
        return ApiResponse.ok(bookingService.reschedule(user.id(), bookingId, request));
    }

    @Operation(summary = "Upload a booking photo (type: PROBLEM|WORK)")
    @PostMapping(value = "/{bookingId}/photos", consumes = "multipart/form-data")
    public ApiResponse<BookingResponse> uploadPhoto(@CurrentUser UserPrincipal user, @PathVariable UUID bookingId,
                                                    @RequestParam BookingPhotoType type,
                                                    @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(bookingService.uploadPhoto(user.id(), bookingId, type, file));
    }
}
