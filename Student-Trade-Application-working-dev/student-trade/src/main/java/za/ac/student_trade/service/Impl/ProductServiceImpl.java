package za.ac.student_trade.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import za.ac.student_trade.domain.Product;
import za.ac.student_trade.factory.ProductFactory;
import za.ac.student_trade.repository.ProductRepository;
import za.ac.student_trade.repository.StudentRepository;
import za.ac.student_trade.service.IProductService;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements IProductService {

    private ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product addProduct(Product product, MultipartFile imageFile) throws IOException {
        return productRepository.save(
                ProductFactory.create(
                        product.getProductName(),
                        product.getProductDescription(),
                        product.getCondition(),
                        product.getPrice(),
                        product.getCurrency(),
                        product.getProductCategory(),
                        product.isAvailabilityStatus(),
                        imageFile.getOriginalFilename(),
                        imageFile.getContentType(),
                        imageFile.getBytes(),
                        product.getReleaseDate(),
                        product.getSeller()
                )
        );
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).get();
    }


    @Override
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product read(Long id) {
        return productRepository.findById(id).get();
    }

    @Override
    public Product update(Product product) {
        return productRepository.save(product);
    }


    @Override
    public Product updateProductWithImage(Product product, MultipartFile imageFile) throws IOException {
        // Fetch existing product
        Product existingProduct = productRepository.findById(product.getProductId()).orElse(null);
        if (existingProduct == null) {
            return null;
        }

        byte[] imageData = existingProduct.getImageData();
        String imageType = existingProduct.getImageType();
        String imageName = existingProduct.getImageName();
        if (imageFile != null && !imageFile.isEmpty()) {
            imageData = imageFile.getBytes();
            imageType = imageFile.getContentType();
            imageName = imageFile.getOriginalFilename();
        }

        Product updatedProduct = new Product.Builder()
                .setProductId(existingProduct.getProductId())
                .setProductName(product.getProductName())
                .setProductDescription(product.getProductDescription())
                .setPrice(product.getPrice())
                .setCondition(existingProduct.getCondition())
                .setCurrency(existingProduct.getCurrency())
                .setProductCategory(existingProduct.getProductCategory())
                .setSeller(existingProduct.getSeller())
                .setImageData(imageData)
                .setImageType(imageType)
                .setImageName(imageName)
                .build();


        return productRepository.save(updatedProduct);
    }


    @Override
    public List<Product> getSoldProductsByStudent(String studentId) {
        return productRepository.findBySellerStudentIdAndAvailabilityStatusFalse(studentId);
    }

    @Override
    public List<Product> getAvailableProductsByStudent(String studentId) {
        return productRepository.findBySellerStudentIdAndAvailabilityStatusTrue(studentId);
    }



    @Override
    public List<Product> getAll() {
        return productRepository.findAll();
    }
    @Override
    public void delete(Long productId) {
        productRepository.deleteById(productId);
    }

}
