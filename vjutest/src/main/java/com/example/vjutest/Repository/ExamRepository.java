package com.example.vjutest.Repository;

import com.example.vjutest.Model.Exam;
import com.example.vjutest.Model.Subject;
import com.example.vjutest.Model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByClassSubject_ClassEntity_Id(Long classId); 
    List<Exam> findByClassSubject_ClassEntity_IdAndIsPublicTrue(Long classId);
    List<Exam> findBySubject_IdAndIsPublicTrue(Long subjectId);
    List<Exam> findBySubject_IdAndCreatedBy(Long subjectId, User createdBy);
    List<Exam> findBySubject_Id(Long subjectId);
    List<Exam> findByClassSubject_ClassEntity_IdAndCreatedBy(Long classId, User createBy);
    List<Exam> findByClassSubject_ClassEntity_IdAndClassSubject_Subject_Id(Long classId, Long subjectId);
    List<Exam> findByClassSubject_ClassEntity_IdAndClassSubject_Subject_IdAndCreatedBy(Long classId, Long subjectId, User createdBy);
    List<Exam> findByClassSubject_ClassEntity_IdAndClassSubject_Subject_IdAndIsPublicTrue(Long classId, Long subjectId);
    List<Exam> findByClassSubject_Subject_IdAndIsPublicTrue(Long subjectId);
    List<Exam> findByCreatedBy(User createdBy);
    int countBySubject(Subject subject);
    boolean existsByExamCode(String examCode);

    @Query("SELECT e FROM Exam e WHERE e.status = :status AND e.endAt < :now")
    List<Exam> findByStatusAndEndAtBefore(@Param("status") Exam.Status status, @Param("now") LocalDateTime now);

    List<Exam> findBySubject_IdAndIsPublicTrueAndStatus(Long subjectId, Exam.Status status);
}
