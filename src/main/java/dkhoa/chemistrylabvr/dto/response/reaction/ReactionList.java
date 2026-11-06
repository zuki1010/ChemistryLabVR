package dkhoa.chemistrylabvr.dto.response.reaction;

import java.util.List;

public record ReactionList(
        Long id,
        String equation,
        List<ReactionComponent> reactants,
        List<ReactionComponent> products,
        String visualEffects,
        String description
) {
}
