package dkhoa.chemistrylabvr.service;

import dkhoa.chemistrylabvr.dto.projection.ChemicalDetail;
import dkhoa.chemistrylabvr.dto.request.chemical.CreateChemical;
import dkhoa.chemistrylabvr.dto.request.chemical.UpdateChemical;
import dkhoa.chemistrylabvr.entity.Chemical;
import dkhoa.chemistrylabvr.enums.ChemicalState;
import dkhoa.chemistrylabvr.exception.AppException;
import dkhoa.chemistrylabvr.repository.ChemicalRepository;
import lombok.RequiredArgsConstructor;
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
        if(id==null) throw new AppException("id chất không được để trống!");
        return chemicalRepository.findProjectedById(id)
                .orElseThrow(() -> new AppException("Chất này không tồn tại!"));
    }

    public void create(CreateChemical dto) {
        if(chemicalRepository.existsByFormula(dto.getFormula())) {
            throw new AppException("Công thức hóa học đã tồn tại!");
        }
        String unity_asset_id = generateUnityAsset(dto.getFormula(), dto.getState());
        Chemical c = new Chemical();
        c.setName(dto.getName());
        c.setFormula(dto.getFormula());
        c.setState(dto.getState());
        c.setColorHex(dto.getColor_hex());
        c.setDescription(dto.getDescription());
        c.setUnityAssetId(unity_asset_id);
        c.setCreatedAt(Instant.now());
        c.setDensity(dto.getDensity());
        c.setDangerLevel(dto.getDanger_level());
        c.setUnit(dto.getUnit());

        chemicalRepository.save(c);
    }

    public void update(long id, UpdateChemical dto) {
        Chemical c = chemicalRepository.findById(id)
                .orElseThrow(() -> new AppException("Chất này không có trong hệ thống!"));
        c.setName(dto.getName());
        c.setFormula(dto.getFormula());
        c.setColorHex(dto.getColor_hex());
        c.setDensity(dto.getDensity());
        c.setState(dto.getState());
        c.setUnit(dto.getUnit());
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
        return String.format("prefab_%s_%s", state.name().toLowerCase(), formula.toLowerCase());
    }

}
