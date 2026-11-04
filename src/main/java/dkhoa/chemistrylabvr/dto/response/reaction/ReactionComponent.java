package dkhoa.chemistrylabvr.dto.response.reaction;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReactionComponent(
        @JsonProperty("chemical_id") Long chemicalId,
        String formula,
        int coefficient
) {
}
