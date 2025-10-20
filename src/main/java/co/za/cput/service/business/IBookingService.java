package co.za.cput.service.business;

import co.za.cput.domain.business.Booking;
import co.za.cput.dto.BookingApplicationRequest;
import co.za.cput.dto.BookingApplicationView;
import co.za.cput.dto.BookingStatusUpdateRequest;
import co.za.cput.service.IService;

import java.util.List;

public interface IBookingService extends IService<Booking, Long> {
    List<Booking> getAllBookings();

    BookingApplicationView applyForAccommodation(BookingApplicationRequest request);

    List<BookingApplicationView> findApplicationsForLandlord(Long landlordId);

    List<BookingApplicationView> findApplicationsForStudent(Long studentId);

    BookingApplicationView updateApplicationStatus(Long bookingId, BookingStatusUpdateRequest request);
}