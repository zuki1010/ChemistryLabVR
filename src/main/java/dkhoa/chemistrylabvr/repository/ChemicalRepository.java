package dkhoa.chemistrylabvr.repository;

import dkhoa.chemistrylabvr.dto.projection.ChemicalDetail;
import dkhoa.chemistrylabvr.entity.Chemical;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChemicalRepository extends JpaRepository<Chemical, Long> {
    boolean findByFormula(String formula);
    Optional<ChemicalDetail> findProjectedById(Long id);

    boolean existsByFormula(String formula);
}