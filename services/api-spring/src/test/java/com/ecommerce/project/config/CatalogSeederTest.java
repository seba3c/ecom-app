package com.ecommerce.project.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ecommerce.project.model.Product;
import com.ecommerce.project.repository.CategoryRepository;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.security.repository.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "app.seed-catalog=true")
@ActiveProfiles({"dev", "dev_h2"})
class CatalogSeederTest {

    @Autowired
    private CatalogSeeder catalogSeeder;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void seedsCatalogAndPreservesExistingRowsOnRerun() throws Exception {
        assertEquals(10, categoryRepository.count());
        assertEquals(100, productRepository.count());
        assertNotNull(userRepository.findByUsername("seller2").orElseThrow());
        assertNotNull(userRepository.findByUsername("seller3").orElseThrow());
        assertEquals(
                50,
                productRepository.findAll().stream()
                        .filter(product -> product.getSeller().getUsername().equals("seller2"))
                        .count());
        assertEquals(
                50,
                productRepository.findAll().stream()
                        .filter(product -> product.getSeller().getUsername().equals("seller3"))
                        .count());
        assertEquals(
                10,
                productRepository.findAll().stream()
                        .filter(product -> product.getCategory().getName().equals("Electronics"))
                        .count());

        Product existing = productRepository.findByName("Wireless Headphones");
        existing.setPrice(new BigDecimal("321.00"));
        productRepository.save(existing);
        catalogSeeder.run(new DefaultApplicationArguments());

        assertEquals(10, categoryRepository.count());
        assertEquals(100, productRepository.count());
        assertEquals(
                new BigDecimal("321.00"),
                productRepository.findByName("Wireless Headphones").getPrice());
    }
}
