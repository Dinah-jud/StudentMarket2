package za.ac.student_trade.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import za.ac.student_trade.domain.Administrator;
import za.ac.student_trade.domain.SuperAdmin;
import za.ac.student_trade.repository.AdministratorRepository;
import za.ac.student_trade.repository.SuperAdminRepository;
import za.ac.student_trade.service.ISuperAdminService;

import java.util.List;

@Service
public class SuperAdminServiceImpl implements ISuperAdminService {

    private final SuperAdminRepository superAdminRepository;
    private final AdministratorRepository administratorRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SuperAdminServiceImpl(SuperAdminRepository superAdminRepository,
                                 AdministratorRepository administratorRepository,
                                 PasswordEncoder passwordEncoder) {
        this.superAdminRepository = superAdminRepository;
        this.administratorRepository = administratorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public SuperAdmin create(SuperAdmin superAdmin) {
        return superAdminRepository.save(superAdmin);
    }

    @Override
    public SuperAdmin read(Long id) {
        return superAdminRepository.findById(id).orElse(null);
    }

    @Override
    public SuperAdmin update(SuperAdmin superAdmin) {
        return superAdminRepository.save(superAdmin);
    }

    @Override
    public List<SuperAdmin> getAll() {
        return superAdminRepository.findAll();
    }

    @Override
    public List<SuperAdmin> findByEmailAndPassword(String email, String password) {
        System.out.println("Searching for super admin with email: '" + email + "' and password: '" + password + "'");
        List<SuperAdmin> result = superAdminRepository.findByEmailAndPassword(email, password);
        System.out.println("Found " + result.size() + " super admin(s)");
        return result;
    }

    // Admin management methods
    @Override
    public Administrator createAdmin(Administrator administrator) {
        try {
            // Check if admin with same email already exists
            String email = administrator.getEmail().trim().toLowerCase();
            boolean emailExists = administratorRepository.findAll()
                    .stream()
                    .anyMatch(admin -> admin.getEmail().trim().toLowerCase().equals(email));
                    
            if (emailExists) {
                throw new RuntimeException("Admin with email '" + administrator.getEmail() + "' already exists");
            }
            
            // Hash the password before saving
            Administrator adminWithHashedPassword = new Administrator.Builder()
                    .copy(administrator)
                    .setEmail(administrator.getEmail().trim()) // Ensure email is trimmed
                    .setPassword(passwordEncoder.encode(administrator.getPassword()))
                    .build();
                    
            Administrator savedAdmin = administratorRepository.save(adminWithHashedPassword);
            
            System.out.println("✅ Admin created successfully:");
            System.out.println("   Email: " + savedAdmin.getEmail());
            System.out.println("   Username: " + savedAdmin.getUsername());
            System.out.println("   ID: " + savedAdmin.getAdminId());
            System.out.println("   🔐 Password has been hashed and stored securely");
            
            return savedAdmin;
            
        } catch (DataIntegrityViolationException e) {
            // This catches database constraint violations (like unique email constraint)
            throw new RuntimeException("Admin with email '" + administrator.getEmail() + "' already exists");
        } catch (RuntimeException e) {
            // Re-throw our custom exceptions
            throw e;
        } catch (Exception e) {
            // Handle any other unexpected exceptions
            throw new RuntimeException("Failed to create admin: " + e.getMessage());
        }
    }

    @Override
    public Administrator updateAdmin(Administrator administrator) {
        try {
            if (!administratorRepository.existsById(administrator.getAdminId())) {
                throw new RuntimeException("Administrator not found with id: " + administrator.getAdminId());
            }
            
            // Check if email is being changed to an existing email (but allow keeping the same email)
            String newEmail = administrator.getEmail().trim().toLowerCase();
            boolean emailExistsForOtherAdmin = administratorRepository.findAll()
                    .stream()
                    .anyMatch(admin -> !admin.getAdminId().equals(administrator.getAdminId()) && 
                                     admin.getEmail().trim().toLowerCase().equals(newEmail));
                                     
            if (emailExistsForOtherAdmin) {
                throw new RuntimeException("Email '" + administrator.getEmail() + "' is already in use by another admin");
            }
            
            // Check if password was changed and hash it if needed
            Administrator existingAdmin = administratorRepository.findById(administrator.getAdminId()).orElse(null);
            Administrator adminToUpdate = new Administrator.Builder()
                    .copy(administrator)
                    .setEmail(administrator.getEmail().trim()) // Ensure email is trimmed
                    .build();
            
            // Only hash password if it's different from the existing hashed password
            if (existingAdmin != null && !existingAdmin.getPassword().equals(administrator.getPassword())) {
                // Check if the new password is already hashed (BCrypt hashes start with $2a$, $2b$, or $2y$)
                String newPassword = administrator.getPassword();
                if (!newPassword.startsWith("$2a$") && !newPassword.startsWith("$2b$") && !newPassword.startsWith("$2y$")) {
                    // Password is not hashed, so hash it
                    adminToUpdate = new Administrator.Builder()
                            .copy(adminToUpdate)
                            .setPassword(passwordEncoder.encode(administrator.getPassword()))
                            .build();
                            
                    System.out.println("🔐 Admin password updated and hashed for: " + administrator.getEmail());
                }
            }
            
            Administrator savedAdmin = administratorRepository.save(adminToUpdate);
            System.out.println("✅ Admin updated successfully: " + savedAdmin.getEmail());
            
            return savedAdmin;
            
        } catch (DataIntegrityViolationException e) {
            // This catches database constraint violations (like unique email constraint)
            throw new RuntimeException("Email '" + administrator.getEmail() + "' is already in use by another admin");
        } catch (RuntimeException e) {
            // Re-throw our custom exceptions
            throw e;
        } catch (Exception e) {
            // Handle any other unexpected exceptions
            throw new RuntimeException("Failed to update admin: " + e.getMessage());
        }
    }

    @Override
    public void deleteAdmin(Long adminId) {
        if (!administratorRepository.existsById(adminId)) {
            throw new RuntimeException("Administrator not found with id: " + adminId);
        }
        administratorRepository.deleteById(adminId);
    }

    @Override
    public Administrator getAdminById(Long adminId) {
        return administratorRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Administrator not found with id: " + adminId));
    }

    @Override
    public List<Administrator> getAllAdmins() {
        return administratorRepository.findAll();
    }
}