package dkhoa.chemistrylabvr.dto.response.reaction;

import dkhoa.chemistrylabvr.enums.ChemicalState;
import dkhoa.chemistrylabvr.enums.PhaseChange;

public record ReactionComponent(
        Long chemicalId,
        String formula,
        int coefficient,
        ChemicalState state,
        String unityAssetId,
        PhaseChange phaseChange
) {
}
