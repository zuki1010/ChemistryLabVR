package dkhoa.chemistrylabvr.repository;

import dkhoa.chemistrylabvr.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}