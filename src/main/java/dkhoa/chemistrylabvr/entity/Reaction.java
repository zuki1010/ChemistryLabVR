package dkhoa.chemistrylabvr.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "\"Reaction\"")
public class Reaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "equation", nullable = false, columnDefinition = "text")
    private String equation;

    @Column(name = "reactants_json", nullable = false, columnDefinition = "text")
    private String reactantsJson;

    @Column(name = "products_json", nullable = false, columnDefinition = "text")
    private String productsJson;

    @Column(name = "visual_effects_json", nullable = false, columnDefinition = "text")
    private String visualEffectsJson;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conditions_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> conditionsJson = new LinkedHashMap<>();

    @Column(name = "is_reversible", nullable = false)
    private Boolean isReversible = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "source_reference", columnDefinition = "text")
    private String sourceReference;

    @OneToMany(mappedBy = "reaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("componentType ASC, displayOrder ASC")
    private List<ReactionComponentEntity> components = new ArrayList<>();
}
