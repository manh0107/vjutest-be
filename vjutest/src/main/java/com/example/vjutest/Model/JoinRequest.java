package com.example.vjutest.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "join_requests")
public class JoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Type type;

    public JoinRequest() {}

    public JoinRequest(User user, ClassEntity classEntity) {
        this.user = user;
        this.classEntity = classEntity;
        this.status = Status.PENDING; 
    }

    public JoinRequest(User user, ClassEntity classEntity, Status status, Type type) {
        this.user = user;
        this.classEntity = classEntity;
        this.status = status;
        this.type = type;
    }

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    public enum Type {
        STUDENT_REQUEST,
        TEACHER_INVITE
    }
}
