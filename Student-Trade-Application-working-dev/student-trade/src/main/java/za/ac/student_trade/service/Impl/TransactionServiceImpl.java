package za.ac.student_trade.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.student_trade.domain.Product;
import za.ac.student_trade.domain.Student;
import za.ac.student_trade.domain.Transaction;
import za.ac.student_trade.repository.ProductRepository;
import za.ac.student_trade.repository.StudentRepository;
import za.ac.student_trade.repository.TransactionRepository;
import za.ac.student_trade.service.Impl.EmailService;
import za.ac.student_trade.service.ITransactionService;

import java.util.List;

@Service
public class TransactionServiceImpl implements ITransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final StudentRepository studentRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  ProductRepository productRepository,
                                  StudentRepository studentRepository) {
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional
    public Transaction createTransaction(Transaction transaction, Long productSoldId, String buyerId) {
        // Fetch product and buyer
        Product productSold = productRepository.findById(productSoldId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productSoldId));

        // Check if product is still available
        if (!productSold.isAvailabilityStatus()) {
            throw new RuntimeException("Product is no longer available for purchase");
        }

        Student buyer = studentRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + buyerId));

        Student seller = productSold.getSeller();

        // Create transaction with all product details
        Transaction newTransaction = new Transaction.Builder()
                .setPrice(productSold.getPrice())
                .setProduct(productSold)
                .setProductLabel(productSold.getProductName())
                .setImageOfProduct(productSold.getImageData())
                .setProductCondition(productSold.getCondition())
                .setProductDescription(productSold.getProductDescription())
                .setBuyer(buyer)
                .setSeller(seller)
                .setStatus(Transaction.TransactionStatus.COMPLETED)
                .build();

        // Save transaction
        Transaction savedTransaction = transactionRepository.save(newTransaction);

        // Mark product as unavailable
        Product updatedProduct = new Product.Builder()
                .copy(productSold)
                .setAvailabilityStatus(false)
                .build();
        productRepository.save(updatedProduct);

        // Send professional invoice emails with PDF attachments
        try {
            System.out.println("📧 Sending invoice emails...");

            // Send invoice to seller
            emailService.sendSellerInvoice(savedTransaction);
            System.out.println("✅ Seller invoice sent to: " + seller.getEmail());

            // Send invoice to buyer
            emailService.sendBuyerInvoice(savedTransaction);
            System.out.println("✅ Buyer invoice sent to: " + buyer.getEmail());

        } catch (Exception e) {
            System.err.println("❌ Failed to send email notifications: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the transaction if email fails
        }

        return savedTransaction;
    }

    @Override
    public Transaction create(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction read(String id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }

    @Override
    public Transaction update(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> getAll() {
        return transactionRepository.findAll();
    }
}