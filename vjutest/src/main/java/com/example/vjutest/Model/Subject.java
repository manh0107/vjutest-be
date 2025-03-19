package com.example.vjutest.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "subject_code", unique = true, nullable = false)
    private String subjectCode;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "credit_hour", nullable = false)
    private Integer creditHour;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClassSubject> classSubjects = new ArrayList<>();

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Question> questions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createdAt;

    public Subject() {
    }

    public Subject(String name, String subjectCode, String description, Integer creditHour, User createdBy) {
        this.name = name;
        this.subjectCode = subjectCode;
        this.description = description;
        this.creditHour = creditHour;
        this.createdBy = createdBy;
    }

    public Long getQuestionTotal() {
        return (long) (questions != null ? questions.size() : 0);
    }    

    @PrePersist
    public void prePersist() {
        if (this.subjectCode == null || !this.subjectCode.startsWith("S-")) {
            this.subjectCode = "S-" + this.subjectCode;
        }
    }
    @PreUpdate
    public void preUpdate() {
        if (this.subjectCode == null || !this.subjectCode.startsWith("S-")) {
            this.subjectCode = "S-" + this.subjectCode;
        }
    }
}
