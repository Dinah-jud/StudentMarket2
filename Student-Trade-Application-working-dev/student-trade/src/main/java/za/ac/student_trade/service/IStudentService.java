package za.ac.student_trade.service;

import org.springframework.web.multipart.MultipartFile;
import za.ac.student_trade.domain.Student;

import java.io.IOException;
import java.util.List;

public interface IStudentService extends IService<Student , String> {
    List<Student> findByEmailAndPassword(String email, String password);

    void delete(String id);

    Student updateStudent(String studentId, Student request, MultipartFile profileImage) throws IOException;
}
