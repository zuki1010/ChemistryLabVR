package dkhoa.chemistrylabvr.dto.request.reaction;

import dkhoa.chemistrylabvr.enums.PhaseChange;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReactionComponentRequest(
        @NotNull(message = "Phải chọn hóa chất") Long chemicalId,
        @Min(value = 1, message = "Hệ số phải lớn hơn 0") int coefficient,
        PhaseChange phaseChange
) {
}
