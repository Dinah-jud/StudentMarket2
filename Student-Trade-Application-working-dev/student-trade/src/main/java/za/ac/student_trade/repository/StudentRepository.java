package za.ac.student_trade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.student_trade.domain.Student;


import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    List<Student> findByFirstNameAndLastName(String firstName, String lastName);
    List<Student> findByEmailAndPassword(String email, String password);
    Optional<Student> findByEmail(String email);
}
