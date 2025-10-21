package co.za.cput.controller.users;

import co.za.cput.domain.users.Student;
import co.za.cput.service.users.implementation.StudentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(originPatterns = "${app.security.cors.allowed-origin-patterns}")
@RequestMapping({"/api/students", "/HouseConnect/Student"})
public class StudentController {

    private final StudentServiceImpl studentService;

    @Autowired
    public StudentController(StudentServiceImpl studentService) {
        this.studentService = studentService;
    }

    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody Student student) {
        if (student == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student details are required."));
        }

        try {
            Student created = studentService.create(student);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", exception.getMessage()));
        }
    }

    @GetMapping("/read/{Id}")
    public ResponseEntity<Student> read(@PathVariable Long Id) {
        Student student = studentService.read(Id);
        if (student == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(student);
    }

    @PutMapping("/update")
    public ResponseEntity<Student> update(@RequestBody Student student) {
        if (student.getStudentID() == null) {
            return ResponseEntity.badRequest().build();
        }
        Student updated = studentService.update(student);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/getAllStudents")
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        if (students == null || students.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(students);
    }

    @DeleteMapping("/delete/{Id}")
    public void delete(@PathVariable Long Id) {
        studentService.delete(Id);
    }
}
