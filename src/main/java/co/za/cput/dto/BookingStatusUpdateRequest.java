package co.za.cput.dto;

import co.za.cput.domain.business.Booking;

public class BookingStatusUpdateRequest {
    private Booking.BookingStatus status;

    public BookingStatusUpdateRequest() {
    }

    public Booking.BookingStatus getStatus() {
        return status;
    }

    public void setStatus(Booking.BookingStatus status) {
        this.status = status;
    }
}