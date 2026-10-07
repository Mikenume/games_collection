package com.miguel.gamescollection.security;

import com.miguel.gamescollection.model.Role;
import com.miguel.gamescollection.model.User;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// Usuario autenticado que se guarda en la sesión. Lleva solo los datos que
// hacen falta, no la entidad User: la sesión se serializa y una entidad JPA
// con relaciones LAZY daría problemas.
public class UserPrincipal implements UserDetails, CredentialsContainer {

    private final Integer id;
    private final String username;
    private String password;
    private final Role role;
    private final boolean enabled;

    public UserPrincipal(Integer id, String username, String password, Role role, boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(user.getId(), user.getUsername(), user.getPassword(),
                user.getRole(), user.isEnabled());
    }

    public Integer getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isDemo() {
        return role == Role.DEMO;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
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
    public boolean isEnabled() {
        return enabled;
    }

    // Spring la llama tras el login para no guardar la contraseña en la sesión
    @Override
    public void eraseCredentials() {
        password = null;
    }
}
