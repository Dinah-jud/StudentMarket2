package za.ac.student_trade.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private String transactionId;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "image_of_product", columnDefinition = "MEDIUMBLOB")
    private byte[] imageOfProduct;


    @Column(name = "product_label")
    private String productLabel;

    @Column(name = "description")
    private String productDescription;

    @Column(name = "product_condition")
    private String productCondition;

    @Column(name = "price")
    private double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private Student buyer;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Student seller;

    public Transaction() {
    }

    protected Transaction(Builder builder) {
        this.transactionId = builder.transactionId;
        this.transactionDate = builder.transactionDate;
        this.price = builder.price;
        this.status = builder.status;
        this.product = builder.product;
        this.buyer = builder.buyer;
        this.seller = builder.seller;
        this.productCondition = builder.productCondition;
        this.productLabel = builder.productLabel;
        this.productDescription = builder.productDescription;
        this.imageOfProduct = builder.imageOfProduct;
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public double getPrice() { return price; }
    public TransactionStatus getStatus() { return status; }
    public Product getProduct() { return product; }
    public Student getBuyer() { return buyer; }
    public Student getSeller() { return seller; }
    public String getProductLabel() { return productLabel; }
    public String getProductDescription() { return productDescription; }
    public String getProductCondition() { return productCondition; }
    public byte[] getImageOfProduct() { return imageOfProduct; }

    public enum TransactionStatus {
        PENDING, COMPLETED, CANCELLED, FAILED
    }

    public static class Builder {
        private String transactionId;
        private LocalDateTime transactionDate;
        private double price;
        private TransactionStatus status;
        private Product product;
        private Student buyer;
        private Student seller;
        private String productLabel;
        private String productDescription;
        private String productCondition;
        private byte[] imageOfProduct;

        public Builder setTransactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder setTransactionDate(LocalDateTime transactionDate) {
            this.transactionDate = transactionDate;
            return this;
        }

        public Builder setPrice(double price) {
            this.price = price;
            return this;
        }

        public Builder setStatus(TransactionStatus status) {
            this.status = status;
            return this;
        }

        public Builder setProduct(Product product) {
            this.product = product;
            return this;
        }

        public Builder setBuyer(Student buyer) {
            this.buyer = buyer;
            return this;
        }

        public Builder setSeller(Student seller) {
            this.seller = seller;
            return this;
        }

        public Builder setProductLabel(String productLabel) {
            this.productLabel = productLabel;
            return this;
        }

        public Builder setProductDescription(String productDescription) {
            this.productDescription = productDescription;
            return this;
        }

        public Builder setProductCondition(String productCondition) {
            this.productCondition = productCondition;
            return this;
        }

        public Builder setImageOfProduct(byte[] imageOfProduct) {
            this.imageOfProduct = imageOfProduct;
            return this;
        }

        public Builder copy(Transaction transaction) {
            this.transactionId = transaction.transactionId;
            this.transactionDate = transaction.transactionDate;
            this.price = transaction.price;
            this.status = transaction.status;
            this.product = transaction.product;
            this.buyer = transaction.buyer;
            this.seller = transaction.seller;
            this.productLabel = transaction.productLabel;
            this.productDescription = transaction.productDescription;
            this.productCondition = transaction.productCondition;
            this.imageOfProduct = transaction.imageOfProduct;
            return this;
        }

        public Transaction build() {
            // Generate UUID if not set
            if (this.transactionId == null) {
                this.transactionId = UUID.randomUUID().toString();
            }
            // Set current date if not set
            if (this.transactionDate == null) {
                this.transactionDate = LocalDateTime.now();
            }
            // Set default status if not set
            if (this.status == null) {
                this.status = TransactionStatus.PENDING;
            }
            return new Transaction(this);
        }
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", transactionDate=" + transactionDate +
                ", price=" + price +
                ", status=" + status +
                ", product=" + product +
                ", buyer=" + buyer +
                ", seller=" + seller +
                '}';
    }
}