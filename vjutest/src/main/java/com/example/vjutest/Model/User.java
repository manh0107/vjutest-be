package com.example.vjutest.Model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="users")
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", unique = true, nullable = false)
    private Long code;

    @Column(name = "phone_number", unique = true, nullable = false, length = 15)
    private Long phoneNumber;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "gender", length = 50)
    private String gender;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "image", nullable = false)
    private String image = "https://static.vecteezy.com/system/resources/thumbnails/020/765/399/small/default-profile-account-unknown-icon-black-silhouette-free-vector.jpg";

    @Column(nullable = false)
    private boolean enabled = false; // Mặc định chưa kích hoạt

    @Column(unique = true)
    private String verificationToken; // Lưu mã xác nhận email

    @CreationTimestamp 
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClassEntity> createClasses;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Subject> createSubjects;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Exam> createdExams;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Question> createdQuestions;

    @ManyToMany(mappedBy = "users")
    private Set<ClassEntity> classes;

    @ManyToMany(mappedBy = "teachers")
    private Set<ClassEntity> teacherOfClasses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<JoinRequest> joinRequests;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAnswer> userAnswers;

    public User(String name, Long code, Long phoneNumber, String className, String gender, String email, String password, Role role, String image) {
        this.name = name;
        this.code = code;
        this.phoneNumber = phoneNumber;
        this.className = className;
        this.gender = gender;
        this.email = email;
        this.password = password;
        this.role = role;
        this.image = (image != null && !image.isEmpty()) ? image : "default.jpg";  
    }
}

