package za.ac.student_trade.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import za.ac.student_trade.domain.Residence;
import za.ac.student_trade.domain.Student;
import za.ac.student_trade.factory.StudentFactory;
import za.ac.student_trade.repository.ResidenceRepository;
import za.ac.student_trade.repository.StudentRepository;
import za.ac.student_trade.service.Impl.EmailService;
import za.ac.student_trade.service.IStudentService;

import java.io.IOException;
import java.util.*;

@Service
public class StudentServiceImpl implements IStudentService {

    private final StudentRepository studentRepository;
    private final ResidenceRepository residenceRepository;
    private final EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // Temporary store for OTPs
    private final Map<String, String> otpStore = new HashMap<>();
    private final Map<String, Long> otpExpiry = new HashMap<>();

    public StudentServiceImpl(StudentRepository studentRepository, ResidenceRepository residenceRepository,  EmailService emailService) {
        this.studentRepository = studentRepository;
        this.residenceRepository = residenceRepository;
        this.emailService = emailService;
    }

    @Override
    public Student create(Student student) {

        Student createdStudent = StudentFactory.createStudent(
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getPassword(),
                student.getResidence()
        );
        return studentRepository.save(createdStudent);
    }

    @Override
    public Student read(String s) {
        return this.studentRepository.findById(s).orElse(null);
    }


    @Override
    public Student update(Student student) {
        return this.studentRepository.save(student);
    }

    @Override
    public List<Student> getAll() {
        return this.studentRepository.findAll();
    }

    @Override
    public void delete(String studentId) {
        studentRepository.deleteById(studentId);
    }

    @Override
    public List<Student> findByEmailAndPassword(String email, String password) {
        System.out.println("Searching for email: '" + email + "' and password: '" + password + "'");

        List<Student> result = this.studentRepository.findByEmailAndPassword(email, password);
        System.out.println("Found " + result.size() + " students");

        return result;
    }

    @Override
    public Student updateStudent(String studentId, Student request, MultipartFile profileImage) throws IOException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        Student.Builder studentBuilder = new Student.Builder().copy(student);


        if (request.getFirstName() != null) studentBuilder.setFirstName(request.getFirstName());
        if (request.getLastName() != null) studentBuilder.setLastName(request.getLastName());
        if (request.getEmail() != null) studentBuilder.setEmail(request.getEmail());

        if (request.getResidence() != null) {
            Residence currentResidence = student.getResidence();
            Residence.Builder residenceBuilder = (currentResidence != null)
                    ? new Residence.Builder().copy(currentResidence)
                    : new Residence.Builder();

            residenceBuilder
                    .setResidenceName(request.getResidence().getResidenceName())
                    .setRoomNumber(request.getResidence().getRoomNumber())
                    .setFloorNumber(request.getResidence().getFloorNumber())
                    .setBuildingName(request.getResidence().getBuildingName())
                    .setAddress(request.getResidence().getAddress());

            Residence updatedResidence = residenceBuilder.build();
            residenceRepository.save(updatedResidence);
            studentBuilder.setResidence(updatedResidence);
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            studentBuilder.setProfileImage(profileImage.getBytes());
        }

        Student updatedStudent = studentBuilder.build();
        return studentRepository.save(updatedStudent);
    }

    // Step 1: Send OTP
    public String sendOtp(String email) {
        Optional<Student> studentOpt = studentRepository.findByEmail(email);
        if (studentOpt.isEmpty()) {
            throw new RuntimeException("No account found with that email.");
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        otpStore.put(email, otp);
        otpExpiry.put(email, System.currentTimeMillis() + 5 * 60 * 1000); // 5 mins expiry

        emailService.sendOtpEmail(email, otp);
        return "OTP sent successfully to " + email;
    }

    // Step 2: Verify OTP and reset password
    public String resetPassword(String email, String otp, String newPassword) {
        if (!otpStore.containsKey(email)) {
            throw new RuntimeException("No OTP found for this email. Request a new one.");
        }

        if (!otpStore.get(email).equals(otp)) {
            throw new RuntimeException("Invalid OTP. Please try again.");
        }

        if (System.currentTimeMillis() > otpExpiry.get(email)) {
            otpStore.remove(email);
            otpExpiry.remove(email);
            throw new RuntimeException("OTP expired. Please request a new one.");
        }

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found."));

        String encodedPassword = passwordEncoder.encode(newPassword);

        student = new Student.Builder().copy(student).setPassword(encodedPassword).build();
        studentRepository.save(student);

        otpStore.remove(email);
        otpExpiry.remove(email);
        return "Password reset successful.";
    }
}

