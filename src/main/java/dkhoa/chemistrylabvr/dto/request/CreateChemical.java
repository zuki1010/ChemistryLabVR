package dkhoa.chemistrylabvr.dto.request;

import dkhoa.chemistrylabvr.enums.ChemicalState;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreateChemical {
    private String name;
    private String formula;
    private ChemicalState state;
    private String color_hex;
    private String description;
    private BigDecimal density;
    private int danger_level;
    private String unit;
}
