package dkhoa.chemistrylabvr.entity;

import dkhoa.chemistrylabvr.enums.PhaseChange;
import dkhoa.chemistrylabvr.enums.ReactionComponentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "\"ReactionComponent\"")
public class ReactionComponentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reaction_id", nullable = false)
    private Reaction reaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chemical_id", nullable = false)
    private Chemical chemical;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false, length = 20)
    private ReactionComponentType componentType;

    @Column(name = "coefficient", nullable = false)
    private Integer coefficient;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase_change", nullable = false, length = 20)
    private PhaseChange phaseChange = PhaseChange.NONE;
}
