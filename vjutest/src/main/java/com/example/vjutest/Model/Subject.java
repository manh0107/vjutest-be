package com.example.vjutest.Model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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

    @OneToMany(mappedBy = "subject", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<Chapter> chapters = new HashSet<>();

    @OneToMany(mappedBy = "subject", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<ClassSubject> classSubjects = new HashSet<>();

    @OneToMany(mappedBy = "subject", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<Exam> exams = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "subject_majors",
        joinColumns = @JoinColumn(name = "subject_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "major_id", referencedColumnName = "id")
    )
    private Set<Major> majors = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "subject_departments",
        joinColumns = @JoinColumn(name = "subject_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "department_id", referencedColumnName = "id")
    )
    private Set<Department> departments = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", referencedColumnName = "id")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by")
    private User modifiedBy;

    @UpdateTimestamp
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Enumerated(EnumType.STRING)
    private VisibilityScope visibility;

    public enum VisibilityScope {
        PUBLIC,         // Cả trường
        DEPARTMENT,     // Theo khoa
        MAJOR           // Theo ngành
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
