package za.ac.student_trade.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import za.ac.student_trade.domain.Administrator;
import za.ac.student_trade.domain.Student;
import za.ac.student_trade.domain.SuperAdmin;
import za.ac.student_trade.repository.AdministratorRepository;
import za.ac.student_trade.repository.StudentRepository;
import za.ac.student_trade.repository.SuperAdminRepository;

import java.util.List;

/**
 * This service runs once on application startup to hash existing plain text
 * passwords
 * Remove this component after running once to hash all existing passwords
 */
@Component
@Order(1) // Run first to migrate passwords before other initialization
public class PasswordMigrationService implements CommandLineRunner {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting password migration...");

        // Hash student passwords
        List<Student> students = studentRepository.findAll();
        for (Student student : students) {
            if (!isPasswordHashed(student.getPassword())) {
                String hashedPassword = passwordEncoder.encode(student.getPassword());
                Student updatedStudent = new Student.Builder()
                        .copy(student)
                        .setPassword(hashedPassword)
                        .build();
                studentRepository.save(updatedStudent);
                System.out.println("Updated password for student: " + student.getEmail());
            }
        }

        // Hash administrator passwords
        List<Administrator> administrators = administratorRepository.findAll();
        for (Administrator admin : administrators) {
            if (!isPasswordHashed(admin.getPassword())) {
                String hashedPassword = passwordEncoder.encode(admin.getPassword());
                Administrator updatedAdmin = new Administrator.Builder()
                        .copy(admin)
                        .setPassword(hashedPassword)
                        .build();
                administratorRepository.save(updatedAdmin);
                System.out.println("Updated password for admin: " + admin.getEmail());
            }
        }

        // Hash super admin passwords
        List<SuperAdmin> superAdmins = superAdminRepository.findAll();
        for (SuperAdmin superAdmin : superAdmins) {
            if (!isPasswordHashed(superAdmin.getPassword())) {
                String hashedPassword = passwordEncoder.encode(superAdmin.getPassword());
                SuperAdmin updatedSuperAdmin = new SuperAdmin.Builder()
                        .copy(superAdmin)
                        .setPassword(hashedPassword)
                        .build();
                superAdminRepository.save(updatedSuperAdmin);
                System.out.println("Updated password for super admin: " + superAdmin.getEmail());
            }
        }

        System.out.println("Password migration completed!");
    }

    private boolean isPasswordHashed(String password) {
        // BCrypt hashes start with $2a$, $2b$, or $2y$ and are 60 characters long
        return password != null && password.startsWith("$2") && password.length() == 60;
    }
}