package co.za.cput.dto;

import java.time.LocalDate;

public class BookingApplicationRequest {
    private Long studentId;
    private Long accommodationId;
    private LocalDate preferredCheckInDate;
    private LocalDate preferredCheckOutDate;

    public BookingApplicationRequest() {
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getAccommodationId() {
        return accommodationId;
    }

    public void setAccommodationId(Long accommodationId) {
        this.accommodationId = accommodationId;
    }

    public LocalDate getPreferredCheckInDate() {
        return preferredCheckInDate;
    }

    public void setPreferredCheckInDate(LocalDate preferredCheckInDate) {
        this.preferredCheckInDate = preferredCheckInDate;
    }

    public LocalDate getPreferredCheckOutDate() {
        return preferredCheckOutDate;
    }

    public void setPreferredCheckOutDate(LocalDate preferredCheckOutDate) {
        this.preferredCheckOutDate = preferredCheckOutDate;
    }
}