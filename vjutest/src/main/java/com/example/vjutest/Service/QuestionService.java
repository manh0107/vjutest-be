package com.example.vjutest.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.vjutest.DTO.ExamQuestionDTO;
import com.example.vjutest.DTO.QuestionDTO;
import com.example.vjutest.Exception.UnauthorizedAccessException;
import com.example.vjutest.Mapper.QuestionMapper;
import com.example.vjutest.Model.Answer;
import com.example.vjutest.Model.Chapter;
import com.example.vjutest.Model.Exam;
import com.example.vjutest.Model.ExamQuestion;
import com.example.vjutest.Model.Question;
import com.example.vjutest.Model.Subject;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.AnswerRepository;
import com.example.vjutest.Repository.ChapterRepository;
import com.example.vjutest.Repository.ExamQuestionRepository;
import com.example.vjutest.Repository.ExamRepository;
import com.example.vjutest.Repository.QuestionRepository;
import com.example.vjutest.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final UserRepository userRepository;
    private final QuestionMapper questionMapper;
    private final ExamQuestionService examQuestionService;
    private final AnswerRepository answerRepository;
    private final ChapterRepository chapterRepository;
    private final CloudinaryService cloudinaryService;

    // Tạo câu hỏi bên ngoài bài kiểm tra (ngân hàng câu hỏi)
    public QuestionDTO createQuestion(Question questionRequest, Long userId, Long chapterId, MultipartFile imageFile) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chương học không tồn tại!"));
        
        // Kiểm tra quyền tạo bài kiểm tra
        if (!"teacher".equals(user.getRole().getName()) && !"admin".equals(user.getRole().getName())) {
            throw new UnauthorizedAccessException("Bạn không có quyền tạo câu hỏi!");
        }

        // Kiểm tra quyền truy cập dựa trên major
        Subject subject = chapter.getSubject();
        if (subject != null) {
            boolean hasAccess = subject.getMajors().stream()
                    .anyMatch(major -> major.getDepartment().equals(user.getDepartment()));
            if (!hasAccess && !"admin".equals(user.getRole().getName())) {
                throw new UnauthorizedAccessException("Bạn không có quyền tạo câu hỏi cho môn học này!");
            }
        }
        
        // Tạo câu hỏi mới
        Question question = new Question();
        question.setName(questionRequest.getName());
        question.setDifficulty(questionRequest.getDifficulty());
        question.setIsPublic(true); // câu hỏi tạo ngoài bài kiểm tra sẽ là public (ngân hàng câu hỏi)
        question.setCreatedAt(LocalDateTime.now());
        question.setModifiedAt(LocalDateTime.now());
        question.setModifiedBy(user);
        question.setCreatedBy(user);
        question.setChapter(chapter);

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(imageFile, "questions");
            question.setImageUrl(imageUrl);
        }

        return questionMapper.toFullDTO(questionRepository.save(question));
    }

    // Tạo câu hỏi bên trong bài kiểm tra (có thể public hoặc private)
    @Transactional
    public QuestionDTO createQuestionInExam(Question questionRequest, Long examId, Long userId, Long chapterId, MultipartFile imageFile) throws IOException {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài kiểm tra không tồn tại!"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chương học không tồn tại!"));

        // Kiểm tra quyền tạo bài kiểm tra
        if (!exam.getCreatedBy().equals(user) && !"admin".equals(user.getRole().getName())) {
            throw new UnauthorizedAccessException("Bạn không có quyền tạo câu hỏi trong bài kiểm tra!");
        }

        // Kiểm tra quyền truy cập dựa trên major
        Subject subject = exam.getSubject();
        if (subject != null) {
            boolean hasAccess = subject.getMajors().stream()
                    .anyMatch(major -> major.getDepartment().equals(user.getDepartment()));
            if (!hasAccess && !"admin".equals(user.getRole().getName())) {
                throw new UnauthorizedAccessException("Bạn không có quyền tạo câu hỏi cho môn học này!");
            }
        }

        // Tạo câu hỏi mới
        Question question = new Question();
        question.setName(questionRequest.getName());
        question.setDifficulty(questionRequest.getDifficulty());
        question.setCreatedAt(LocalDateTime.now());
        question.setModifiedAt(LocalDateTime.now());
        question.setCreatedBy(user);
        question.setModifiedBy(user);
        
        // Xử lý trạng thái public khác nhau giữa bài kiểm tra trong lớp và ngoài lớp
        if (exam.getClassSubject() != null) {
            // Bài kiểm tra trong lớp: luôn set isPublic = false, lưu trạng thái public vào markedAsPublic
            question.setIsPublic(false);
            question.setMarkedAsPublic(questionRequest.getIsPublic());
        } else {
            // Bài kiểm tra ngoài lớp: luôn public
            question.setIsPublic(true);
            question.setMarkedAsPublic(true);
        }
        
        question.setChapter(chapter);

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(imageFile, "questions");
            question.setImageUrl(imageUrl);
        }
        
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
        exam.setQuestionsCount(exam.getExamQuestions().size());
        examRepository.save(exam);
        
        return questionMapper.toFullDTO(savedQuestion);
    }

    // Lấy danh sách câu hỏi công khai theo môn học
    public List<Question> getPublicQuestionsByExam(Long examId, Long userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài kiểm tra không tồn tại!"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        // Kiểm tra quyền truy cập
        if (!exam.getCreatedBy().equals(user) && !"admin".equals(user.getRole().getName())) {
            throw new UnauthorizedAccessException("Bạn không có quyền xem câu hỏi của bài kiểm tra này!");
        }

        Long subjectId = exam.getSubject().getId();
        return questionRepository.findAllByIsPublicTrueAndChapterSubjectId(subjectId);
    }
    
    // Thêm câu hỏi công khai vào bài kiểm tra
    @Transactional
    public void addQuestionsToExam(Long examId, Long userId, List<Long> questionIds) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài kiểm tra không tồn tại!"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
        
        if(!exam.getCreatedBy().equals(user) && !"admin".equals(user.getRole().getName())) {
            throw new UnauthorizedAccessException("Bạn không có quyền thêm câu hỏi vào bài kiểm tra này!");
        }

        // Kiểm tra quyền truy cập dựa trên major
        Subject subject = exam.getSubject();
        if (subject != null) {
            boolean hasAccess = subject.getMajors().stream()
                    .anyMatch(major -> major.getDepartment().equals(user.getDepartment()));
            if (!hasAccess && !"admin".equals(user.getRole().getName())) {
                throw new UnauthorizedAccessException("Bạn không có quyền thêm câu hỏi cho môn học này!");
            }
        }

        // Chỉ cho phép thêm câu hỏi khi bài kiểm tra đã kết thúc
        if (exam.getStatus() != Exam.Status.CLOSED) {
            throw new UnauthorizedAccessException("Chỉ có thể thêm câu hỏi công khai vào bài kiểm tra sau khi bài kiểm tra đã kết thúc!");
        }
    
        for (Long qId : questionIds) {
            Question question = questionRepository.findById(qId)
                .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại!"));
    
            // Chỉ cho phép gán câu hỏi công khai
            if (Boolean.TRUE.equals(question.getIsPublic())) {
                ExamQuestion eq = new ExamQuestion();
                eq.setExam(exam);
                eq.setQuestion(question);
                eq.setPoint(1); // Điểm mặc định cho câu hỏi public
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

    @Transactional
    public QuestionDTO updateBankQuestion(Long id, QuestionDTO questionRequest, Long userId, MultipartFile imageFile) throws IOException {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!question.getCreatedBy().getId().equals(userId) && !user.getRole().getName().equals("admin")) {
            throw new UnauthorizedAccessException("Bạn không có quyền cập nhật câu hỏi này");
        }

        // Kiểm tra quyền truy cập dựa trên major
        Subject subject = question.getChapter().getSubject();
        if (subject != null) {
            boolean hasAccess = subject.getMajors().stream()
                    .anyMatch(major -> major.getDepartment().equals(user.getDepartment()));
            if (!hasAccess && !"admin".equals(user.getRole().getName())) {
                throw new UnauthorizedAccessException("Bạn không có quyền cập nhật câu hỏi của môn học này!");
            }
        }

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image if exists
            if (question.getImageUrl() != null) {
                cloudinaryService.deleteImage(question.getImageUrl());
            }
            // Upload new image
            String imageUrl = cloudinaryService.uploadImage(imageFile, "questions");
            question.setImageUrl(imageUrl);
        }

        question.setName(questionRequest.getName());
        question.setDifficulty(questionRequest.getDifficulty());
        question.setModifiedAt(LocalDateTime.now());
        question.setModifiedBy(user);

        question = questionRepository.save(question);
        return questionMapper.toFullDTO(question);
    }

    @Transactional
    public QuestionDTO updateQuestionInExam(Long id, QuestionDTO questionRequest, Long userId, Long examId, MultipartFile imageFile) throws IOException {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài kiểm tra"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!exam.getCreatedBy().getId().equals(userId) && !user.getRole().getName().equals("admin")) {
            throw new UnauthorizedAccessException("Bạn không có quyền sửa câu hỏi này trong bài kiểm tra");
        }

        // Kiểm tra quyền truy cập dựa trên major
        Subject subject = exam.getSubject();
        if (subject != null) {
            boolean hasAccess = subject.getMajors().stream()
                    .anyMatch(major -> major.getDepartment().equals(user.getDepartment()));
            if (!hasAccess && !"admin".equals(user.getRole().getName())) {
                throw new UnauthorizedAccessException("Bạn không có quyền cập nhật câu hỏi của môn học này!");
            }
        }

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image if exists
            if (question.getImageUrl() != null) {
                cloudinaryService.deleteImage(question.getImageUrl());
            }
            // Upload new image
            String imageUrl = cloudinaryService.uploadImage(imageFile, "questions");
            question.setImageUrl(imageUrl);
        }

        question.setName(questionRequest.getName());
        question.setDifficulty(questionRequest.getDifficulty());
        question.setModifiedAt(LocalDateTime.now());
        question.setModifiedBy(user);

        // Xử lý trạng thái public khác nhau giữa bài kiểm tra trong lớp và ngoài lớp
        if (exam.getClassSubject() != null) {
            // Bài kiểm tra trong lớp: luôn set isPublic = false, lưu trạng thái public vào markedAsPublic
            question.setIsPublic(false);
            question.setMarkedAsPublic(questionRequest.getIsPublic());
        } else {
            // Bài kiểm tra ngoài lớp: luôn public
            question.setIsPublic(true);
            question.setMarkedAsPublic(true);
        }

        Question savedQuestion = questionRepository.save(question);
        List<ExamQuestion> createdExamQuestions = new ArrayList<>();

        // Lặp qua các câu hỏi trong bài kiểm tra để cập nhật điểm
        if (questionRequest.getExamQuestions() != null && !questionRequest.getExamQuestions().isEmpty()) {
            for (ExamQuestionDTO examQuestionDTO : questionRequest.getExamQuestions()) {
                Integer point = examQuestionDTO.getPoint();
                if (point == null || point <= 0) {
                    throw new RuntimeException("Điểm của câu hỏi không hợp lệ!");
                }

                // Tìm ExamQuestion hiện có
                ExamQuestion existingExamQuestion = examQuestionRepository.findByExamAndQuestion(exam, savedQuestion)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi trong bài kiểm tra"));

                // Cập nhật điểm
                existingExamQuestion.setPoint(point);
                examQuestionRepository.save(existingExamQuestion);
                createdExamQuestions.add(existingExamQuestion);
            }
        }

        savedQuestion.setExamQuestions(createdExamQuestions);

        int maxScore = exam.getExamQuestions().stream()
                .mapToInt(ExamQuestion::getPoint)
                .sum();
        exam.setMaxScore(maxScore);
        exam.setQuestionsCount(exam.getExamQuestions().size());
        examRepository.save(exam);

        return questionMapper.toFullDTO(savedQuestion);
    }

    @Transactional
    public void deleteQuestion(Long id, Long userId) throws IOException {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!question.getCreatedBy().getId().equals(userId) && !user.getRole().getName().equals("admin")) {
            throw new UnauthorizedAccessException("Bạn không có quyền xóa câu hỏi này");
        }

        // Kiểm tra quyền truy cập dựa trên major
        Subject subject = question.getChapter().getSubject();
        if (subject != null) {
            boolean hasAccess = subject.getMajors().stream()
                    .anyMatch(major -> major.getDepartment().equals(user.getDepartment()));
            if (!hasAccess && !"admin".equals(user.getRole().getName())) {
                throw new UnauthorizedAccessException("Bạn không có quyền xóa câu hỏi của môn học này!");
            }
        }

        // Delete image from Cloudinary if exists
        if (question.getImageUrl() != null) {
            cloudinaryService.deleteImage(question.getImageUrl());
        }

        questionRepository.delete(question);
    }

    public List<QuestionDTO> getQuestionsByExam(Long examId, Long userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài kiểm tra"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra quyền truy cập
        if (!exam.getCreatedBy().equals(user) && !"admin".equals(user.getRole().getName())) {
            throw new UnauthorizedAccessException("Bạn không có quyền xem câu hỏi của bài kiểm tra này!");
        }

        List<Question> questions = questionRepository.findByExamId(examId);
        return questions.stream()
                .map(questionMapper::toFullDTO)
                .collect(Collectors.toList());
    }

    public List<Question> getQuestionsByChapter(Long chapterId) {
        return questionRepository.findByChapterId(chapterId);
    }

    public List<Question> getCompletedQuestionsByChapter(Long chapterId) {
        return questionRepository.findByChapterIdAndIsCompletedTrue(chapterId);
    }

    // Tự động thêm câu hỏi public vào chương tương ứng khi bài kiểm tra kết thúc
    @Transactional
    public void autoAddPublicQuestionsToChapters(Exam exam) {
        if (exam.getStatus() != Exam.Status.CLOSED) {
            return;
        }

        for (ExamQuestion examQuestion : exam.getExamQuestions()) {
            Question question = examQuestion.getQuestion();
            if (question.getMarkedAsPublic()) {
                question.setIsPublic(true);
                questionRepository.save(question);
            }
        }
    }

    @Transactional
    public QuestionDTO duplicateQuestion(Long examId, Long questionId, Long userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài kiểm tra"));

        Question originalQuestion = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra quyền
        if (!exam.getCreatedBy().getId().equals(userId) && !user.getRole().getName().equals("admin")) {
            throw new UnauthorizedAccessException("Bạn không có quyền sao chép câu hỏi trong bài kiểm tra này");
        }

        // Tạo câu hỏi mới từ câu hỏi gốc
        Question newQuestion = new Question();
        newQuestion.setName(originalQuestion.getName() + " (Bản sao)");
        newQuestion.setDifficulty(originalQuestion.getDifficulty());
        newQuestion.setIsPublic(originalQuestion.getIsPublic());
        newQuestion.setMarkedAsPublic(originalQuestion.getMarkedAsPublic());
        newQuestion.setCreatedAt(LocalDateTime.now());
        newQuestion.setModifiedAt(LocalDateTime.now());
        newQuestion.setCreatedBy(user);
        newQuestion.setModifiedBy(user);
        newQuestion.setChapter(originalQuestion.getChapter());
        newQuestion.setImageUrl(originalQuestion.getImageUrl());

        // Lưu câu hỏi mới
        Question savedQuestion = questionRepository.save(newQuestion);

        // Sao chép các đáp án
        List<Answer> originalAnswers = answerRepository.findByQuestionId(questionId);
        for (Answer originalAnswer : originalAnswers) {
            Answer newAnswer = new Answer();
            newAnswer.setAnswerName(originalAnswer.getAnswerName());
            newAnswer.setIsCorrect(originalAnswer.getIsCorrect());
            newAnswer.setQuestion(savedQuestion);
            newAnswer.setCreatedBy(user);
            newAnswer.setModifiedBy(user);
            newAnswer.setImageUrl(originalAnswer.getImageUrl());
            answerRepository.save(newAnswer);
        }

        // Kiểm tra và set trạng thái hoàn thành cho câu hỏi mới
        savedQuestion.setIsCompleted(checkIfQuestionIsCompleted(savedQuestion));
        savedQuestion = questionRepository.save(savedQuestion);

        // Tạo ExamQuestion mới
        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setExam(exam);
        examQuestion.setQuestion(savedQuestion);
        examQuestion.setPoint(1); // Điểm mặc định
        examQuestionRepository.save(examQuestion);

        // Cập nhật maxScore của exam
        exam.updateMaxScore();
        exam.setQuestionsCount(exam.getExamQuestions().size());
        examRepository.save(exam);

        return questionMapper.toFullDTO(savedQuestion);
    }
}

