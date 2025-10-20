package co.za.cput.service.business.implementation;

import co.za.cput.domain.business.Booking;
import co.za.cput.domain.business.Review;
import co.za.cput.repository.business.BookingRepository;
import co.za.cput.repository.business.ReviewRepository;
import co.za.cput.service.business.IReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl implements IReviewService {

    private ReviewRepository reviewRepository;

    //added because it will be used to link the booking to the review that will be made later by the student.
    private BookingRepository bookingRepository;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Review create(Review review) {
        return reviewRepository.saveAndFlush(review);
    }

    @Override
    public Review read(Long ID) {
        return reviewRepository.findById(ID).orElse(null);
    }

    @Override
    public Review update(Review review) {
        return reviewRepository.saveAndFlush(review);
    }

    @Override
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    //Needs testing.
    //This method is used to share an experience about the accommodation.
    @Override
    public Review addReviewToBooking(Long bookingID, Review review) {
        Booking booking = bookingRepository.findById(bookingID)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Link the booking to the review
        int rating = review.getRating();
        if (rating < 1) {
            rating = 1;
        } else if (rating > 5) {
            rating = 5;
        }

        Review reviewToSave = new Review.Builder()
                .copy(review)
                .setRating(rating)
                .setReviewDate(review.getReviewDate() != null ? review.getReviewDate() : java.time.LocalDate.now())
                .setBooking(booking)
                .build();

        return reviewRepository.saveAndFlush(reviewToSave);
    }

    @Override
    public void delete(Long ID) {
        reviewRepository.deleteById(ID);
    }
}
