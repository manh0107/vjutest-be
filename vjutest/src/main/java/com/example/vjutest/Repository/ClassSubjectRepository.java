package com.example.vjutest.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vjutest.Model.ClassSubject;

@Repository
public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {
    List<ClassSubject> findByClassEntity_Id(Long classId);
    Optional<ClassSubject> findByClassEntity_IdAndSubject_Id(Long classId, Long SubjectId);
}
