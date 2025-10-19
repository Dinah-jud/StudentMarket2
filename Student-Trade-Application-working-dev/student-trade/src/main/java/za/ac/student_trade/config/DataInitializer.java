package za.ac.student_trade.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import za.ac.student_trade.domain.Administrator;
import za.ac.student_trade.domain.SuperAdmin;
import za.ac.student_trade.factory.SuperAdminFactory;
import za.ac.student_trade.repository.AdministratorRepository;
import za.ac.student_trade.repository.SuperAdminRepository;

@Component
public class DataInitializer {

    private final SuperAdminRepository superAdminRepository;
    private final AdministratorRepository administratorRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(SuperAdminRepository superAdminRepository, 
                          AdministratorRepository administratorRepository,
                          PasswordEncoder passwordEncoder) {
        this.superAdminRepository = superAdminRepository;
        this.administratorRepository = administratorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        try {
            createProductionSuperAdmin();
            // Also make sure any existing admins have hashed passwords
            hashExistingAdminPasswords();
        } catch (Exception e) {
            System.err.println("❌ Error during DataInitializer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createProductionSuperAdmin() {
        String productionEmail = "superadmin@studenttrade.ac.za";
        String productionPassword = "SuperAdmin@123";
        
        try {
            // Check if the production super admin specifically exists
            boolean productionSuperAdminExists = superAdminRepository.findAll()
                    .stream()
                    .anyMatch(sa -> sa.getEmail().equals(productionEmail));
                    
            if (!productionSuperAdminExists) {
                System.out.println("🔧 Creating production super admin...");
                
                // Create production super admin with properly hashed password
                SuperAdmin superAdmin = new SuperAdmin.Builder()
                        .setUsername("superadmin")
                        .setEmail(productionEmail)
                        .setPassword(passwordEncoder.encode(productionPassword))
                        .build();

                SuperAdmin savedSuperAdmin = superAdminRepository.save(superAdmin);
                
                if (savedSuperAdmin != null && savedSuperAdmin.getSuperAdminId() != null) {
                    System.out.println("========================================");
                    System.out.println("✅ PRODUCTION SUPER ADMIN CREATED");
                    System.out.println("========================================");
                    System.out.println("Username: superadmin");
                    System.out.println("Email: " + productionEmail);
                    System.out.println("Password: " + productionPassword);
                    System.out.println("ID: " + savedSuperAdmin.getSuperAdminId());
                    System.out.println("========================================");
                    System.out.println("🔐 You can now login with these credentials");
                    System.out.println("========================================");
                } else {
                    System.err.println("❌ Failed to save production super admin");
                }
            } else {
                System.out.println("ℹ️  Production Super Admin already exists.");
                System.out.println("📧 Email: " + productionEmail);
                System.out.println("🔑 Password: " + productionPassword);
            }
        } catch (Exception e) {
            System.err.println("❌ Error creating production super admin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void hashExistingAdminPasswords() {
        try {
            System.out.println("🔍 Checking existing admin passwords...");
            
            var admins = administratorRepository.findAll();
            int hashedCount = 0;
            
            for (Administrator admin : admins) {
                String password = admin.getPassword();
                
                // Check if password is not already hashed (BCrypt hashes start with $2a$, $2b$, or $2y$)
                if (password != null && 
                    !password.startsWith("$2a$") && 
                    !password.startsWith("$2b$") && 
                    !password.startsWith("$2y$")) {
                    
                    // Hash the plain text password
                    Administrator updatedAdmin = new Administrator.Builder()
                            .copy(admin)
                            .setPassword(passwordEncoder.encode(password))
                            .build();
                            
                    administratorRepository.save(updatedAdmin);
                    hashedCount++;
                    
                    System.out.println("🔐 Hashed password for admin: " + admin.getEmail());
                }
            }
            
            if (hashedCount > 0) {
                System.out.println("✅ Hashed " + hashedCount + " admin password(s)");
            } else {
                System.out.println("ℹ️  All admin passwords are already hashed");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error hashing admin passwords: " + e.getMessage());
            e.printStackTrace();
        }
    }
}