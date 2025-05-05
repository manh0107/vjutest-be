package com.example.vjutest.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vjutest.Model.ClassEntity;
import com.example.vjutest.Model.ClassSubject;
import com.example.vjutest.Model.Subject;

@Repository
public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {
    List<ClassSubject> findByClassEntity_Id(Long classId);
    Optional<ClassSubject> findByClassEntity_IdAndSubject_Id(Long classId, Long SubjectId);
    ClassSubject findByClassEntityAndSubject(ClassEntity classEntity, Subject subject);

    int countBySubject(Subject subject);
    boolean existsByClassEntityAndSubject(ClassEntity classEntity, Subject subject);
    List<ClassSubject> findByClassEntity(ClassEntity classEntity);
    List<ClassSubject> findBySubject(Subject subject);
}
