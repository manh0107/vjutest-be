package com.example.vjutest.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vjutest.Model.Exam;
import com.example.vjutest.Model.Question;
import com.example.vjutest.Model.Result;
import com.example.vjutest.Model.User;
import com.example.vjutest.Model.UserAnswer;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
   List<UserAnswer> findByExamAndUser(Exam exam, User user);
   Optional<UserAnswer> findByExamAndUserAndQuestion(Exam exam, User user, Question question);
   UserAnswer findByResultAndQuestion(Result result, Question question);
   List<UserAnswer> findByResult(Result result);
   List<UserAnswer> findByResultId(Long resultId);
}
