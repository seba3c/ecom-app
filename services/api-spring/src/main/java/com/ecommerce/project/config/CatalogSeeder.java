package com.ecommerce.project.config;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repository.CategoryRepository;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.security.repository.UserRepository;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.seed-catalog", havingValue = "true")
@Order(2)
public class CatalogSeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final Path catalogPath;

    public CatalogSeeder(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper,
            @Value("${app.seed-catalog.path:../../tools/seed/catalog.json}") String catalogPath) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.catalogPath = Path.of(catalogPath);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        Catalog catalog = objectMapper.readValue(Files.readString(catalogPath), Catalog.class);
        Map<String, User> sellers = List.of("seller2", "seller3").stream()
                .collect(Collectors.toMap(username -> username, username -> userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new IllegalStateException("Missing seed seller: " + username))));

        for (CategoryEntry entry : catalog.categories()) {
            Category category = categoryRepository.findByName(entry.name());
            if (category == null) {
                category = new Category();
                category.setName(entry.name());
                category = categoryRepository.save(category);
            }
            for (ProductEntry item : entry.products()) {
                if (productRepository.findByName(item.name()) != null) {
                    continue;
                }
                Product product = new Product();
                product.setName(item.name());
                product.setDescription(item.description());
                product.setQuantity(item.quantity());
                product.setPrice(item.price());
                product.setDiscount(item.discount());
                product.setCategory(category);
                User seller = sellers.get(item.seller());
                if (seller == null) {
                    throw new IllegalArgumentException("Unknown seed seller: " + item.seller());
                }
                product.setSeller(seller);
                productRepository.save(product);
            }
        }
    }

    public record Catalog(List<CategoryEntry> categories) {}

    public record CategoryEntry(String name, List<ProductEntry> products) {}

    public record ProductEntry(
            String name, String description, int quantity, BigDecimal price, BigDecimal discount, String seller) {}
}
