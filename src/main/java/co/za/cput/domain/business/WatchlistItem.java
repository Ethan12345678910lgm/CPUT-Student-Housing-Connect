package co.za.cput.domain.business;

import co.za.cput.domain.users.Student;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class WatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "studentID")
    @JsonIgnoreProperties({"bookings"})
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", referencedColumnName = "accommodationID")
    @JsonIgnoreProperties({"bookings"})
    private Accommodation accommodation;

    private LocalDateTime createdAt;

    protected WatchlistItem() {
    }

    private WatchlistItem(Builder builder) {
        this.id = builder.id;
        this.student = builder.student;
        this.accommodation = builder.accommodation;
        this.createdAt = builder.createdAt;
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public Accommodation getAccommodation() {
        return accommodation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static class Builder {
        private Long id;
        private Student student;
        private Accommodation accommodation;
        private LocalDateTime createdAt;

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder setStudent(Student student) {
            this.student = student;
            return this;
        }

        public Builder setAccommodation(Accommodation accommodation) {
            this.accommodation = accommodation;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder copy(WatchlistItem item) {
            this.id = item.getId();
            this.student = item.getStudent();
            this.accommodation = item.getAccommodation();
            this.createdAt = item.getCreatedAt();
            return this;
        }

        public WatchlistItem build() {
            return new WatchlistItem(this);
        }
    }
}