package com.example.vjutest.User;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.vjutest.Model.User;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    
    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRole() != null ? 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().toUpperCase())) : 
            Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    public Long getId() {
        return user.getId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Bạn có thể thay đổi logic này tùy theo yêu cầu
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Bạn có thể thay đổi logic này tùy theo yêu cầu
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Bạn có thể thay đổi logic này tùy theo yêu cầu
    }

    @Override
    public boolean isEnabled() {
        return user.getIsEnabled();  // Trả về trạng thái kích hoạt của user
    }
}
