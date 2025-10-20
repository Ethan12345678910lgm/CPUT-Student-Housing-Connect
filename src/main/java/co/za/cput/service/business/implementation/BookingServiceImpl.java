package co.za.cput.service.business.implementation;

import co.za.cput.domain.business.Accommodation;
import co.za.cput.domain.business.Booking;
import co.za.cput.domain.users.Student;
import co.za.cput.dto.BookingApplicationRequest;
import co.za.cput.dto.BookingApplicationView;
import co.za.cput.dto.BookingStatusUpdateRequest;
import co.za.cput.repository.business.AccommodationRepository;
import co.za.cput.repository.business.BookingRepository;
import co.za.cput.repository.users.StudentRepository;
import co.za.cput.service.business.IBookingService;
import co.za.cput.util.LinkingEntitiesHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements IBookingService {

    private BookingRepository bookingRepository;
    private StudentRepository studentRepository;
    private AccommodationRepository accommodationRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository,
                              StudentRepository studentRepository,
                              AccommodationRepository accommodationRepository) {
        this.bookingRepository = bookingRepository;
        this.studentRepository = studentRepository;
        this.accommodationRepository = accommodationRepository;
    }

    @Override
    public Booking create(Booking booking) {
        // Prepare booking with persisted linked entities (student, accommodation)
        Booking preparedBooking = LinkingEntitiesHelper.prepareBookingWithLinkedEntities(
                booking, studentRepository, accommodationRepository);

        // Link the booking inside the review (if review exists)
        preparedBooking = LinkingEntitiesHelper.setBookingInReview(preparedBooking);

        // Save booking
        return bookingRepository.saveAndFlush(preparedBooking);
    }

    @Override
    public Booking read(Long Id) {
        return bookingRepository.findById(Id).orElse(null);
    }

    @Override
    public Booking update(Booking booking) {
        if (booking.getBookingID() == null || !bookingRepository.existsById(booking.getBookingID())) {
            return null; // or throw exception if preferred
        }

        // Prepare booking with persisted linked entities (student, accommodation)
        Booking preparedBooking = LinkingEntitiesHelper.prepareBookingWithLinkedEntities(
                booking, studentRepository, accommodationRepository);

        // Link the booking inside the review (if review exists)
        preparedBooking = LinkingEntitiesHelper.setBookingInReview(preparedBooking);

        // Save updated booking
        return bookingRepository.saveAndFlush(preparedBooking);
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public void delete(Long Id) {
        bookingRepository.deleteById(Id);
    }

    @Override
    public BookingApplicationView applyForAccommodation(BookingApplicationRequest request) {
        if (request == null || request.getStudentId() == null || request.getAccommodationId() == null) {
            throw new IllegalArgumentException("Student id and accommodation id are required.");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));

        Accommodation accommodation = accommodationRepository.findById(request.getAccommodationId())
                .orElseThrow(() -> new IllegalArgumentException("Accommodation not found."));

        boolean alreadyApplied = bookingRepository.findByStudent_StudentIDOrderByCreatedAtDesc(student.getStudentID())
                .stream()
                .anyMatch(existing -> existing.getAccommodation() != null
                        && existing.getAccommodation().getAccommodationID() != null
                        && existing.getAccommodation().getAccommodationID().equals(accommodation.getAccommodationID())
                        && (existing.getBookingStatus() == Booking.BookingStatus.IN_PROGRESS
                        || existing.getBookingStatus() == Booking.BookingStatus.CONFIRMED));

        if (alreadyApplied) {
            throw new IllegalArgumentException("You already have an active application for this listing.");
        }

        LocalDate requestDate = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        Booking newBooking = new Booking.Builder()
                .setStudent(student)
                .setAccommodation(accommodation)
                .setRequestDate(requestDate)
                .setCheckInDate(request.getPreferredCheckInDate())
                .setCheckOutDate(request.getPreferredCheckOutDate())
                .setTotalAmount(accommodation.getRent())
                .setCreatedAt(now)
                .setUpdatedAt(now)
                .setPaymentStatus(Booking.PaymentStatus.PENDING)
                .setBookingStatus(Booking.BookingStatus.IN_PROGRESS)
                .build();

        Booking saved = bookingRepository.saveAndFlush(newBooking);
        return toView(saved);
    }

    @Override
    public List<BookingApplicationView> findApplicationsForLandlord(Long landlordId) {
        if (landlordId == null) {
            throw new IllegalArgumentException("Landlord id is required.");
        }

        return bookingRepository.findByAccommodation_Landlord_LandlordIDOrderByCreatedAtDesc(landlordId)
                .stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingApplicationView> findApplicationsForStudent(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student id is required.");
        }

        return bookingRepository.findByStudent_StudentIDOrderByCreatedAtDesc(studentId)
                .stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Override
    public BookingApplicationView updateApplicationStatus(Long bookingId, BookingStatusUpdateRequest request) {
        if (bookingId == null || request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("A valid status update request is required.");
        }

        Booking existing = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        Booking updated = new Booking.Builder()
                .copy(existing)
                .setBookingStatus(request.getStatus())
                .setUpdatedAt(LocalDateTime.now())
                .build();

        Booking saved = bookingRepository.saveAndFlush(updated);
        return toView(saved);
    }

    private BookingApplicationView toView(Booking booking) {
        if (booking == null) {
            return null;
        }

        Student student = booking.getStudent();
        Accommodation accommodation = booking.getAccommodation();
        Long landlordId = accommodation != null && accommodation.getLandlord() != null
                ? accommodation.getLandlord().getLandlordID()
                : null;
        String landlordEmail = accommodation != null && accommodation.getLandlord() != null
                && accommodation.getLandlord().getContact() != null
                ? accommodation.getLandlord().getContact().getEmail()
                : null;

        String addressLine = null;
        String suburb = null;
        if (accommodation != null && accommodation.getAddress() != null) {
            addressLine = String.format("%s %s",
                    valueOrBlank(accommodation.getAddress().getStreetNumber()),
                    valueOrBlank(accommodation.getAddress().getStreetName())).trim();
            suburb = accommodation.getAddress().getSuburb();
        }

        return new BookingApplicationView(
                booking.getBookingID(),
                student != null ? student.getStudentID() : null,
                student != null ? student.getStudentName() : null,
                student != null ? student.getStudentSurname() : null,
                student != null && student.getContact() != null ? student.getContact().getEmail() : null,
                accommodation != null ? accommodation.getAccommodationID() : null,
                addressLine != null && !addressLine.isBlank() ? addressLine : suburb,
                suburb,
                accommodation != null ? accommodation.getRent() : null,
                booking.getRequestDate(),
                booking.getBookingStatus(),
                booking.getPaymentStatus(),
                landlordId,
                landlordEmail
        );
    }

    private String valueOrBlank(String value) {
        return value != null ? value : "";
    }
}

