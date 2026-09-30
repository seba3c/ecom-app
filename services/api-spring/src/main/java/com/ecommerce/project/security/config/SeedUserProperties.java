package com.ecommerce.project.security.config;

import com.ecommerce.project.model.AppRole;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "spring.app")
public class SeedUserProperties {

    private List<SeedUser> seedUsers = new ArrayList<>();

    @Setter
    @Getter
    public static class SeedUser {
        private String username;
        private String email;
        private String password;
        private Set<AppRole> roles = new HashSet<>();
    }
}
