package dkhoa.chemistrylabvr.dto.request.chemical;

import dkhoa.chemistrylabvr.enums.ChemicalState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateChemical {
    private String name;
    private String formula;
    private BigDecimal density;
    private int danger_level;
    private String description;
    private ChemicalState state;
    private String unit;
    private String color_hex;
}
