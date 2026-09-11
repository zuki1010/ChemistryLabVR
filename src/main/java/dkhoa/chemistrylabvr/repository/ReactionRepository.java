package dkhoa.chemistrylabvr.repository;

import dkhoa.chemistrylabvr.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
}