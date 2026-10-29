package dkhoa.chemistrylabvr.service;

import dkhoa.chemistrylabvr.dto.projection.ChemicalDetail;
import dkhoa.chemistrylabvr.dto.request.chemical.CreateChemical;
import dkhoa.chemistrylabvr.dto.request.chemical.UpdateChemical;
import dkhoa.chemistrylabvr.entity.Chemical;
import dkhoa.chemistrylabvr.enums.ChemicalState;
import dkhoa.chemistrylabvr.exception.AppException;
import dkhoa.chemistrylabvr.repository.ChemicalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChemicalService {
    private final ChemicalRepository chemicalRepository;

    public List<Chemical> getAll() {
        return chemicalRepository.findAll();
    }

    public ChemicalDetail getOne(Long id) {
        if(id==null) throw new AppException("id chất không được để trống!", HttpStatus.BAD_REQUEST);
        return chemicalRepository.findProjectedById(id)
                .orElseThrow(() -> new AppException("Chất này không tồn tại!", HttpStatus.NOT_FOUND));
    }

    public void create(CreateChemical dto) {
        if(chemicalRepository.existsByFormulaIgnoreCase(dto.getFormula().trim())) {
            throw new AppException("Công thức hóa học đã tồn tại!", HttpStatus.CONFLICT);
        } else if(chemicalRepository.existsByNameIgnoreCase(dto.getName().trim())) {
            throw new AppException("Tên chất này đã tồn tại!", HttpStatus.CONFLICT);
        }
        String unity_asset_id = generateUnityAsset(dto.getFormula(), dto.getState());
        Chemical c = new Chemical();
        c.setName(dto.getName().trim());
        c.setFormula(dto.getFormula().trim());
        c.setState(dto.getState());
        c.setColorHex(dto.getColor_hex().trim());
        c.setDescription(dto.getDescription());
        c.setUnityAssetId(unity_asset_id);
        c.setCreatedAt(Instant.now());
        c.setDensity(dto.getDensity());
        c.setDangerLevel(dto.getDanger_level());
        c.setUnit(defaultUnitFor(dto.getState()));

        chemicalRepository.save(c);
    }

    public void update(long id, UpdateChemical dto) {
        Chemical c = chemicalRepository.findById(id)
                .orElseThrow(() -> new AppException("Chất này không có trong hệ thống!", HttpStatus.NOT_FOUND));
        if(chemicalRepository.existsByFormulaIgnoreCaseAndIdNot(dto.getFormula().trim(), id)) {
            throw new AppException("Công thức hóa học đã tồn tại!", HttpStatus.CONFLICT);
        } else if(chemicalRepository.existsByNameIgnoreCaseAndIdNot(dto.getName().trim(), id)) {
            throw new AppException("Tên chất này đã tồn tại!", HttpStatus.CONFLICT);
        }
        c.setName(dto.getName().trim());
        c.setFormula(dto.getFormula().trim());
        c.setColorHex(dto.getColor_hex().trim());
        c.setDensity(dto.getDensity());
        c.setState(dto.getState());
        c.setUnit(defaultUnitFor(dto.getState()));
        c.setDangerLevel(dto.getDanger_level());
        c.setDescription(dto.getDescription());
        String unity_asset = generateUnityAsset(dto.getFormula(), dto.getState());
        c.setUnityAssetId(unity_asset);

        chemicalRepository.save(c);
    }

    public void delete(long id) {

    }

    public String generateUnityAsset(String formula, ChemicalState state) {
        if(formula==null) return "prefab_generic_";
        return String.format("prefab_%s_%s", state.name().toLowerCase().trim(), formula.toLowerCase().trim());
    }

    public String defaultUnitFor(ChemicalState state) {
        return switch (state) {
            case GAS -> "L";
            case SOLID -> "g";
            case LIQUID -> "mL";
        };
    }

}
