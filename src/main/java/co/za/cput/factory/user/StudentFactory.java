package co.za.cput.factory.user;
//Firstname:        Kwanda
//LastName:         Twalo
//Student Number:   218120192.

import co.za.cput.domain.business.Booking;
import co.za.cput.domain.generic.Contact;
import co.za.cput.domain.users.Student;
import co.za.cput.util.Helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class StudentFactory {
    public static Student createStudent(String studentName,
                                 String studentSurname,
                                 LocalDate studentDateOfBirth,
                                 String gender,
                                 String password,
                                 LocalDateTime registrationDate,
                                 boolean isStudentVerified,
                                 Student.FundingStatus fundingStatus,
                                 Contact contact,
                                 List<Booking> bookings) {

        if (Helper.isNullorEmpty(studentName) ||
                Helper.isNullorEmpty(studentSurname) ||
                !Helper.validateStudentDateOfBirth(studentDateOfBirth) ||
                Helper.isNullorEmpty(gender) ||
                (password != null && !Helper.isValidPassword(password)) ||
                !Helper.isValidTimestamp(registrationDate)) {
            return null;
        }

        Student.Builder builder = new Student.Builder()
                .setStudentName(studentName)
                .setStudentSurname(studentSurname)
                .setDateOfBirth(studentDateOfBirth)
                .setGender(gender)
                .setRegistrationDate(registrationDate)
                .setIsStudentVerified(isStudentVerified)
                .setFundingStatus(fundingStatus)
                .setContact(contact)
                .setBookings(bookings);

        if (!Helper.isNullorEmpty(password)) {
            builder.setPassword(password);
        }

        return builder.build();
    }

    public static Student createStudent(String studentName,
                                        String studentSurname,
                                        LocalDate studentDateOfBirth,
                                        String gender,
                                        LocalDateTime registrationDate,
                                        boolean isStudentVerified,
                                        Student.FundingStatus fundingStatus,
                                        Contact contact,
                                        List<Booking> bookings) {
        return createStudent(studentName, studentSurname, studentDateOfBirth, gender, null, registrationDate,
                isStudentVerified, fundingStatus, contact, bookings);
    }
}
