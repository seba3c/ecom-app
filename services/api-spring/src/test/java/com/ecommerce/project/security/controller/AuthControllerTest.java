package com.ecommerce.project.security.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.security.dto.LoginRequest;
import com.ecommerce.project.security.dto.SignupRequest;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.repository.RoleRepository;
import com.ecommerce.project.security.repository.UserRepository;
import com.ecommerce.project.security.service.UserDetailsImpl;
import com.ecommerce.project.security.service.UserDetailsServiceImpl;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@WithSecurityContext(factory = WithUserDetailsImplSecurityContextFactory.class)
@interface WithUserDetailsImpl {
    String username() default "alice";

    long id() default 1L;

    String[] roles() default {"ROLE_USER"};
}

class WithUserDetailsImplSecurityContextFactory implements WithSecurityContextFactory<WithUserDetailsImpl> {
    @Override
    public org.springframework.security.core.context.SecurityContext createSecurityContext(
            WithUserDetailsImpl annotation) {
        List<SimpleGrantedAuthority> authorities = java.util.Arrays.stream(annotation.roles())
                .map(SimpleGrantedAuthority::new)
                .collect(java.util.stream.Collectors.toList());
        UserDetailsImpl principal = new UserDetailsImpl(
                annotation.id(), annotation.username(), annotation.username() + "@test.com", "encoded", authorities);
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
        org.springframework.security.core.context.SecurityContext context =
                org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        return context;
    }
}

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void signin_validCredentials_returns200WithCookieAndUserInfo() throws Exception {
        UserDetailsImpl principal = new UserDetailsImpl(
                1L, "alice", "alice@example.com", "encoded", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        ResponseCookie jwtCookie = ResponseCookie.from("ecommerce-app", "jwt-token-here")
                .path("/api")
                .maxAge(86400)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtils.generateJwtCookie(any(UserDetailsImpl.class))).thenReturn(jwtCookie);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("alice");
        loginRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void signin_badCredentials_returns404WithMessage() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("alice");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Bad credentials"));
    }

    @Test
    void signup_newUser_returns200WithSuccessMessage() throws Exception {
        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password1")).thenReturn("encoded");
        when(roleRepository.findByName(AppRole.ROLE_USER)).thenReturn(Optional.of(new Role(AppRole.ROLE_USER)));

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("bob");
        signupRequest.setEmail("bob@example.com");
        signupRequest.setPassword("password1");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));
    }

    @Test
    void signup_duplicateUsername_returns400() throws Exception {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("alice");
        signupRequest.setEmail("alice@example.com");
        signupRequest.setPassword("password1");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
    }

    @Test
    void signup_duplicateEmail_returns400() throws Exception {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("alice");
        signupRequest.setEmail("alice@example.com");
        signupRequest.setPassword("password1");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Email is already taken!"));
    }

    @Test
    void signup_withAdminRole_returnsBadRequest() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("admin");
        signupRequest.setEmail("admin@example.com");
        signupRequest.setPassword("adminpass");
        signupRequest.setRoles(Set.of("admin"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_withSellerRole_assignsRoleSeller() throws Exception {
        when(userRepository.existsByUsername("seller")).thenReturn(false);
        when(userRepository.existsByEmail("seller@example.com")).thenReturn(false);
        when(passwordEncoder.encode("sellerpass")).thenReturn("encoded");
        when(roleRepository.findByName(AppRole.ROLE_SELLER)).thenReturn(Optional.of(new Role(AppRole.ROLE_SELLER)));

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("seller");
        signupRequest.setEmail("seller@example.com");
        signupRequest.setPassword("sellerpass");
        signupRequest.setRoles(Set.of("seller"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));
    }

    @Test
    @WithUserDetailsImpl(
            username = "alice",
            id = 1L,
            roles = {"ROLE_USER"})
    void currentUsername_withAuthenticatedUser_returnsUsername() throws Exception {
        mockMvc.perform(get("/api/auth/username"))
                .andExpect(status().isOk())
                .andExpect(content().string("alice"));
    }

    @Test
    void currentUsername_withoutAuthentication_returnsEmptyString() throws Exception {
        mockMvc.perform(get("/api/auth/username"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @WithUserDetailsImpl(
            username = "alice",
            id = 1L,
            roles = {"ROLE_USER"})
    void currentUserDetails_withAuthenticatedUser_returnsUserInfo() throws Exception {
        mockMvc.perform(get("/api/auth/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void currentUserDetails_withoutAuthentication_returnsMessage() throws Exception {
        mockMvc.perform(get("/api/auth/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No user details found"));
    }

    @Test
    @WithUserDetailsImpl(
            username = "alice",
            id = 1L,
            roles = {"ROLE_USER"})
    void signout_withAuthenticatedUser_returns200AndMessage() throws Exception {
        ResponseCookie cleanCookie =
                ResponseCookie.from("ecommerce-app", null).path("/api").build();

        when(jwtUtils.generateJwtCleanCookie()).thenReturn(cleanCookie);

        mockMvc.perform(post("/api/auth/signout"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.message").value("User signed out successfully!"));
    }

    @Test
    @WithUserDetailsImpl(
            username = "alice",
            id = 1L,
            roles = {"ROLE_USER"})
    void signout_returnsCleanCookie() throws Exception {
        ResponseCookie cleanCookie =
                ResponseCookie.from("ecommerce-app", null).path("/api").build();

        when(jwtUtils.generateJwtCleanCookie()).thenReturn(cleanCookie);

        mockMvc.perform(post("/api/auth/signout")).andExpect(header().string("Set-Cookie", cleanCookie.toString()));
    }

    @Test
    void signout_withoutAuthentication_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/signout"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No user signed in"));
    }
}
