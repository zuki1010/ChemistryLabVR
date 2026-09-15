package dkhoa.chemistrylabvr.repository;

import dkhoa.chemistrylabvr.entity.Chemical;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChemicalRepository extends JpaRepository<Chemical, Long> {
    boolean findByFormula(String formula);
}