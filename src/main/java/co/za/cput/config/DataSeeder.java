package co.za.cput.config;

import co.za.cput.domain.business.Accommodation;
import co.za.cput.domain.business.Booking;
import co.za.cput.domain.business.Verification;
import co.za.cput.domain.generic.Address;
import co.za.cput.domain.generic.Contact;
import co.za.cput.domain.users.Administrator;
import co.za.cput.domain.users.Landlord;
import co.za.cput.domain.users.Student;
import co.za.cput.repository.business.AccommodationRepository;
import co.za.cput.repository.business.BookingRepository;
import co.za.cput.repository.business.VerificationRepository;
import co.za.cput.repository.users.AdministratorRepository;
import co.za.cput.repository.users.LandLordRepository;
import co.za.cput.repository.users.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final AdministratorRepository administratorRepository;
    private final LandLordRepository landLordRepository;
    private final StudentRepository studentRepository;
    private final AccommodationRepository accommodationRepository;
    private final VerificationRepository verificationRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AdministratorRepository administratorRepository,
                      LandLordRepository landLordRepository,
                      StudentRepository studentRepository,
                      AccommodationRepository accommodationRepository,
                      VerificationRepository verificationRepository,
                      BookingRepository bookingRepository,
                      PasswordEncoder passwordEncoder) {
        this.administratorRepository = administratorRepository;
        this.landLordRepository = landLordRepository;
        this.studentRepository = studentRepository;
        this.accommodationRepository = accommodationRepository;
        this.verificationRepository = verificationRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensureSuperAdministrator();

        boolean hasSeedData = landLordRepository.count() > 0
                || studentRepository.count() > 0
                || accommodationRepository.count() > 0
                || verificationRepository.count() > 0
                || bookingRepository.count() > 0;

        if (hasSeedData) {
            return;
        }

        if (administratorRepository.count() == 1) {
            Administrator operationsAdmin = createAdministrator();
            administratorRepository.save(operationsAdmin);
        }

        Administrator verifyingAdministrator = administratorRepository.findAll().stream()
                .filter(admin -> !admin.isSuperAdmin())
                .findFirst()
                .orElseGet(() -> administratorRepository.findFirstBySuperAdminTrue().orElse(null));

        Landlord verifiedLandlord = landLordRepository.save(createLandlord(
                "Sipho",
                "Mbeki",
                true,
                "sipho.mbeki@rentconnect.co.za",
                "0712345678",
                "0823456789",
                "Landlord1234"
        ));

        Landlord pendingLandlord = landLordRepository.save(createLandlord(
                "Nomsa",
                "Zulu",
                false,
                "nomsa.zulu@rentconnect.co.za",
                "0739876543",
                "0621234567",
                "Landlord1235"
        ));

        Accommodation belharLoft = accommodationRepository.save(createAccommodation(
                verifiedLandlord,
                "18",
                "Protea Road",
                "Belhar",
                "Cape Town",
                7493,
                3500.0,
                true,
                true,
                true,
                1.2,
                Accommodation.RoomType.SINGLE,
                Accommodation.BathroomType.PRIVATE,
                Accommodation.AccommodationStatus.AVAILABLE
        ));

        Accommodation observatoryNest = accommodationRepository.save(createAccommodation(
                pendingLandlord,
                "45B",
                "Boundary Road",
                "Observatory",
                "Cape Town",
                7925,
                2800.0,
                true,
                false,
                false,
                2.5,
                Accommodation.RoomType.DOUBLE,
                Accommodation.BathroomType.SHARED,
                Accommodation.AccommodationStatus.AVAILABLE
        ));

        landLordRepository.save(new Landlord.Builder()
                .copy(verifiedLandlord)
                .setAccommodationList(List.of(belharLoft))
                .build());

        landLordRepository.save(new Landlord.Builder()
                .copy(pendingLandlord)
                .setAccommodationList(List.of(observatoryNest))
                .build());

        Student student = studentRepository.save(createStudent());

        Booking confirmedBooking = bookingRepository.save(new Booking.Builder()
                .setRequestDate(LocalDate.now().minusDays(10))
                .setCheckInDate(LocalDate.now().minusDays(2))
                .setCheckOutDate(LocalDate.now().plusMonths(6))
                .setTotalAmount(21000.00)
                .setUpdatedAt(LocalDateTime.now().minusDays(1))
                .setCreatedAt(LocalDateTime.now().minusDays(10))
                .setPaymentStatus(Booking.PaymentStatus.PAID)
                .setBookingStatus(Booking.BookingStatus.CONFIRMED)
                .setStudent(student)
                .setAccommodation(belharLoft)
                .build());

        studentRepository.save(new Student.Builder()
                .copy(student)
                .setBookings(List.of(confirmedBooking))
                .build());

        verificationRepository.save(new Verification.Builder()
                .setCreateAt(LocalDateTime.now().minusDays(5))
                .setUpdateAt(LocalDateTime.now().minusDays(2))
                .setVerificationDate(LocalDate.now().minusDays(2))
                .setVerificationStatus(Verification.VerificationStatus.APPROVED)
                .setNotes("Utility bills and ownership confirmed.")
                .setAdministrator(verifyingAdministrator)
                .setAccommodation(belharLoft)
                .build());

        verificationRepository.save(new Verification.Builder()
                .setCreateAt(LocalDateTime.now().minusDays(3))
                .setUpdateAt(LocalDateTime.now().minusDays(1))
                .setVerificationStatus(Verification.VerificationStatus.PENDING)
                .setNotes("Awaiting safety compliance certificates.")
                .setAdministrator(verifyingAdministrator)
                .setAccommodation(observatoryNest)
                .build());
    }

    private Administrator ensureSuperAdministrator() {
        String superAdminEmail = "superadmin@gmail.com";
        return administratorRepository.findFirstByContact_EmailIgnoreCase(superAdminEmail)
                .filter(Administrator::isSuperAdmin)
                .orElseGet(() -> administratorRepository.saveAndFlush(createSuperAdministrator(superAdminEmail)));
    }

    private Administrator createSuperAdministrator(String email) {
        Contact adminContact = new Contact.Builder()
                .setEmail(email)
                .setPhoneNumber("0601234567")
                .setAlternatePhoneNumber("0789876543")
                .setIsEmailVerified(true)
                .setIsPhoneVerified(true)
                .setPreferredContactMethod(Contact.PreferredContactMethod.EMAIL)
                .build();

        return new Administrator.Builder()
                .setAdminName("Super")
                .setAdminSurname("Administrator")
                .setAdminPassword(passwordEncoder.encode("SuperAdmin*"))
                .setAdminRoleStatus(Administrator.AdminRoleStatus.ACTIVE)
                .setSuperAdmin(true)
                .setContact(adminContact)
                .build();
    }

    private Administrator createAdministrator() {
        Contact adminContact = new Contact.Builder()
                .setEmail("admin@cput-housing.co.za")
                .setPhoneNumber("0601234567")
                .setAlternatePhoneNumber("0789876543")
                .setIsEmailVerified(true)
                .setIsPhoneVerified(true)
                .setPreferredContactMethod(Contact.PreferredContactMethod.EMAIL)
                .build();

        return new Administrator.Builder()
                .setAdminName("Agnes")
                .setAdminSurname("Mokoena")
                .setAdminPassword(passwordEncoder.encode("Admin1234"))
                .setAdminRoleStatus(Administrator.AdminRoleStatus.ACTIVE)
                .setSuperAdmin(false)
                .setContact(adminContact)
                .build();
    }

    private Landlord createLandlord(String firstName,
                                    String lastName,
                                    boolean verified,
                                    String email,
                                    String phoneNumber,
                                    String altPhone,
                                    String password) {
        Contact contact = new Contact.Builder()
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAlternatePhoneNumber(altPhone)
                .setIsEmailVerified(true)
                .setIsPhoneVerified(false)
                .setPreferredContactMethod(Contact.PreferredContactMethod.EMAIL)
                .build();

        return new Landlord.Builder()
                .setLandlordFirstName(firstName)
                .setLandlordLastName(lastName)
                .setVerified(verified)
                .setDateRegistered(LocalDate.now().minusMonths(3))
                .setPassword(passwordEncoder.encode(password))
                .setContact(contact)
                .build();
    }

    private Accommodation createAccommodation(Landlord landlord,
                                              String streetNumber,
                                              String streetName,
                                              String suburb,
                                              String city,
                                              int postalCode,
                                              double rent,
                                              boolean wifi,
                                              boolean furnished,
                                              boolean utilitiesIncluded,
                                              double distance,
                                              Accommodation.RoomType roomType,
                                              Accommodation.BathroomType bathroomType,
                                              Accommodation.AccommodationStatus status) {
        Address address = new Address.Builder()
                .setStreetNumber(streetNumber)
                .setStreetName(streetName)
                .setSuburb(suburb)
                .setCity(city)
                .setPostalCode(postalCode)
                .build();

        return new Accommodation.Builder()
                .setRent(rent)
                .setWifiAvailable(wifi)
                .setFurnished(furnished)
                .setUtilitiesIncluded(utilitiesIncluded)
                .setDistanceFromCampus(distance)
                .setRoomType(roomType)
                .setBathroomType(bathroomType)
                .setAccommodationStatus(status)
                .setAddress(address)
                .setLandlord(landlord)
                .build();
    }

    private Student createStudent() {
        Contact contact = new Contact.Builder()
                .setEmail("student@cput.ac.za")
                .setPhoneNumber("0612345678")
                .setAlternatePhoneNumber("0812345678")
                .setIsEmailVerified(true)
                .setIsPhoneVerified(true)
                .setPreferredContactMethod(Contact.PreferredContactMethod.EMAIL)
                .build();

        return new Student.Builder()
                .setStudentName("Thando")
                .setStudentSurname("Jacobs")
                .setDateOfBirth(LocalDate.now().minusYears(20))
                .setGender("Female")
                .setPassword(passwordEncoder.encode("Student1234"))
                .setRegistrationDate(LocalDateTime.now().minusMonths(2))
                .setIsStudentVerified(true)
                .setFundingStatus(Student.FundingStatus.FUNDED)
                .setContact(contact)
                .build();
    }
}