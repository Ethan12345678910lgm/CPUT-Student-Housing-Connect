package co.za.cput.factory.business;

import co.za.cput.domain.business.Accommodation;
import co.za.cput.domain.business.Booking;
import co.za.cput.domain.business.Review;
import co.za.cput.domain.users.Student;
import co.za.cput.util.Helper;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingFactory {
    public static Booking createBooking(LocalDate requestDate,
                                        LocalDate checkInDate,
                                        LocalDate checkOutDate,
                                        double totalAmount,
                                        LocalDateTime createdAt,
                                        LocalDateTime updatedAt,
                                        Booking.PaymentStatus paymentStatus,
                                        Booking.BookingStatus bookingStatus,
                                        Student student,
                                        Accommodation accommodation,
                                        Review review) {

        if (!Helper.isValidDate(requestDate) ||
                checkInDate == null ||
                !Helper.isValidCheckOutDate(checkOutDate) ||
                !Helper.isValidBookingDates(checkInDate, checkOutDate) ||
                !Helper.isValid_DecimalNumber(totalAmount) ||
                !Helper.isValidTimestamp(createdAt) ||
                !Helper.isValidTimestamp(updatedAt)
                ) {
            return null;
        }

        if (checkInDate.isBefore(requestDate)) {
            return null;
        }

        return new Booking.Builder()
                .setRequestDate(requestDate)
                .setCheckInDate(checkInDate)
                .setCheckOutDate(checkOutDate)
                .setTotalAmount(totalAmount)
                .setCreatedAt(createdAt)
                .setUpdatedAt(updatedAt)
                .setPaymentStatus(paymentStatus)
                .setBookingStatus(bookingStatus)
                .setStudent(student)
                .setAccommodation(accommodation)
                .setReview(review)
                .build();
    }
}
