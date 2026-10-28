package dkhoa.chemistrylabvr.controller;

import dkhoa.chemistrylabvr.dto.projection.ChemicalDetail;
import dkhoa.chemistrylabvr.dto.request.chemical.CreateChemical;
import dkhoa.chemistrylabvr.dto.request.chemical.UpdateChemical;
import dkhoa.chemistrylabvr.entity.Chemical;
import dkhoa.chemistrylabvr.service.ChemicalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/chemical")
@CrossOrigin("*")
@Tag(name = "Chemical", description = "CRUD Chemical")
@RequiredArgsConstructor
public class ChemicalController {
    private final ChemicalService chemicalService;

    @GetMapping("/all")
    public ResponseEntity<List<Chemical>> getAll() {
        return ResponseEntity.ok(chemicalService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChemicalDetail> getOne(@PathVariable("id") Long chemical_id) {
        return ResponseEntity.ok(chemicalService.getOne(chemical_id));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createChemical(@Valid @RequestBody CreateChemical dto) {
        chemicalService.create(dto);
        return ResponseEntity.ok("Thêm chất thành công");
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateChemical(@PathVariable("id") Long chemical_id,
                                            @Valid @RequestBody UpdateChemical dto) {
        chemicalService.update(chemical_id, dto);
        return ResponseEntity.ok("Cập nhật thành công");
    }
}
