package co.za.cput.dto;

import co.za.cput.domain.business.Booking;

import java.time.LocalDate;

public record BookingApplicationView(
        Long bookingId,
        Long studentId,
        String studentFirstName,
        String studentLastName,
        String studentEmail,
        Long accommodationId,
        String accommodationAddress,
        String accommodationSuburb,
        Double monthlyRent,
        LocalDate requestDate,
        Booking.BookingStatus bookingStatus,
        Booking.PaymentStatus paymentStatus,
        Long landlordId,
        String landlordEmail
) {
}