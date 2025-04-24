package com.example.vjutest.Model;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
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
    @JoinColumn(name = "class_subject_id", referencedColumnName = "id")
    private ClassSubject classSubject;

    @Column(name = "max_score")
    private Integer maxScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private ExamVisibility visibility;

    @Column(name = "pass_score", nullable = false)
    private Integer passScore;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", referencedColumnName = "id")
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

    @OneToMany(mappedBy = "exam", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<Result> results = new HashSet<>();

    @OneToMany(mappedBy = "exam", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<ExamQuestion> examQuestions = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", referencedColumnName = "id")
    private Subject subject;

   
    public enum Status {
        DRAFT, PUBLISHED, CLOSED
    }

    public enum ExamVisibility {
        PUBLIC,        // Tất cả sinh viên đều thấy
        DEPARTMENT,    // Chỉ sinh viên trong khoa thấy
        MAJOR,         // Chỉ sinh viên trong ngành thấy
        CLASS          // Chỉ sinh viên trong lớp cụ thể thấy
    }
    

    public int getTotalQuestions() {
        if (examQuestions == null) return 0;
        return (int) examQuestions.stream()
                .map(ExamQuestion::getQuestion)
                .filter(Question::getIsCompleted)
                .count();
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
}
