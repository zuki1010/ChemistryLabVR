package dkhoa.chemistrylabvr.dto.projection;

import java.time.Instant;

public interface ChemicalDetail {
    String getName();
    String getFormula();
    String getState();
    String getColorHex();
    Instant getCreatedAt();
    Double getDensity();
    int getDangerLevel();
    String getUnit();
}
