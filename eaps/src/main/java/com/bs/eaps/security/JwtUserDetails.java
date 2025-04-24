package com.bs.eaps.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * JWT用户详情
 */
public class JwtUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final List<GrantedAuthority> authorities;
    private final Map<String, Object> additionalInfo;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;

    public JwtUserDetails(Long id, String username, String password, List<GrantedAuthority> authorities) {
        this(id, username, password, authorities, null, true, true, true, true);
    }

    public JwtUserDetails(Long id, String username, String password, List<GrantedAuthority> authorities,
            Map<String, Object> additionalInfo) {
        this(id, username, password, authorities, additionalInfo, true, true, true, true);
    }

    public JwtUserDetails(Long id, String username, String password, List<GrantedAuthority> authorities,
            Map<String, Object> additionalInfo, boolean accountNonExpired, boolean accountNonLocked,
            boolean credentialsNonExpired, boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.additionalInfo = additionalInfo;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }
}