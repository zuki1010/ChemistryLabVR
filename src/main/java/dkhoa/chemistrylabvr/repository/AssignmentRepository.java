package dkhoa.chemistrylabvr.repository;

import dkhoa.chemistrylabvr.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
}