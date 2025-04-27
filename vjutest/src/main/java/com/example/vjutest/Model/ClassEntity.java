package com.example.vjutest.Model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name="classes", uniqueConstraints = @UniqueConstraint(columnNames = {"id", "created_by_id"}))
public class ClassEntity {
    
    public enum VisibilityScope {
        PUBLIC,
        DEPARTMENT,
        MAJOR
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "class_code", unique = true, nullable = false, length = 50)
    private String classCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private VisibilityScope visibility = VisibilityScope.MAJOR;

    @CreationTimestamp 
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", referencedColumnName = "id")
    private User createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "class_majors",
        joinColumns = @JoinColumn(name = "class_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "major_id", referencedColumnName = "id")
    )
    private Set<Major> majors = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "class_departments",
        joinColumns = @JoinColumn(name = "class_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "department_id", referencedColumnName = "id")
    )
    private Set<Department> departments = new HashSet<>();


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "class_user",
        joinColumns = @JoinColumn(name = "class_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id")
    )
    private Set<User> users = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "class_teacher",
        joinColumns = @JoinColumn(name = "class_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "teacher_id", referencedColumnName = "id")
    )
    private Set<User> teachers = new HashSet<>();

    @OneToMany(mappedBy = "classEntity", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<JoinRequest> joinRequests = new HashSet<>();

    @OneToMany(mappedBy = "classEntity", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<ClassSubject> classSubjects = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (this.classCode != null && !this.classCode.startsWith("C-")) {
            this.classCode = "C-" + this.classCode;
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (this.classCode != null && !this.classCode.startsWith("C-")) {
            this.classCode = "C-" + this.classCode;
        }
    }

    public boolean isTeacherOfClass(User user) {
        return this.getTeachers().contains(user) || this.getCreatedBy().equals(user);
    }
}
