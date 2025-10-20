package co.za.cput.service.business.implementation;

import co.za.cput.domain.business.Accommodation;
import co.za.cput.domain.business.WatchlistItem;
import co.za.cput.domain.users.Student;
import co.za.cput.repository.business.AccommodationRepository;
import co.za.cput.repository.business.WatchlistRepository;
import co.za.cput.repository.users.StudentRepository;
import co.za.cput.service.business.IWatchlistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class WatchlistServiceImpl implements IWatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final StudentRepository studentRepository;
    private final AccommodationRepository accommodationRepository;

    public WatchlistServiceImpl(WatchlistRepository watchlistRepository,
                                StudentRepository studentRepository,
                                AccommodationRepository accommodationRepository) {
        this.watchlistRepository = watchlistRepository;
        this.studentRepository = studentRepository;
        this.accommodationRepository = accommodationRepository;
    }

    @Override
    public WatchlistItem addToWatchlist(Long studentId, Long accommodationId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("Accommodation not found."));

        return watchlistRepository.findFirstByStudent_StudentIDAndAccommodation_AccommodationID(studentId, accommodationId)
                .orElseGet(() -> watchlistRepository.saveAndFlush(new WatchlistItem.Builder()
                        .setStudent(student)
                        .setAccommodation(accommodation)
                        .setCreatedAt(LocalDateTime.now())
                        .build()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchlistItem> findForStudent(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student id is required.");
        }
        return watchlistRepository.findByStudent_StudentIDOrderByCreatedAtDesc(studentId);
    }

    @Override
    public void removeFromWatchlist(Long studentId, Long accommodationId) {
        if (studentId == null || accommodationId == null) {
            throw new IllegalArgumentException("Student id and accommodation id are required.");
        }
        watchlistRepository.deleteByStudent_StudentIDAndAccommodation_AccommodationID(studentId, accommodationId);
    }
}