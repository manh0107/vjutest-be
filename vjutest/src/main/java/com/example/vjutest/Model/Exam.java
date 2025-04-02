package com.example.vjutest.Model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "exam_code", nullable = false, unique = true)
    private String examCode;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "duration_time", nullable = false)
    private Long durationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_subject_id", nullable = true)
    private ClassSubject classSubject;

    @Column(name = "max_score")
    private Integer maxScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "pass_quit", nullable = false, length = 8)
    private String passQuit;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by", nullable = false)
    private User modifiedBy;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExamQuestion> examQuestions;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false) // Bắt buộc phải có môn học
    private Subject subject;

    public Exam() {
    }

    public Exam(String name, String examCode, String description, Long durationTime, ClassSubject classSubject, String passQuit, boolean isPublic, LocalDateTime startAt, LocalDateTime endAt, Status status, User createdBy) {
        this.name = name;
        this.examCode = examCode;
        this.description = description;
        this.durationTime = durationTime;
        this.classSubject = classSubject;
        this.passQuit = passQuit;
        this.isPublic = isPublic;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
        this.createdBy = createdBy;
    }

    public enum Status {
        DRAFT, PUBLISHED, CLOSED
    }

    public int getTotalQuestions() {
        return examQuestions != null ? examQuestions.size() : 0;
    }

    public void updateMaxScore() {
        this.maxScore = examQuestions.stream()
            .mapToInt(ExamQuestion::getPoint)
            .sum();
    }    

    @PrePersist
    public void prePersist() {
        if (!this.examCode.startsWith("E-")) {
            this.examCode = "E-" + this.examCode;
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (!this.examCode.startsWith("E-")) {
            this.examCode = "E-" + this.examCode;
        }
    }

    public boolean isPublic() {
        return isPublic;
    }
    
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
    
}
