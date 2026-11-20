package dkhoa.chemistrylabvr.dto.request.reaction;

import dkhoa.chemistrylabvr.enums.ChemicalState;
import dkhoa.chemistrylabvr.enums.PhaseChange;

public record ReactionComponentRequest(
        Long chemicalId,
        int coefficient,
        ChemicalState state,
        PhaseChange phaseChange
) {
}
