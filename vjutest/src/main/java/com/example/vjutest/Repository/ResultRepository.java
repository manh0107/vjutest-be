package com.example.vjutest.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.example.vjutest.Model.Exam;
import com.example.vjutest.Model.Result;
import com.example.vjutest.Model.User;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long>{
    boolean existsByUserIdAndExamId(Long userId, Long examId);
    Optional<Result> findByUserIdAndExamId(Long userId, Long examId);
    Optional<Result> findByExamAndUser(Exam exam, User user);
    List<Result> findByUserIdAndIsSubmittedTrue(Long userId);
    long countByExamAndUser(Exam exam, User user);
    Optional<Result> findTopByExamAndUserOrderByStartTimeDesc(Exam exam, User user);
    Optional<Result> findTopByExamAndUserOrderByIdDesc(Exam exam, User user);
    Optional<Result> findTopByUserIdAndExamIdOrderByStartTimeDesc(Long userId, Long examId);
    Optional<Result> findTopByUserIdAndExamIdAndIsSubmittedTrueOrderBySubmitTimeDesc(Long userId, Long examId);
    List<Result> findTop10ByOrderBySubmitTimeDesc();
}