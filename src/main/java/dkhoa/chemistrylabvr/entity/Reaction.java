package dkhoa.chemistrylabvr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "\"Reaction\"")
public class Reaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "equation", nullable = false, length = Integer.MAX_VALUE)
    private String equation;

    @NotNull
    @Column(name = "reactants_json", nullable = false, length = Integer.MAX_VALUE)
    private String reactantsJson;

    @NotNull
    @Column(name = "products_json", nullable = false, length = Integer.MAX_VALUE)
    private String productsJson;

    @NotNull
    @Column(name = "visual_effects_json", nullable = false, length = Integer.MAX_VALUE)
    private String visualEffectsJson;

    @NotNull
    @Column(name = "description", nullable = false, length = Integer.MAX_VALUE)
    private String description;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;


}