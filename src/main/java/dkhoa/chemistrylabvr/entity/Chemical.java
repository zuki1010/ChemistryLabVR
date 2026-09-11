package dkhoa.chemistrylabvr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
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
    private String state;

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
    private OffsetDateTime createdAt;


}