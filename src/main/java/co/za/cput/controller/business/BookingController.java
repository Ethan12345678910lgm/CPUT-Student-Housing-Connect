package co.za.cput.controller.business;

import co.za.cput.domain.business.Booking;
import co.za.cput.dto.BookingApplicationRequest;
import co.za.cput.dto.BookingApplicationView;
import co.za.cput.dto.BookingStatusUpdateRequest;
import co.za.cput.service.business.implementation.BookingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/bookings", "/HouseConnect/Booking"})
public class BookingController {

    private final BookingServiceImpl bookingService;

    @Autowired
    public BookingController(BookingServiceImpl bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/create")
    public ResponseEntity<Booking> create(@RequestBody Booking booking) {
        Booking createdBooking = bookingService.create(booking);
        return ResponseEntity.ok(createdBooking);
    }

    @PostMapping("/apply")
    public ResponseEntity<?> apply(@RequestBody BookingApplicationRequest request) {
        try {
            BookingApplicationView application = bookingService.applyForAccommodation(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(application);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }
    }

    @GetMapping("/read/{bookingID}")
    public ResponseEntity<Booking> read(@PathVariable Long bookingID) {
        Booking booking = bookingService.read(bookingID);
        if (booking == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/update")
    public ResponseEntity<Booking> update(@RequestBody Booking booking) {
        if (booking.getBookingID() == null) {
            return ResponseEntity.badRequest().build();
        }
        Booking updatedBooking = bookingService.update(booking);
        if (updatedBooking == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedBooking);
    }

    @GetMapping("/getAllBookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        if (bookings == null || bookings.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/landlord/{landlordId}")
    public ResponseEntity<List<BookingApplicationView>> listForLandlord(@PathVariable Long landlordId) {
        List<BookingApplicationView> applications = bookingService.findApplicationsForLandlord(landlordId);
        if (applications.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<BookingApplicationView>> listForStudent(@PathVariable Long studentId) {
        List<BookingApplicationView> applications = bookingService.findApplicationsForStudent(studentId);
        if (applications.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(applications);
    }

    @PatchMapping("/applications/{bookingId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long bookingId,
                                          @RequestBody BookingStatusUpdateRequest request) {
        try {
            BookingApplicationView updated = bookingService.updateApplicationStatus(bookingId, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException exception) {
            HttpStatus status = "Booking not found.".equals(exception.getMessage())
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(exception.getMessage());
        }
    }

    @DeleteMapping("/delete/{bookingID}")
    public void delete(@PathVariable Long bookingID) {
        bookingService.delete(bookingID);
    }
}
