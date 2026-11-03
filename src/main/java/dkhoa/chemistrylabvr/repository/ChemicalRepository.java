package dkhoa.chemistrylabvr.repository;

import dkhoa.chemistrylabvr.dto.projection.ChemicalDetail;
import dkhoa.chemistrylabvr.entity.Chemical;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChemicalRepository extends JpaRepository<Chemical, Long> {
    Optional<ChemicalDetail> findProjectedById(Long id);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByFormulaAndIdNot(String formula, Long id);

    boolean existsByFormula(String formula);

    List<Chemical> findAllByIsDeleted(Boolean isDeleted);

    boolean findByFormula(String formula);

    boolean findByFormulaAndIsDeleted(String formula, Boolean isDeleted);

    boolean existsByFormulaAndIsDeleted(String formula, Boolean isDeleted);
}
