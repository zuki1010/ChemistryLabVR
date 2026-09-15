package dkhoa.chemistrylabvr.entity;

import dkhoa.chemistrylabvr.enums.ChemicalState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"Chemical\"")
public class Chemical {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false, length = Integer.MAX_VALUE)
    private String name;

    @NotNull
    @Column(name = "formula", nullable = false, length = Integer.MAX_VALUE)
    private String formula;

    @NotNull
    @Column(name = "state", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    private ChemicalState state;

    @NotNull
    @Column(name = "color_hex", nullable = false, length = Integer.MAX_VALUE)
    private String colorHex;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @NotNull
    @Column(name = "unity_asset_id", nullable = false, length = Integer.MAX_VALUE)
    private String unityAssetId;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "density", precision = 10, scale = 3)
    private BigDecimal density;
    @Column(name = "danger_level")
    private Integer dangerLevel;
    @Column(name = "unit", length = Integer.MAX_VALUE)
    private String unit;


}