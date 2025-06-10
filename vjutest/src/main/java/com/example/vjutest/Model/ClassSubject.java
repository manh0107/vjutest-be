package com.example.vjutest.Model;

import java.util.List;

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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "class_subjects")
public class ClassSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private ClassEntity classEntity;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "file_name")
    private String fileName;

    @OneToMany(mappedBy = "classSubject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Exam> exams;

    @Column(name = "folder_id")
    private String googleDriveFolderId;

    public ClassSubject() {
    }

    public ClassSubject(ClassEntity classEntity, Subject subject, String documentUrl) {
        this.classEntity = classEntity;
        this.subject = subject;
        this.documentUrl = documentUrl;
    }
}
