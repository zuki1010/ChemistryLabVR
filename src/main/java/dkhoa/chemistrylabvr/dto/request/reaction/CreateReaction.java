package dkhoa.chemistrylabvr.dto.request.reaction;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateReaction {
    @NotEmpty(message = "Phải có ít nhất một chất phản ứng")
    @Valid
    private List<ReactionComponentRequest> reactants = new ArrayList<>();

    @NotEmpty(message = "Phải có ít nhất một sản phẩm")
    @Valid
    private List<ReactionComponentRequest> products = new ArrayList<>();

    private List<Long> catalystChemicalIds = new ArrayList<>();

    @Valid
    private ReactionCondition conditions;

    private boolean reversible;

    @NotBlank(message = "Mô tả phản ứng không được để trống")
    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;

    @Size(max = 1000, message = "Nguồn tham khảo tối đa 1000 ký tự")
    private String sourceReference;
}
