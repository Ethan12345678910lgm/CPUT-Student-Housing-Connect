package co.za.cput.repository.business;

import co.za.cput.domain.business.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<WatchlistItem, Long> {
    List<WatchlistItem> findByStudent_StudentIDOrderByCreatedAtDesc(Long studentId);

    Optional<WatchlistItem> findFirstByStudent_StudentIDAndAccommodation_AccommodationID(Long studentId, Long accommodationId);

    void deleteByStudent_StudentIDAndAccommodation_AccommodationID(Long studentId, Long accommodationId);
}