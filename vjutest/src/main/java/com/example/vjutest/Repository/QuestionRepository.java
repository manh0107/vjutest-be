package com.example.vjutest.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vjutest.Model.Question;
import com.example.vjutest.Model.Subject;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByIsPublic(Boolean isPublic);
    List<Question> findAllByIsPublicTrueAndSubjectId(Long subjectId);
    List<Question> findAllByExamQuestions_Exam_Id(Long examId);

    int countBySubject(Subject subject);
}
