package za.ac.student_trade.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import za.ac.student_trade.domain.Administrator;
import za.ac.student_trade.domain.Student;
import za.ac.student_trade.domain.SuperAdmin;
import za.ac.student_trade.config.CustomUserPrincipal;
import za.ac.student_trade.service.Impl.AdministratorServiceImpl;
import za.ac.student_trade.service.Impl.StudentServiceImpl;
import za.ac.student_trade.service.Impl.SuperAdminServiceImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("api/student")
public class StudentController {

    private StudentServiceImpl studentService;
    private AdministratorServiceImpl administratorServiceImpl;
    private SuperAdminServiceImpl superAdminServiceImpl;

    @Autowired
    public void setStudentService(StudentServiceImpl studentService) {
        this.studentService = studentService;
    }

    @Autowired
    public void setAdministratorServiceImpl(AdministratorServiceImpl administratorServiceImpl) {
        this.administratorServiceImpl = administratorServiceImpl;
    }

    @Autowired
    public void setSuperAdminServiceImpl(SuperAdminServiceImpl superAdminServiceImpl) {
        this.superAdminServiceImpl = superAdminServiceImpl;
    }

    @PostMapping("/create")
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        return ResponseEntity.ok(studentService.create(student));
    }


    @GetMapping("/read/{id}")
    @PreAuthorize("hasRole('STUDENT') and #id == authentication.principal.userId or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Student read(@PathVariable String id) {
        return this.studentService.read(id);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('STUDENT') and #student.studentId == authentication.principal.userId or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Student update(@RequestBody Student student) {
        return this.studentService.update(student);
    }

    @PatchMapping(value = "/update/{id}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.userId or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Student> updateStudent(
            @PathVariable("id") String studentId,
            @RequestPart("student") Student studentRequest,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) throws IOException {
        Student updated = studentService.updateStudent(studentId, studentRequest, profileImage);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public List<Student> getAll() {
        return this.studentService.getAll();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable String id) {
        studentService.delete(id);
        return ResponseEntity.ok().build();
    }

    // Get current student profile (for authenticated student)
    @GetMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getCurrentStudentProfile() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            CustomUserPrincipal userPrincipal = (CustomUserPrincipal) auth.getPrincipal();
            String studentId = userPrincipal.getUserId();
            
            Student student = studentService.read(studentId);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Student not found"));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", student
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error retrieving profile: " + e.getMessage()));
        }
    }

    @GetMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            // Priority 1: Check SuperAdmin first (highest authority)
            List<SuperAdmin> superAdmins = superAdminServiceImpl.findByEmailAndPassword(email.trim(), password.trim());
            if (!superAdmins.isEmpty()) {
                SuperAdmin superAdmin = superAdmins.get(0);

                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("success", true);
                successResponse.put("message", "Super Admin login successful");
                successResponse.put("role", "superadmin");

                Map<String, Object> superAdminData = new HashMap<>();
                superAdminData.put("superAdminId", superAdmin.getSuperAdminId());
                superAdminData.put("username", superAdmin.getUsername());
                superAdminData.put("email", superAdmin.getEmail());
                successResponse.put("data", superAdminData);

                return ResponseEntity.ok(successResponse);
            }

            // Priority 2: Check Admin
            List<Administrator> admins = administratorServiceImpl.findByEmailAndPassword(email.trim(), password.trim());
            if (!admins.isEmpty()) {
                Administrator admin = admins.get(0);
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("success", true);
                successResponse.put("message", "Admin login successful");
                successResponse.put("role", "admin");

                Map<String, Object> adminData = new HashMap<>();
                adminData.put("adminId", admin.getAdminId());
                adminData.put("firstName", admin.getUsername());
                adminData.put("email", admin.getEmail());
                successResponse.put("data", adminData);

                return ResponseEntity.ok(successResponse);
            }

            // Priority 3: Check Student
            List<Student> students = studentService.findByEmailAndPassword(email.trim(), password.trim());
            if (!students.isEmpty()) {
                Student student = students.get(0);

                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("success", true);
                successResponse.put("message", "Student login successful");
                successResponse.put("role", "student");

                Map<String, Object> studentData = new HashMap<>();
                studentData.put("studentId", student.getStudentId());
                studentData.put("firstName", student.getFirstName());
                studentData.put("lastName", student.getLastName());
                studentData.put("email", student.getEmail());
                successResponse.put("data", studentData);

                return ResponseEntity.ok(successResponse);
            }

            // None found
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {
        try {
            String message = studentService.sendOtp(email);
            return ResponseEntity.ok(Map.of("success", true, "message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@RequestParam String email, @RequestParam String otp, @RequestParam String newPassword) {
        try {
            String message = studentService.resetPassword(email, otp, newPassword);
            return ResponseEntity.ok(Map.of("success", true, "message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}