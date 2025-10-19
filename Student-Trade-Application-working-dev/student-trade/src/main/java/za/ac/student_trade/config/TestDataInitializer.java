package za.ac.student_trade.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import za.ac.student_trade.domain.*;
import za.ac.student_trade.service.Impl.AdministratorServiceImpl;
import za.ac.student_trade.service.Impl.StudentServiceImpl;
import za.ac.student_trade.service.Impl.SuperAdminServiceImpl;

@Component
@Order(2) // Run after PasswordMigrationService
public class TestDataInitializer implements CommandLineRunner {

    @Autowired
    private StudentServiceImpl studentService;

    @Autowired
    private AdministratorServiceImpl administratorService;

    @Autowired
    private SuperAdminServiceImpl superAdminService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createTestUsers();
    }

    private void createTestUsers() {
        try {
            // Create test student if doesn't exist
            if (studentService.getAll().stream().noneMatch(s -> s.getEmail().equals("test.student@example.com"))) {
                Student testStudent = new Student.Builder()
                        .setStudentId("TEST001")
                        .setFirstName("Test")
                        .setLastName("Student")
                        .setEmail("test.student@example.com")
                        .setPassword(passwordEncoder.encode("student123"))
                        .build();

                studentService.create(testStudent);
                System.out.println("✅ Created test student: test.student@example.com / student123");
            }

            // Create test admin if doesn't exist
            if (administratorService.getAll().stream().noneMatch(a -> a.getEmail().equals("test.admin@example.com"))) {
                Administrator testAdmin = new Administrator.Builder()
                        .setUsername("testadmin")
                        .setEmail("test.admin@example.com")
                        .setPassword(passwordEncoder.encode("admin123"))
                        .build();

                administratorService.create(testAdmin);
                System.out.println("✅ Created test admin: test.admin@example.com / admin123");
            }

            // Create test superadmin if doesn't exist
            if (superAdminService.getAll().stream()
                    .noneMatch(sa -> sa.getEmail().equals("test.superadmin@example.com"))) {
                SuperAdmin testSuperAdmin = new SuperAdmin.Builder()
                        .setUsername("testsuperadmin")
                        .setEmail("test.superadmin@example.com")
                        .setPassword(passwordEncoder.encode("superadmin123"))
                        .build();

                superAdminService.create(testSuperAdmin);
                System.out.println("✅ Created test superadmin: test.superadmin@example.com / superadmin123");
            }

        } catch (Exception e) {
            System.err.println("❌ Error creating test users: " + e.getMessage());
        }
    }
}