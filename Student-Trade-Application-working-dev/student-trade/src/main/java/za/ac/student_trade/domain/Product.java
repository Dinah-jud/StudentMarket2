package za.ac.student_trade.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_description")
    private String productDescription;

    @Column(name = "product_condition")
    private String condition;

    @Column(name = "price")
    private Long price;

    @Column(name = "currency")
    private String currency;

    @Column(name = "category")
    private String productCategory;

    @Column(name = "availability")
    private boolean availabilityStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "imageName")
    private String imageName;

    @Column(name = "imageType")
    private String imageType;

    @Lob
    @Column(name = "imageData", columnDefinition = "MEDIUMBLOB")
    private byte[] imageData;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id", nullable = false)
    @JsonIgnoreProperties({"productForSale", "purchases"})
    private Student seller;

    @OneToOne(mappedBy = "product")
    @JsonIgnore // ADDED: Break circular reference
    private Transaction transaction;

    public Product() {
    }

    protected Product(Builder builder) {
        this.productId = builder.productId;
        this.productName = builder.productName;
        this.productDescription = builder.productDescription;
        this.condition = builder.condition;
        this.price = builder.price;
        this.currency = builder.currency;
        this.productCategory = builder.productCategory;
        this.availabilityStatus = builder.availabilityStatus;
        this.imageName = builder.imageName;
        this.imageType = builder.imageType;
        this.imageData = builder.imageData;
        this.releaseDate = builder.releaseDate;
        this.seller = builder.seller;
        this.transaction = builder.transaction;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public String getCondition() {
        return condition;
    }

    public String getCurrency() {
        return currency;
    }

    public Long getPrice() {
        return price;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public boolean isAvailabilityStatus() {
        return availabilityStatus;
    }

    public Student getSeller() {
        return seller;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageType() {
        return imageType;
    }

    public byte[] getImageData() {
        return imageData;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", productDescription='" + productDescription + '\'' +
                ", condition='" + condition + '\'' +
                ", price=" + price + '\'' +
                ", productCategory=" + productCategory +
                ", availabilityStatus=" + availabilityStatus +
                ", releaseDate=" + releaseDate +
                ", imageName='" + imageName + '\'' +
                ", imageType='" + imageType + '\'' +
                ", imageDate=" + Arrays.toString(imageData) + '\'' +
                ", seller=" + seller +
                '}'; // Removed transaction from toString to avoid circular reference
    }

    public static class Builder {
        private Long productId;
        private String productName;
        private String productDescription;
        private String condition;
        private Long price;
        private String currency;
        private String productCategory;
        private boolean availabilityStatus;
        private LocalDate releaseDate;
        private String imageName;
        private String imageType;
        private byte[] imageData;
        private Student seller;
        private Transaction transaction;

        public Builder setProductId(Long productId) {
            this.productId = productId;
            return this;
        }

        public Builder setProductName(String productName) {
            this.productName = productName;
            return this;
        }

        public Builder setProductDescription(String productDescription) {
            this.productDescription = productDescription;
            return this;
        }

        public Builder setCondition(String condition) {
            this.condition = condition;
            return this;
        }

        public Builder setPrice(Long price) {
            this.price = price;
            return this;
        }

        public Builder setCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder setProductCategory(String productCategory) {
            this.productCategory = productCategory;
            return this;
        }

        public Builder setAvailabilityStatus(boolean availabilityStatus) {
            this.availabilityStatus = availabilityStatus;
            return this;
        }


        public Builder setSeller(Student seller) {
            this.seller = seller;
            return this;
        }

        public Builder setTransaction(Transaction transaction) {
            this.transaction = transaction;
            return this;
        }

        public Builder setReleaseDate(LocalDate releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public Builder setImageName(String imageName) {
            this.imageName = imageName;
            return this;
        }

        public Builder setImageType(String imageType) {
            this.imageType = imageType;
            return this;
        }

        public Builder setImageData(byte[] imageData) {
            this.imageData = imageData;
            return this;
        }

        public Builder copy(Product product) {
            this.productId = product.getProductId();
            this.productName = product.getProductName();
            this.productDescription = product.getProductDescription();
            this.condition = product.getCondition();
            this.price = product.getPrice();
            this.currency = product.getCurrency();
            this.productCategory = product.getProductCategory();
            this.availabilityStatus = product.isAvailabilityStatus();
            this.releaseDate = product.getReleaseDate();
            this.imageData = product.getImageData();
            this.imageName = product.getImageName();
            this.imageType = product.getImageType();
            this.seller = product.getSeller();
            this.transaction = product.getTransaction();
            return this;
        }

        public Builder builder(Product product) {
            this.productId = product.getProductId();
            this.productName = product.getProductName();
            this.productDescription = product.getProductDescription();
            this.condition = product.getCondition();
            this.price = product.getPrice();
            this.currency = product.getCurrency();
            this.productCategory = product.getProductCategory();
            this.availabilityStatus = product.isAvailabilityStatus();
            this.releaseDate = product.getReleaseDate();
            this.imageName = product.getImageName();
            this.imageType = product.getImageType();
            this.imageData = product.getImageData();
            this.seller = product.getSeller();
            this.transaction = product.getTransaction();
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }
}