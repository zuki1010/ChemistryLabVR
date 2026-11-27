package dkhoa.chemistrylabvr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "\"CourseUnit\"")
public class CourseUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "parent_id")
    private CourseUnit parent;

    @Size(max = 180)
    @NotNull
    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Size(max = 500)
    @NotNull
    @ColumnDefault("''")
    @Column(name = "summary", nullable = false, length = 500)
    private String summary;

    @NotNull
    @ColumnDefault("''")
    @Column(name = "content", nullable = false, length = Integer.MAX_VALUE)
    private String content;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "semester")
    private Short semester;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'DRAFT'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;


}
