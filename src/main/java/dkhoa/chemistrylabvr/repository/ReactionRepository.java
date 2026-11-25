package dkhoa.chemistrylabvr.repository;

import dkhoa.chemistrylabvr.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    List<Reaction> findAllByIsDeletedFalseOrderByIdDesc();

    Optional<Reaction> findByIdAndIsDeletedFalse(Long id);
}
