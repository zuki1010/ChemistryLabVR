package dkhoa.chemistrylabvr.dto.request.chemical;

import dkhoa.chemistrylabvr.enums.ChemicalState;
import jakarta.validation.constraints.*;
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
    @NotBlank(message = "Tên chất không được để trống!")
    @Size(max = 150)
    private String name;
    @NotBlank(message = "Ký hiệu hóa học không được để trống!")
    private String formula;
    @Positive
    private BigDecimal density;
    private int danger_level;
    private String description;
    @NotNull(message = "Trạng thái của chất không được để trống!")
    private ChemicalState state;
    private String unit;
    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Màu phải ở định dạng hexa")
    private String color_hex;
}
