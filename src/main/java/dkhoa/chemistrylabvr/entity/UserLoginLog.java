package dkhoa.chemistrylabvr.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "\"UserLoginLogs\"")
public class UserLoginLog {
    @Id
    @Column(name = "id", nullable = false, length = Integer.MAX_VALUE)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "email_attempted", length = Integer.MAX_VALUE)
    private String emailAttempted;

    @Column(name = "status", length = Integer.MAX_VALUE)
    private String status;

    @Column(name = "ip_address", length = Integer.MAX_VALUE)
    private String ipAddress;

    @Column(name = "user_agent", length = Integer.MAX_VALUE)
    private String userAgent;

    @Column(name = "failure_reason", length = Integer.MAX_VALUE)
    private String failureReason;

    @Column(name = "login_at")
    private OffsetDateTime loginAt;


}