package com.example.vjutest.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.vjutest.DTO.QuestionDTO;
import com.example.vjutest.Mapper.QuestionMapper;
import com.example.vjutest.Model.Answer;
import com.example.vjutest.Model.Exam;
import com.example.vjutest.Model.ExamQuestion;
import com.example.vjutest.Model.Question;
import com.example.vjutest.Model.Subject;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.AnswerRepository;
import com.example.vjutest.Repository.ExamQuestionRepository;
import com.example.vjutest.Repository.ExamRepository;
import com.example.vjutest.Repository.QuestionRepository;
import com.example.vjutest.Repository.SubjectRepository;
import com.example.vjutest.Repository.UserRepository;

import jakarta.transaction.Transactional;


@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final QuestionMapper questionMapper;
    private final ExamQuestionService examQuestionService;
    private final AnswerRepository answerRepository;

    @Autowired
    public QuestionService(QuestionRepository questionRepository, 
                           ExamRepository examRepository, 
                           ExamQuestionRepository examQuestionRepository,
                           SubjectRepository subjectRepository,
                           UserRepository userRepository, 
                           QuestionMapper questionMapper,
                           ExamQuestionService examQuestionService,
                           AnswerRepository answerRepository) {
        this.questionRepository = questionRepository;
        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.questionMapper = questionMapper;
        this.examQuestionService = examQuestionService;
        this.answerRepository = answerRepository;
    }

    // Tạo câu hỏi bên ngoài bài kiểm tra (ngân hàng câu hỏi)
    public QuestionDTO createQuestion(Question questionRequest, Long userId, Long subjectId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Môn học không tồn tại!"));
        
        // Kiểm tra quyền tạo bài kiểm tra
        if (!"teacher".equals(user.getRole().getName()) && !"admin".equals(user.getRole().getName())) {
            throw new RuntimeException("Bạn không có quyền tạo câu hỏi!");
        }
        
        // Tạo câu hỏi mới
        Question question = new Question();
        question.setName(questionRequest.getName());
        question.setDifficulty(questionRequest.getDifficulty());
        question.setIsPublic(true);  // Mặc định câu hỏi là public
        question.setCreatedAt(LocalDateTime.now());
        question.setModifiedAt(LocalDateTime.now());
        question.setModifiedBy(user);
        question.setCreatedBy(user);
        question.setSubject(subject);

        return questionMapper.toFullDTO(questionRepository.save(question)); // Lưu câu hỏi vào ngân hàng câu hỏi
    }

    // Tạo câu hỏi bên trong bài kiểm tra (có thể public hoặc private)
    @Transactional
    public QuestionDTO createQuestionInExam(Question questionRequest, Long examId, Long userId, Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Môn học không tồn tại!"));
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài kiểm tra không tồn tại!"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        // Kiểm tra quyền tạo bài kiểm tra
        if (!exam.getCreatedBy().equals(user) && !"admin".equals(user.getRole().getName())) {
            throw new RuntimeException("Bạn không có quyền tạo câu hỏi trong bài kiểm tra!");
        }

        // Tạo câu hỏi mới
        Question question = new Question();
        question.setName(questionRequest.getName());
        question.setDifficulty(questionRequest.getDifficulty());
        question.setCreatedAt(LocalDateTime.now());
        question.setModifiedAt(LocalDateTime.now());
        question.setCreatedBy(user);
        question.setModifiedBy(user);
        
        if(Boolean.TRUE.equals(exam.getIsPublic())) {
            question.setIsPublic(true); 
        } else {
            question.setIsPublic(questionRequest.getIsPublic());
        }

        question.setSubject(subject);
        
        Question savedQuestion = questionRepository.save(question);
        List<ExamQuestion> createdExamQuestions = new ArrayList<>();

        // Lặp qua các câu hỏi trong bài kiểm tra để tạo ExamQuestion
        if (questionRequest.getExamQuestions() != null && !questionRequest.getExamQuestions().isEmpty()) {
            for (ExamQuestion examQuestion : questionRequest.getExamQuestions()) {
                // Kiểm tra điểm, nếu không hợp lệ thì throw lỗi
                Integer point = examQuestion.getPoint();
                if (point == null || point <= 0) {
                    throw new RuntimeException("Điểm của câu hỏi không hợp lệ!");
                }

                createdExamQuestions.add(examQuestionService.createExamQuestion(exam, savedQuestion, point));
            }
        }

        savedQuestion.setExamQuestions(createdExamQuestions);

        exam.updateMaxScore();
        examRepository.save(exam);
        
        return questionMapper.toFullDTO(savedQuestion);
    }

    // Lấy danh sách câu hỏi từ ngân hàng câu hỏi
    public List<Question> getQuestionsFromBank() {
        return questionRepository.findAllByIsPublic(true); // Giả sử chỉ lấy các câu hỏi công khai
    }

    // Lấy danh sách câu hỏi công khai theo môn học
    public List<Question> getPublicQuestionsByExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài kiểm tra không tồn tại!"));
        
        Long subjectId = exam.getSubject().getId();
        return questionRepository.findAllByIsPublicTrueAndSubjectId(subjectId);
    }
    
    // Thêm câu hỏi công khai vào bài kiểm tra
    @Transactional
    public void addQuestionsToExam(Long examId, Long userId, List<Long> questionIds) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài kiểm tra không tồn tại!"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
        
        if(!exam.getCreatedBy().equals(user) && !"admin".equals(user.getRole().getName())) {
            throw new RuntimeException("Bạn không có quyền thêm câu hỏi vào bài kiểm tra này!");
        }
    
        for (Long qId : questionIds) {
            Question question = questionRepository.findById(qId)
                .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại!"));
    
            // Chỉ cho phép gán câu hỏi công khai
            if (Boolean.TRUE.equals(question.getIsPublic())) {
                ExamQuestion eq = new ExamQuestion();
                eq.setExam(exam);
                eq.setQuestion(question);
                eq.setPoint(eq.getPoint());
                examQuestionRepository.save(eq);
            }
        }
    }

    public boolean checkIfQuestionIsCompleted(Question question) {
        List<Answer> answers = answerRepository.findByQuestionId(question.getId());
        if (answers.size() < 2) {
            return false;
        }

        long correctAnswersCount = answers.stream()
                .filter(Answer::getIsCorrect)
                .count();

        return correctAnswersCount == 1;
    }
}

