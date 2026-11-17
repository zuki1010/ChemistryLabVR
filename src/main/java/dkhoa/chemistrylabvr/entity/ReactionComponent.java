package dkhoa.chemistrylabvr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "\"ReactionComponent\"")
public class ReactionComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "reaction_id", nullable = false)
    private Reaction reaction;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "chemical_id", nullable = false)
    private Chemical chemical;

    @Size(max = 20)
    @NotNull
    @Column(name = "component_type", nullable = false, length = 20)
    private String componentType;

    @NotNull
    @Column(name = "coefficient", nullable = false)
    private Integer coefficient;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'NONE'")
    @Column(name = "phase_change", nullable = false, length = 20)
    private String phaseChange;


}