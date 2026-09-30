package com.ecommerce.project.security.service;

import static org.junit.jupiter.api.Assertions.*;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class UserDetailsImplTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@example.com", "encodedPassword");
        user.setId(1L);
        user.setRoles(Set.of(new Role(AppRole.ROLE_USER)));
    }

    @Test
    void build_mapsUserFieldsCorrectly() {
        UserDetailsImpl details = UserDetailsImpl.build(user);

        assertEquals(1L, details.getId());
        assertEquals("testuser", details.getUsername());
        assertEquals("test@example.com", details.getEmail());
        assertEquals("encodedPassword", details.getPassword());
    }

    @Test
    void build_singleRole_createsCorrectAuthority() {
        UserDetailsImpl details = UserDetailsImpl.build(user);

        assertEquals(1, details.getAuthorities().size());
        assertTrue(details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_USER")));
    }

    @Test
    void build_multipleRoles_createsAllAuthorities() {
        user.setRoles(Set.of(new Role(AppRole.ROLE_USER), new Role(AppRole.ROLE_ADMIN), new Role(AppRole.ROLE_SELLER)));

        UserDetailsImpl details = UserDetailsImpl.build(user);

        assertEquals(3, details.getAuthorities().size());
        Set<String> authorityNames = Set.copyOf(details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        assertTrue(authorityNames.contains("ROLE_USER"));
        assertTrue(authorityNames.contains("ROLE_ADMIN"));
        assertTrue(authorityNames.contains("ROLE_SELLER"));
    }

    @Test
    void userDetailsBooleans_allReturnTrue() {
        UserDetailsImpl details = UserDetailsImpl.build(user);

        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
    }

    @Test
    void equals_sameId_returnsTrue() {
        UserDetailsImpl a = UserDetailsImpl.build(user);

        User other = new User("otheruser", "other@example.com", "pass");
        other.setId(1L);
        other.setRoles(Set.of(new Role(AppRole.ROLE_ADMIN)));
        UserDetailsImpl b = UserDetailsImpl.build(other);

        assertEquals(a, b);
    }

    @Test
    void equals_differentId_returnsFalse() {
        UserDetailsImpl a = UserDetailsImpl.build(user);

        User other = new User("otheruser", "other@example.com", "pass");
        other.setId(2L);
        other.setRoles(Set.of(new Role(AppRole.ROLE_USER)));
        UserDetailsImpl b = UserDetailsImpl.build(other);

        assertNotEquals(a, b);
    }

    @Test
    void hashCode_sameId_sameHashCode() {
        UserDetailsImpl a = UserDetailsImpl.build(user);

        User other = new User("otheruser", "other@example.com", "pass");
        other.setId(1L);
        other.setRoles(Set.of(new Role(AppRole.ROLE_USER)));
        UserDetailsImpl b = UserDetailsImpl.build(other);

        assertEquals(a.hashCode(), b.hashCode());
    }
}
