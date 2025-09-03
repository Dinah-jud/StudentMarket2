package za.ac.student_trade.domain;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "student")
public class Student {

    @Id @Column(name = "student_id")
    protected String studentId;

    @Column(name = "first_name", nullable = false)
    protected String firstName;

    @Column(name = "last_name", nullable = false)
    protected String lastName;

    @Column(name = "email", nullable = false)
    protected String email;

    @Column(name = "password", nullable = false)
    protected String password;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "residence_id")
    protected Residence residence;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    protected List<Product> productForSale;

    @OneToMany(mappedBy = "buyer")
    protected List<Transaction> purchases;

    protected Student() {}

    public Student(Builder builder) {
        this.studentId = builder.studentId;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.password = builder.password;
        this.residence = builder.residence;
        this.productForSale = builder.productForSale;
        this.purchases = builder.purchases;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Residence getResidence() {
        return residence;
    }

    public List<Product> getProductForSale() {
        return productForSale;
    }

    public List<Transaction> getPurchases() {
        return purchases;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", residence=" + residence +
                ", productForSale=" + productForSale +
                ", purchases=" + purchases +
                '}';
    }

    public static class Builder {
        private String studentId;
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private Residence residence;
        private List<Product> productForSale;
        private List<Transaction> purchases;

        public Builder setStudentId(String studentId) {
            this.studentId = studentId;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setResidence(Residence residence) {
            this.residence = residence;
            return this;
        }

        public Builder setProductForSale(List<Product> productForSale) {
            this.productForSale = productForSale;
            return this;
        }

        public Builder setPurchases(List<Transaction> purchases) {
            this.purchases = purchases;
            return this;
        }

        public Builder copy(Student student) {
            this.studentId = student.studentId;
            this.firstName = student.getFirstName();
            this.lastName = student.getLastName();
            this.email = student.getEmail();
            this.password = student.getPassword();
            this.residence = student.getResidence();
            this.productForSale = student.getProductForSale();
            this.purchases = student.getPurchases();
            return this;
        }

        public Student build() {
            return new Student(this);
        }
    }
}
