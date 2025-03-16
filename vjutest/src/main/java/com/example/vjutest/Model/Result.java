package com.example.vjutest.Model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;

@Entity
@Table(name = "results")
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "duration", nullable = false)
    private Long duration;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "pass_quit", nullable = false)
    private Status passQuit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by")
    private User modifiedBy;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    public Result() {
    }

    public Result(User user, Exam exam, int score, Long duration, User modifiedBy) {
        this.user = user;
        this.exam = exam;
        this.score = score;
        this.duration = duration;
        this.submittedAt = LocalDateTime.now();
        this.modifiedBy = modifiedBy;
        this.modifiedAt = LocalDateTime.now();
    }

    public enum Status {
        PASS, FAIL
    }
}
