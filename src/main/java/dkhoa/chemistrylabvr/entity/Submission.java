package dkhoa.chemistrylabvr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "\"Submission\"")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @NotNull
    @Column(name = "score", nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @NotNull
    @Column(name = "execution_time_seconds", nullable = false)
    private Integer executionTimeSeconds;

    @NotNull
    @Column(name = "action_logs_json", nullable = false, length = Integer.MAX_VALUE)
    private String actionLogsJson;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;


}