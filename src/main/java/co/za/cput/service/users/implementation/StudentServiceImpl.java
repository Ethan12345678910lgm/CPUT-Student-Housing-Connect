package co.za.cput.service.users.implementation;

import co.za.cput.domain.generic.Contact;
import co.za.cput.domain.users.Student;
import co.za.cput.repository.users.StudentRepository;
import co.za.cput.service.users.IStudentService;
import co.za.cput.util.Helper;
import co.za.cput.util.LinkingEntitiesHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.List;

@Service
public class StudentServiceImpl implements IStudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentServiceImpl(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Student create(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student details are required.");
        }

        Student securedStudent = secureStudent(student);
        if (securedStudent == null) {
            throw new IllegalArgumentException("Student details are required.");
        }

        Contact contact = securedStudent.getContact();
        if (contact != null && !Helper.isNullorEmpty(contact.getEmail())) {
            if (studentRepository.existsByContact_EmailIgnoreCase(contact.getEmail())) {
                throw new IllegalArgumentException("A student with this email already exists.");
            }
        }
        // Step 1: Save the student without bookings to generate an ID
        Student studentWithoutBookings = new Student.Builder()
                .copy(securedStudent)
                .setBookings(null)
                .build();

        Student savedStudent = studentRepository.saveAndFlush(studentWithoutBookings); // Now has ID

        // No bookings to link – return the saved student as-is
        if (securedStudent.getBookings() == null || securedStudent.getBookings().isEmpty()) {
            return savedStudent;
        }

        // Step 2: Attach bookings with the now-persisted student while keeping the persisted contact
        Student studentWithLinkedBookings = LinkingEntitiesHelper.setStudentInBookings(
                new Student.Builder()
                        .copy(savedStudent)
                        .setBookings(securedStudent.getBookings())
                        .build()        );

        // Step 3: Save again with linked bookings
        return studentRepository.saveAndFlush(studentWithLinkedBookings);
    }

    @Override
    public Student read(Long Id) {
        return studentRepository.findById(Id).orElse(null);
    }

    @Override
    public Student update(Student student) {
        // Optional: validate that student exists first
        if (!studentRepository.existsById(student.getStudentID())) {
            return null;
        }

        Student securedStudent = secureStudent(student);
        // Link bookings properly
        Student studentWithLinkedBookings = LinkingEntitiesHelper.setStudentInBookings(securedStudent);
        return studentRepository.saveAndFlush(studentWithLinkedBookings);
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public void delete(Long Id) {
        studentRepository.deleteById(Id);
    }
    private Student secureStudent(Student student) {
        if (student == null) {
            return null;
        }

        Contact contact = sanitiseContact(student.getContact());

        return new Student.Builder()
                .copy(student)
                .setStudentName(normalise(student.getStudentName()))
                .setStudentSurname(normalise(student.getStudentSurname()))
                .setGender(normalise(student.getGender()))
                .setPassword(hashPassword(student.getPassword()))
                .setContact(contact)
                .build();
    }

    private Contact sanitiseContact(Contact contact) {
        if (contact == null) {
            return null;
        }

        return new Contact.Builder()
                .copy(contact)
                .setEmail(contact.getEmail() != null ? contact.getEmail().trim().toLowerCase(Locale.ROOT) : null)
                .setPhoneNumber(normalise(contact.getPhoneNumber()))
                .setAlternatePhoneNumber(normalise(contact.getAlternatePhoneNumber()))
                .build();
    }

    private String hashPassword(String password) {
        if (password == null || password.isBlank()) {
            return password;
        }
        String trimmed = password.trim();
        if (trimmed.startsWith("$2a$") || trimmed.startsWith("$2b$") || trimmed.startsWith("$2y$")) {
            return trimmed;
        }
        return passwordEncoder.encode(trimmed);
    }

    private String normalise(String value) {
        return value != null ? value.trim() : null;
    }
}
