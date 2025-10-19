package za.ac.student_trade.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import za.ac.student_trade.domain.Role;

import java.util.Collection;
import java.util.Collections;

public class CustomUserPrincipal implements UserDetails {
    private String username;
    private String password;
    private String userId;
    private Role role;
    private boolean enabled;

    public CustomUserPrincipal(String username, String password, String userId, Role role) {
        this.username = username;
        this.password = password;
        this.userId = userId;
        this.role = role;
        this.enabled = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }
}