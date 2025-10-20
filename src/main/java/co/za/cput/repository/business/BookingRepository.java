package co.za.cput.repository.business;

import co.za.cput.domain.business.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    long countByBookingStatus(Booking.BookingStatus status);

    long countByAccommodation_AccommodationID(Long accommodationId);

    long countByAccommodation_AccommodationIDAndBookingStatus(Long accommodationId, Booking.BookingStatus status);

    List<Booking> findByAccommodation_Landlord_LandlordIDOrderByCreatedAtDesc(Long landlordId);

    List<Booking> findByStudent_StudentIDOrderByCreatedAtDesc(Long studentId);
}