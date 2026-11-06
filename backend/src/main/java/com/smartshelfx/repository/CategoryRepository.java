package com.smartshelfx.repository;

import com.smartshelfx.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CategoryRepository
 * 
 * Spring Data JPA repository for Category entities.
 * Generates SQL queries automatically based on method names.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Category> findByNameIgnoreCase(String name);
}
