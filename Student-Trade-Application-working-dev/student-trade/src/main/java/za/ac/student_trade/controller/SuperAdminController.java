package za.ac.student_trade.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.ac.student_trade.domain.Administrator;
import za.ac.student_trade.domain.SuperAdmin;
import za.ac.student_trade.service.Impl.SuperAdminServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/superadmin")
public class SuperAdminController {

    private final SuperAdminServiceImpl superAdminService;

    @Autowired
    public SuperAdminController(SuperAdminServiceImpl superAdminService) {
        this.superAdminService = superAdminService;
    }

    // Super Admin login
    @GetMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            List<SuperAdmin> superAdmins = superAdminService.findByEmailAndPassword(email.trim(), password.trim());

            if (!superAdmins.isEmpty()) {
                SuperAdmin superAdmin = superAdmins.get(0);

                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("success", true);
                successResponse.put("message", "Login successful");
                successResponse.put("role", "superadmin");

                Map<String, Object> superAdminData = new HashMap<>();
                superAdminData.put("superAdminId", superAdmin.getSuperAdminId());
                superAdminData.put("username", superAdmin.getUsername());
                superAdminData.put("email", superAdmin.getEmail());
                successResponse.put("data", superAdminData);

                return ResponseEntity.ok(successResponse);
            }

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

    // Create Admin (Super Admin only)
    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createAdmin(@RequestBody Administrator administrator) {
        try {
            // Validate input
            if (administrator.getEmail() == null || administrator.getEmail().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Email is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            if (administrator.getUsername() == null || administrator.getUsername().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Username is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            if (administrator.getPassword() == null || administrator.getPassword().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Password is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            Administrator createdAdmin = superAdminService.createAdmin(administrator);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Administrator created successfully");
            response.put("data", createdAdmin);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            
            // Check for specific error types
            if (e.getMessage().contains("already exists")) {
                errorResponse.put("message", e.getMessage());
                errorResponse.put("errorType", "DUPLICATE_EMAIL");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            } else {
                errorResponse.put("message", "Failed to create administrator: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Update Admin (Super Admin only)
    @PutMapping("/admin/update")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateAdmin(@RequestBody Administrator administrator) {
        try {
            Administrator updatedAdmin = superAdminService.updateAdmin(administrator);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Administrator updated successfully");
            response.put("data", updatedAdmin);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            
            // Check for specific error types
            if (e.getMessage().contains("already in use")) {
                errorResponse.put("message", e.getMessage());
                errorResponse.put("errorType", "DUPLICATE_EMAIL");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            } else if (e.getMessage().contains("not found")) {
                errorResponse.put("message", e.getMessage());
                errorResponse.put("errorType", "NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            } else {
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Delete Admin (Super Admin only)
    @DeleteMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id) {
        try {
            superAdminService.deleteAdmin(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Administrator deleted successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete administrator: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // Get Admin by ID (Super Admin only)
    @GetMapping("/admin/read/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAdminById(@PathVariable Long id) {
        try {
            Administrator admin = superAdminService.getAdminById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", admin);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // Get All Admins (Super Admin only)
    @GetMapping("/admin/getAll")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAllAdmins() {
        try {
            List<Administrator> admins = superAdminService.getAllAdmins();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", admins);
            response.put("count", admins.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve administrators: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}