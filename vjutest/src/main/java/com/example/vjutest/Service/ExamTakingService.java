package com.example.vjutest.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.vjutest.Model.Answer;
import com.example.vjutest.Model.Exam;
import com.example.vjutest.Model.ExamQuestion;
import com.example.vjutest.Model.Question;
import com.example.vjutest.Model.Result;
import com.example.vjutest.Model.User;
import com.example.vjutest.Model.UserAnswer;
import com.example.vjutest.Repository.ExamRepository;
import com.example.vjutest.Repository.ResultRepository;
import com.example.vjutest.Repository.UserAnswerRepository;
import com.example.vjutest.Repository.QuestionRepository;
import com.example.vjutest.Repository.AnswerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExamTakingService {
    
    private final ResultRepository resultRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public boolean canStartExam(Exam exam, User student) {
        if (!exam.canStartExam(student)) {
            return false;
        }

        long attempts = resultRepository.countByExamAndUser(exam, student);
        return attempts < exam.getMaxAttempts();
    }

    public Duration getRemainingTime(Exam exam, User student) {
        Result result = resultRepository.findByExamAndUser(exam, student)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy kết quả"));
        
        LocalDateTime startTime = result.getStartTime();
        LocalDateTime now = LocalDateTime.now();
        Duration elapsed = Duration.between(startTime, now);
        Duration totalTime = Duration.ofMinutes(exam.getDurationTime());
        
        return totalTime.minus(elapsed);
    }

    public List<Question> getExamQuestions(Exam exam) {
        List<Question> questions = exam.getExamQuestions().stream()
            .map(examQuestion -> examQuestion.getQuestion())
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(exam.getRandomQuestions())) {
            Collections.shuffle(questions);
            if (exam.getQuestionsCount() != null) {
                questions = questions.subList(0, Math.min(exam.getQuestionsCount(), questions.size()));
            }
        }

        return questions;
    }

    @Transactional
    public void submitAnswer(Long examId, Long questionId, Long answerId, User student) {
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài kiểm tra"));
        
        Result result = resultRepository.findByExamAndUser(exam, student)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy kết quả"));

        if (result.getIsSubmitted()) {
            throw new RuntimeException("Bài thi đã được nộp!");
        }

        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đáp án"));

        Optional<UserAnswer> existingUserAnswer = Optional.ofNullable(
            userAnswerRepository.findByResultAndQuestion(result, question)
        );

        UserAnswer userAnswer = existingUserAnswer.orElseGet(() -> {
            UserAnswer newUserAnswer = new UserAnswer();
            newUserAnswer.setResult(result);
            newUserAnswer.setQuestion(question);
            newUserAnswer.setUser(student);
            newUserAnswer.setExam(exam);
            return newUserAnswer;
        });

        userAnswer.setAnswer(answer);
        userAnswer.setIsSubmitted(false);

        userAnswerRepository.save(userAnswer);
    }

    @Transactional
    public void markQuestion(Long examId, Long questionId, User student) {
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài kiểm tra"));
        
        Result result = resultRepository.findByExamAndUser(exam, student)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy kết quả"));

        if (result.getIsSubmitted()) {
            throw new RuntimeException("Bài thi đã được nộp!");
        }

        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        Optional<UserAnswer> existingUserAnswer = Optional.ofNullable(
            userAnswerRepository.findByResultAndQuestion(result, question)
        );

        UserAnswer userAnswer = existingUserAnswer.orElseGet(() -> {
            UserAnswer newUserAnswer = new UserAnswer();
            newUserAnswer.setResult(result);
            newUserAnswer.setQuestion(question);
            newUserAnswer.setUser(student);
            newUserAnswer.setExam(exam);
            return newUserAnswer;
        });

        userAnswer.setIsMarked(!userAnswer.getIsMarked());
        userAnswerRepository.save(userAnswer);
    }

    @Transactional
    public Result submitExam(Long examId, User student) {
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài kiểm tra"));
        
        Result result = resultRepository.findByExamAndUser(exam, student)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy kết quả"));

        if (result.getIsSubmitted()) {
            throw new RuntimeException("Bài thi đã được nộp!");
        }

        // Tính điểm
        List<UserAnswer> userAnswers = userAnswerRepository.findByResult(result);
        int totalScore = 0;
        int maxScore = exam.getMaxScore();

        for (UserAnswer userAnswer : userAnswers) {
            if (userAnswer.getAnswer() != null && userAnswer.getAnswer().getIsCorrect()) {
                ExamQuestion examQuestion = exam.getExamQuestions().stream()
                    .filter(eq -> eq.getQuestion().equals(userAnswer.getQuestion()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi trong bài thi"));
                
                totalScore += examQuestion.getPoint();
            }
        }

        // Kiểm tra điểm số có hợp lệ không
        if (totalScore > maxScore) {
            throw new RuntimeException("Điểm số không hợp lệ! Tổng điểm không được vượt quá " + maxScore);
        }

        result.setScore(totalScore);
        result.setIsPassed(totalScore >= exam.getPassScore());
        result.setIsSubmitted(true);
        result.setSubmitTime(LocalDateTime.now());

        return resultRepository.save(result);
    }
} 