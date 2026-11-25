package dkhoa.chemistrylabvr.dto.response.reaction;

import dkhoa.chemistrylabvr.dto.request.reaction.ReactionCondition;

import java.time.Instant;
import java.util.List;

public record ReactionList(
        Long id,
        String equation,
        List<ReactionComponent> reactants,
        List<ReactionComponent> products,
        List<ReactionComponent> catalysts,
        ReactionCondition conditions,
        ReactionVisualEffects visualEffects,
        String description,
        boolean reversible,
        String sourceReference,
        Instant createdAt,
        Instant updatedAt
) {
}
