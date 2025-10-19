package za.ac.student_trade.service;

import org.springframework.web.multipart.MultipartFile;
import za.ac.student_trade.domain.Product;

import java.io.IOException;

public interface IProductService extends IService<Product, Long>{
    Product addProduct(Product product, MultipartFile imageFile) throws IOException;
    Product getProductById(Long id);
    void delete(Long id);
    Product updateProductWithImage(Product product, MultipartFile imageFile) throws IOException;

}
