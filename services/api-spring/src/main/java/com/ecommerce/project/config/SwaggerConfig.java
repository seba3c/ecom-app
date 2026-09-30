package com.ecommerce.project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public static final class Tags {

        private Tags() {}

        public static final class Auth {
            public static final String NAME = "Auth";
            public static final String DESCRIPTION = "Managing authentication and user sessions";
        }

        public static final class Category {
            public static final String NAME = "Category";
            public static final String DESCRIPTION = "Managing product categories";
        }

        public static final class Product {
            public static final String NAME = "Product";
            public static final String DESCRIPTION = "Managing products";
        }

        public static final class Cart {
            public static final String NAME = "Cart";
            public static final String DESCRIPTION = "Managing the shopping cart";
        }

        public static final class Order {
            public static final String NAME = "Order";
            public static final String DESCRIPTION = "Managing orders and payments";
        }

        public static final class Address {
            public static final String NAME = "Address";
            public static final String DESCRIPTION = "Managing user addresses";
        }
    }

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT Bearer token");

        SecurityRequirement bearerRequirement = new SecurityRequirement().addList("Bearer Authentication");

        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot eCommerce API")
                        .version("1.0")
                        .description("API documentation for Spring Boot eCommerce application")
                        .license(new License().name("Apache 2.0")))
                .components(new Components().addSecuritySchemes("Bearer Authentication", bearerScheme))
                .addSecurityItem(bearerRequirement)
                .tags(List.of(
                        new Tag().name(Tags.Auth.NAME).description(Tags.Auth.DESCRIPTION),
                        new Tag().name(Tags.Category.NAME).description(Tags.Category.DESCRIPTION),
                        new Tag().name(Tags.Product.NAME).description(Tags.Product.DESCRIPTION),
                        new Tag().name(Tags.Cart.NAME).description(Tags.Cart.DESCRIPTION),
                        new Tag().name(Tags.Order.NAME).description(Tags.Order.DESCRIPTION),
                        new Tag().name(Tags.Address.NAME).description(Tags.Address.DESCRIPTION)));
    }
}
