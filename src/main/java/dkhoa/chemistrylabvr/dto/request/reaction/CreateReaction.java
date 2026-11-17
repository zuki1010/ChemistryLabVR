package dkhoa.chemistrylabvr.dto.request.reaction;

import dkhoa.chemistrylabvr.dto.response.reaction.ReactionComponent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReaction {
    @NotNull
    private List<ReactionComponentRequest> reactants;
    @NotNull
    private List<ReactionComponentRequest> products;
    private String conditions;
    private String visualEffects;
    @Size(max = 255)
    private String description;
}
