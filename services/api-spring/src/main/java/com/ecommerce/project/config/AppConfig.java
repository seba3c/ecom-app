package com.ecommerce.project.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    public static class Pagination {
        public static final String PAGE_NUMBER = "0";
        public static final String PAGE_SIZE = "50";
        public static final String SORT_CATEGORIES_BY = "id";
        public static final String SORT_CATEGORIES_DIR = "asc";
        public static final String SORT_PRODUCTS_BY = "id";
        public static final String SORT_PRODUCTS_DIR = "asc";
        public static final String SORT_CARTS_BY = "id";
        public static final String SORT_CARTS_DIR = "asc";
    }
}
