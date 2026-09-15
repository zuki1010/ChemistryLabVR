package dkhoa.chemistrylabvr.service;

import dkhoa.chemistrylabvr.dto.request.CreateChemical;
import dkhoa.chemistrylabvr.entity.Chemical;
import dkhoa.chemistrylabvr.enums.ChemicalState;
import dkhoa.chemistrylabvr.exception.AppException;
import dkhoa.chemistrylabvr.repository.ChemicalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChemicalService {
    private final ChemicalRepository chemicalRepository;

    public List<Chemical> getAll() {
        return chemicalRepository.findAll();
    }

    public Chemical getOne(long id) {
        return chemicalRepository.findById(id)
                .orElseThrow(() -> new AppException("Chất này không tồn tại!"));
    }

    public void create(CreateChemical dto) {
        if(chemicalRepository.findByFormula(dto.getFormula())) {
            throw new AppException("Công thức hóa học đã tồn tại!");
        }
        String unity_asset_id = generateUnityAsset(dto.getFormula(), dto.getState());

    }

    public String generateUnityAsset(String formula, ChemicalState state) {
        if(formula==null) return "prefab_generic_";
        return String.format("%s_%s", formula.toLowerCase(), state.name().toLowerCase());
    }

}
