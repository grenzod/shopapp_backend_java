package com.project.shopapp.repositories;

import com.project.shopapp.models.Entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
