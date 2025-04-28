package com.example.vjutest.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.vjutest.Model.Chapter;
import com.example.vjutest.Model.Question;
import com.example.vjutest.Model.Subject;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByIsPublic(Boolean isPublic);
    
    @Query("SELECT q FROM Question q WHERE q.isPublic = true AND q.chapter.subject.id = :subjectId")
    List<Question> findAllByIsPublicTrueAndChapterSubjectId(@Param("subjectId") Long subjectId);
    
    List<Question> findAllByExamQuestions_Exam_Id(Long examId);

    @Query("SELECT DISTINCT q FROM Question q " +
           "WHERE q.isPublic = true " +
           "AND EXISTS (SELECT 1 FROM q.chapter.subject.majors m WHERE m.department.id = :departmentId)")
    List<Question> findAllByIsPublicTrueAndChapterSubjectMajorsDepartmentId(@Param("departmentId") Long departmentId);

    @Query("SELECT q FROM Question q JOIN q.examQuestions eq WHERE eq.exam.id = :examId")
    List<Question> findByExamId(@Param("examId") Long examId);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.chapter.subject = :subject")
    int countByChapter_Subject(@Param("subject") Subject subject);

    List<Question> findAllByIsPublicTrueAndChapter_Subject(Subject subject);
    List<Question> findByChapter_Subject(Subject subject);
    List<Question> findByCreatedBy_Id(Long userId);
    List<Question> findByIsPublicTrue();
    int countByChapter(Chapter chapter);
}
