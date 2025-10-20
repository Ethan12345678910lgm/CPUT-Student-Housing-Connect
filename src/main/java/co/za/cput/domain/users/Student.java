package co.za.cput.domain.users;
//Firstname:        Kwanda
//LastName:         Twalo
//Student Number:   218120192.

import co.za.cput.domain.business.Booking;
import co.za.cput.domain.generic.Contact;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Student {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long studentID;
    private String studentName;
    private String studentSurname;
    private LocalDate dateOfBirth;
    private String gender;
    @JsonIgnore
    private String password;
    private LocalDateTime registrationDate;
    private boolean isStudentVerified;

    @Enumerated(EnumType.STRING)//this tells JPA that you must store the values as VARCHAR.
    private FundingStatus fundingStatus;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contact_id", referencedColumnName = "contactID")
    private Contact contact;

    //Bidirectional relationship between student and bookings.
    //example A Student makes bookings, and A booking belongs to a student.
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("student-booking")
    private List<Booking> bookings = new ArrayList<Booking>();

    public enum FundingStatus {
        FUNDED,
        SELF_FUNDED,
        NOT_FUNDED
    }

    protected Student() {
    }

    private Student(Builder builder) {
        this.studentID = builder.studentID;
        this.studentName = builder.studentName;
        this.studentSurname = builder.studentSurname;
        this.dateOfBirth = builder.dateOfBirth;
        this.gender = builder.gender;
        this.password = builder.password;
        this.registrationDate = builder.registrationDate;
        this.isStudentVerified = builder.isStudentVerified;
        this.fundingStatus = builder.fundingStatus;
        this.contact = builder.contact;
        this.bookings = builder.bookings != null ? builder.bookings : new ArrayList<>();
    }

    public Long getStudentID() {
        return studentID;
    }

    public void setStudentID(Long studentID) {
        this.studentID = studentID;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentSurname() {
        return studentSurname;
    }

    public void setStudentSurname(String studentSurname) {
        this.studentSurname = studentSurname;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean getIsStudentVerified() {
        return isStudentVerified;
    }

    public boolean isStudentVerified() {
        return isStudentVerified;
    }

    public void setStudentVerified(boolean studentVerified) {
        isStudentVerified = studentVerified;
    }

    public FundingStatus getFundingStatus() {
        return fundingStatus;
    }

    public void setFundingStatus(FundingStatus fundingStatus) {
        this.fundingStatus = fundingStatus;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings != null ? bookings : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentID='" + getStudentID() + '\n' +
                ", studentName='" + getStudentName() + '\n' +
                ", studentSurname='" + getStudentSurname() + '\n' +
                ", dateOfBirth=" + getDateOfBirth() + '\n' +
                ", gender='" + getGender() + '\'' +
                //", password='" + getPassword() + '\n' +
                ", registrationDate=" + getRegistrationDate() + '\n' +
                ", isStudentVerified=" + getIsStudentVerified() + '\n' +
                ", fundingStatus=" + getFundingStatus() + '\n' +
                ", Contacts=" + getContact() + '\n' +
                //", Bookings=" + getBookings() + '\n' +
                '}';
    }

    public static class Builder {
        private Long studentID;
        private String studentName;
        private String studentSurname;
        private LocalDate dateOfBirth;
        private String gender;
        private String password;
        private LocalDateTime registrationDate;
        private boolean isStudentVerified;
        private FundingStatus fundingStatus;
        private Contact contact;
        private List<Booking> bookings;

        public Builder setStudentID(Long studentID) {
            this.studentID = studentID;
            return this;
        }

        public Builder setStudentName(String studentName) {
            this.studentName = studentName;
            return this;
        }

        public Builder setStudentSurname(String studentSurname) {
            this.studentSurname = studentSurname;
            return this;
        }

        public Builder setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder setGender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setRegistrationDate(LocalDateTime registrationDate) {
            this.registrationDate = registrationDate;
            return this;
        }

        public Builder setIsStudentVerified(boolean isStudentVerified) {
            this.isStudentVerified = isStudentVerified;
            return this;
        }

        public Builder setFundingStatus(FundingStatus fundingStatus) {
            this.fundingStatus = fundingStatus;
            return this;
        }

        public Builder setContact(Contact contact) {
            this.contact = contact;
            return this;
        }

        public Builder setBookings(List<Booking> bookings) {
            this.bookings = bookings;
            return this;
        }

        public Builder copy(Student student) {
            this.studentID = student.getStudentID();
            this.studentName = student.getStudentName();
            this.studentSurname = student.getStudentSurname();
            this.dateOfBirth = student.getDateOfBirth();
            this.gender = student.getGender();
            this.password = student.getPassword();
            this.registrationDate = student.getRegistrationDate();
            this.isStudentVerified = student.getIsStudentVerified();
            this.fundingStatus = student.getFundingStatus();
            this.contact = student.getContact();
            this.bookings = student.getBookings();
            return this;
        }

        public Student build() {
            return new Student(this);
        }
    }
}

//Object for testing in POSTMAN:
/*{
        "studentName": "Kwanda",
        "studentSurname": "Twalo",
        "dateOfBirth": "2000-05-15",
        "gender": "Male",
        "password": "securePassword123",
        "registrationDate": "2025-08-04T12:00:00",
        "isStudentVerified": true,
        "fundingStatus": "FUNDED",
        "contact": {
        "email": "kwanda.twalo@example.com",
        "phoneNumber": "0723456789",
        "alternatePhoneNumber": "0734567890",
        "isEmailVerified": true,
        "isPhoneVerified": true,
        "preferredContactMethod": "EMAIL"
        },
        "bookings": [
        {
        "requestDate": "2025-08-01",
        "checkInDate": "2025-09-01",
        "checkOutDate": "2026-06-30",
        "totalAmount": 25000.00,
        "updatedAt": "2025-08-03T10:30:00",
        "createdAt": "2025-08-01T08:00:00",
        "paymentStatus": "PAID",
        "bookingStatus": "CONFIRMED",
        "accommodation": {
        "rent": 2500.00,
        "wifiAvailable": true,
        "furnished": true,
        "distanceFromCampus": 1.2,
        "utilitiesIncluded": true,
        "roomType": "SINGLE",
        "bathroomType": "PRIVATE",
        "accommodationStatus": "AVAILABLE",
        "address": {
        "streetNumber": "12A",
        "streetName": "Main Street",
        "suburb": "Rondebosch",
        "city": "Cape Town",
        "postalCode": 7700
        },
        "landlord": {
        "landlordFirstName": "Sinhle",
        "landlordLastName": "Mthethwa",
        "isVerified": true,
        "dateRegistered": "2024-11-01",
        "password": "landlordPass2024",
        "contact": {
        "email": "sinhle.m@example.com",
        "phoneNumber": "0712345678",
        "alternatePhoneNumber": "0798765432",
        "isEmailVerified": true,
        "isPhoneVerified": false,
        "preferredContactMethod": "PHONE"
        }
        }
        }
        ]
        }*/
