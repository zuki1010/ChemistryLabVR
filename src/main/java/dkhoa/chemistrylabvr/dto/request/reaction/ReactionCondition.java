package dkhoa.chemistrylabvr.dto.request.reaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ReactionCondition(
        @DecimalMin(value = "-273.15", message = "Nhiệt độ không thể thấp hơn độ không tuyệt đối")
        BigDecimal temperatureC,
        @Size(max = 255, message = "Ghi chú điều kiện tối đa 255 ký tự") String notes
) {
}
