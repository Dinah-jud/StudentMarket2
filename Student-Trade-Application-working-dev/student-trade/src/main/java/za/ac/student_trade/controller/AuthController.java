package za.ac.student_trade.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import za.ac.student_trade.domain.Administrator;
import za.ac.student_trade.domain.Role;
import za.ac.student_trade.domain.Student;
import za.ac.student_trade.domain.SuperAdmin;
import za.ac.student_trade.service.Impl.AdministratorServiceImpl;
import za.ac.student_trade.service.Impl.StudentServiceImpl;
import za.ac.student_trade.service.Impl.SuperAdminServiceImpl;
import za.ac.student_trade.util.JwtUtil;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private StudentServiceImpl studentService;

    @Autowired
    private AdministratorServiceImpl administratorService;

    @Autowired
    private SuperAdminServiceImpl superAdminService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String email = loginRequest.getEmail().trim();
            String password = loginRequest.getPassword().trim();

            // Priority 1: Check SuperAdmin first
            List<SuperAdmin> superAdmins = superAdminService.getAll();
            for (SuperAdmin superAdmin : superAdmins) {
                if (superAdmin.getEmail().equals(email) && 
                    passwordEncoder.matches(password, superAdmin.getPassword())) {
                    
                    String token = jwtUtil.generateToken(
                        superAdmin.getEmail(), 
                        Role.SUPER_ADMIN, 
                        superAdmin.getSuperAdminId().toString()
                    );

                    return ResponseEntity.ok(createAuthResponse(
                        "Super Admin login successful", 
                        "SUPER_ADMIN", 
                        token,
                        createSuperAdminData(superAdmin)
                    ));
                }
            }

            // Priority 2: Check Admin
            List<Administrator> admins = administratorService.getAll();
            for (Administrator admin : admins) {
                if (admin.getEmail().equals(email) && 
                    passwordEncoder.matches(password, admin.getPassword())) {
                    
                    String token = jwtUtil.generateToken(
                        admin.getEmail(), 
                        Role.ADMIN, 
                        admin.getAdminId().toString()
                    );

                    return ResponseEntity.ok(createAuthResponse(
                        "Admin login successful", 
                        "ADMIN", 
                        token,
                        createAdminData(admin)
                    ));
                }
            }

            // Priority 3: Check Student
            List<Student> students = studentService.getAll();
            for (Student student : students) {
                if (student.getEmail().equals(email) && 
                    passwordEncoder.matches(password, student.getPassword())) {
                    
                    String token = jwtUtil.generateToken(
                        student.getEmail(), 
                        Role.STUDENT, 
                        student.getStudentId()
                    );

                    return ResponseEntity.ok(createAuthResponse(
                        "Student login successful", 
                        "STUDENT", 
                        token,
                        createStudentData(student)
                    ));
                }
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse("Invalid email or password"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @PostMapping("/register/student")
    public ResponseEntity<?> registerStudent(@Valid @RequestBody Student student) {
        try {
            // Check if email already exists
            List<Student> existingStudents = studentService.findByEmailAndPassword(student.getEmail(), "");
            if (!existingStudents.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Email already registered"));
            }

            // Hash password before saving
            Student studentToCreate = new Student.Builder()
                .copy(student)
                .setPassword(passwordEncoder.encode(student.getPassword()))
                .build();

            Student createdStudent = studentService.create(studentToCreate);

            String token = jwtUtil.generateToken(
                createdStudent.getEmail(), 
                Role.STUDENT, 
                createdStudent.getStudentId()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(createAuthResponse(
                    "Student registration successful", 
                    "STUDENT", 
                    token,
                    createStudentData(createdStudent)
                ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // For JWT, logout is handled on client side by removing token
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }

    // Helper methods
    private Map<String, Object> createAuthResponse(String message, String role, String token, Map<String, Object> userData) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("role", role);
        response.put("token", token);
        response.put("data", userData);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }

    private Map<String, Object> createStudentData(Student student) {
        Map<String, Object> data = new HashMap<>();
        data.put("studentId", student.getStudentId());
        data.put("firstName", student.getFirstName());
        data.put("lastName", student.getLastName());
        data.put("email", student.getEmail());
        return data;
    }

    private Map<String, Object> createAdminData(Administrator admin) {
        Map<String, Object> data = new HashMap<>();
        data.put("adminId", admin.getAdminId());
        data.put("username", admin.getUsername());
        data.put("email", admin.getEmail());
        return data;
    }

    private Map<String, Object> createSuperAdminData(SuperAdmin superAdmin) {
        Map<String, Object> data = new HashMap<>();
        data.put("superAdminId", superAdmin.getSuperAdminId());
        data.put("username", superAdmin.getUsername());
        data.put("email", superAdmin.getEmail());
        return data;
    }

    // Inner class for login request
    public static class LoginRequest {
        private String email;
        private String password;

        public LoginRequest() {}

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}