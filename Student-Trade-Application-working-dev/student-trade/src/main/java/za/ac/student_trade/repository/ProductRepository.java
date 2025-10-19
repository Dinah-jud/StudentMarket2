package za.ac.student_trade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.ac.student_trade.domain.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySellerStudentIdAndAvailabilityStatusFalse(String studentId);

    List<Product> findBySellerStudentIdAndAvailabilityStatusTrue(String studentId);
}
