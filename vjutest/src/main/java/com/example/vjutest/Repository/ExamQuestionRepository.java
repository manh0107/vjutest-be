package com.example.vjutest.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vjutest.Model.ExamQuestion;
import com.example.vjutest.Model.Exam;
import com.example.vjutest.Model.Question;
import java.util.Optional;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    Optional<ExamQuestion> findByExamAndQuestion(Exam exam, Question question);
}
