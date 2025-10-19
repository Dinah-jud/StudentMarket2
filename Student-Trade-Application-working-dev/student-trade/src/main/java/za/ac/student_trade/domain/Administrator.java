package za.ac.student_trade.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "administrator")
public class Administrator {

    @Id
    @Column(name = "admin_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    @Column(name = "username")
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;
    protected Administrator() {
    }

    private Administrator(Builder administrator) {
        this.adminId = administrator.adminId;
        this.username = administrator.username;
        this.email = administrator.email;
        this.password = administrator.password;
    }

    public Long getAdminId() {
        return adminId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Administrator{" +
                "adminId=" + adminId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public static class Builder {
        private Long adminId;
        private String username;
        private String email;
        private String password;



        public Builder setUsername(String username) {
            this.username = username;
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

        public Builder copy(Administrator administrator) {
            this.adminId = administrator.adminId;
            this.username = administrator.username;
            this.email = administrator.email;
            this.password = administrator.password;
            return this;
        }

        public Builder builder(Administrator admin) {
            this.adminId = admin.getAdminId();
            this.username = admin.getUsername();
            this.email = admin.getEmail();
            this.password = admin.getPassword();
            return this;
        }

        public Administrator build() {
            return new Administrator(this);
        }
    }
}
