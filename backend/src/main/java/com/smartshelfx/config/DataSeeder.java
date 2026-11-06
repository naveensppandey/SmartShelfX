package com.smartshelfx.config;

import com.smartshelfx.entity.Category;
import com.smartshelfx.entity.Product;
import com.smartshelfx.entity.User;
import com.smartshelfx.repository.CategoryRepository;
import com.smartshelfx.repository.ProductRepository;
import com.smartshelfx.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * DataSeeder
 * 
 * Runs automatically upon application startup.
 * Seeds initial demo accounts, categories, and inventory items if the database is currently empty.
 * This ensures the application is immediately testable out-of-the-box.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      ProductRepository productRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedCategoriesAndProducts();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            logger.info("Database has no users. Seeding default demo accounts...");

            // 1. Admin account
            User admin = new User(
                    "admin",
                    "admin@smartshelfx.com",
                    passwordEncoder.encode("Admin@123"),
                    "System Administrator",
                    "ADMIN"
            );

            // 2. Warehouse Manager account
            User manager = new User(
                    "manager",
                    "manager@smartshelfx.com",
                    passwordEncoder.encode("Manager@123"),
                    "Warehouse Supervisor",
                    "WAREHOUSE_MANAGER"
            );

            // 3. Vendor account
            User vendor = new User(
                    "vendor",
                    "vendor@smartshelfx.com",
                    passwordEncoder.encode("Vendor@123"),
                    "Logitech Supplier Rep",
                    "VENDOR"
            );

            userRepository.saveAll(List.of(admin, manager, vendor));

            logger.info("Demo users seeded successfully:");
            logger.info("  Admin: admin / Admin@123");
            logger.info("  Manager: manager / Manager@123");
            logger.info("  Vendor: vendor / Vendor@123");
        }
    }

    private void seedCategoriesAndProducts() {
        if (categoryRepository.count() == 0) {
            logger.info("Database has no categories. Seeding initial categories and products...");

            Category electronics = categoryRepository.save(new Category("Electronics", "Laptops, mice, keyboards, and computer peripherals"));
            Category office = categoryRepository.save(new Category("Office Supplies", "Desk chairs, stationery, organizers, and paper products"));
            Category hardware = categoryRepository.save(new Category("Hardware", "Tools, screws, fasteners, and warehouse equipment"));
            Category perishables = categoryRepository.save(new Category("Perishables", "Fast-moving consumable items and refreshments"));

            // Seed demo products
            Product p1 = new Product();
            p1.setName("Logitech MX Master 3S");
            p1.setSku("MOU-LOGI-001");
            p1.setDescription("Wireless ergonomic performance mouse with quiet clicks.");
            p1.setCategory(electronics);
            p1.setVendorId(3L);
            p1.setVendorName("Logitech Supplier Rep");
            p1.setPrice(new BigDecimal("99.99"));
            p1.setCostPrice(new BigDecimal("65.00"));
            p1.setCurrentStock(52);
            p1.setMinStockThreshold(15);
            p1.setReorderQuantity(30);
            p1.setUnit("pcs");

            Product p2 = new Product();
            p2.setName("Keychron K2 Mechanical Keyboard");
            p2.setSku("KEY-K2-002");
            p2.setDescription("75% compact wireless mechanical keyboard with Gateron switches.");
            p2.setCategory(electronics);
            p2.setVendorId(3L);
            p2.setVendorName("Logitech Supplier Rep");
            p2.setPrice(new BigDecimal("89.00"));
            p2.setCostPrice(new BigDecimal("55.00"));
            p2.setCurrentStock(8); // LOW STOCK (< 12)
            p2.setMinStockThreshold(12);
            p2.setReorderQuantity(25);
            p2.setUnit("pcs");

            Product p3 = new Product();
            p3.setName("Dell UltraSharp 27 4K Monitor");
            p3.setSku("MON-DELL-003");
            p3.setDescription("27-inch 4K UHD IPS display with USB-C hub connectivity.");
            p3.setCategory(electronics);
            p3.setPrice(new BigDecimal("549.99"));
            p3.setCostPrice(new BigDecimal("380.00"));
            p3.setCurrentStock(0); // OUT OF STOCK
            p3.setMinStockThreshold(5);
            p3.setReorderQuantity(10);
            p3.setUnit("pcs");

            Product p4 = new Product();
            p4.setName("Ergonomic Mesh Office Chair");
            p4.setSku("OFF-CHR-004");
            p4.setDescription("Breathable mesh high-back chair with adjustable lumbar support.");
            p4.setCategory(office);
            p4.setPrice(new BigDecimal("189.50"));
            p4.setCostPrice(new BigDecimal("110.00"));
            p4.setCurrentStock(22);
            p4.setMinStockThreshold(8);
            p4.setReorderQuantity(15);
            p4.setUnit("pcs");

            Product p5 = new Product();
            p5.setName("Bosch 18V Cordless Drill Kit");
            p5.setSku("HDW-BOS-005");
            p5.setDescription("Compact 1/2-inch drill/driver with 2 lithium batteries and charger.");
            p5.setCategory(hardware);
            p5.setPrice(new BigDecimal("129.00"));
            p5.setCostPrice(new BigDecimal("85.00"));
            p5.setCurrentStock(4); // LOW STOCK (< 10)
            p5.setMinStockThreshold(10);
            p5.setReorderQuantity(20);
            p5.setUnit("pcs");

            productRepository.saveAll(List.of(p1, p2, p3, p4, p5));
            logger.info("Demo products seeded successfully.");
        }
    }
}
