package com.example.vjutest.Service;

import com.example.vjutest.DTO.ExamDTO;
import com.example.vjutest.DTO.ResultDTO;
import com.example.vjutest.DTO.UserAnswerDTO;
import com.example.vjutest.Mapper.ExamMapper;
import com.example.vjutest.Mapper.ResultMapper;
import com.example.vjutest.Mapper.UserAnswerMapper;
import com.example.vjutest.Model.*;
import com.example.vjutest.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Collections;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final ClassEntityRepository classEntityRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ExamMapper examMapper;
    private final ResultRepository resultRepository;
    private final ResultMapper resultMapper;
    private final UserAnswerRepository userAnswerRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserAnswerMapper userAnswerMapper;

    @Autowired
    public ExamService(ExamRepository examRepository,
                       ClassEntityRepository classEntityRepository,
                       ClassSubjectRepository classSubjectRepository,
                       SubjectRepository subjectRepository,
                       UserRepository userRepository,
                       ExamMapper examMapper,
                       ResultRepository resultRepository,
                       ResultMapper resultMapper,
                       UserAnswerRepository userAnswerRepository,
                       QuestionRepository questionRepository,
                       AnswerRepository answerRepository,
                       UserAnswerMapper userAnswerMapper) {
        this.examRepository = examRepository;
        this.classEntityRepository = classEntityRepository;
        this.classSubjectRepository = classSubjectRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.examMapper = examMapper;
        this.resultRepository = resultRepository;
        this.resultMapper = resultMapper;
        this.userAnswerRepository = userAnswerRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.userAnswerMapper = userAnswerMapper;
    }

    //Tạo bài kiểm tra trong lớp học
    @Transactional
    public ExamDTO createExam(Long classId, Long subjectId, Long userId, Exam examRequest) {
        // Kiểm tra lớp học và môn học có tồn tại không
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Lớp học không tồn tại!"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Môn học không tồn tại!"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        // Kiểm tra quyền tạo bài kiểm tra
        if (!classEntity.getTeachers().contains(user) && !"admin".equals(user.getRole().getName())) {
            throw new RuntimeException("Bạn không có quyền tạo bài kiểm tra!");
        }

        // Kiểm tra xem môn học đã có trong lớp chưa, nếu chưa thì thêm vào
        ClassSubject classSubject = classSubjectRepository.findByClassEntity_IdAndSubject_Id(classId, subjectId)
                .orElseGet(() -> {
                    ClassSubject newClassSubject = new ClassSubject();
                    newClassSubject.setClassEntity(classEntity);
                    newClassSubject.setSubject(subject);
                    return classSubjectRepository.save(newClassSubject);
                });

        // Tạo bài kiểm tra
        Exam exam = new Exam();
        exam.setName(examRequest.getName());
        exam.setExamCode(examRequest.getExamCode() != null ? examRequest.getExamCode() : "E-" + subjectId + "-" + System.currentTimeMillis());
        exam.setDescription(examRequest.getDescription());
        
        exam.setDurationTime(examRequest.getDurationTime());
        if (examRequest.getDurationTime() < 0) {
            throw new RuntimeException("Thời gian làm bài không hợp lệ!");
        }

        exam.setPassScore(examRequest.getPassScore());
        exam.setIsPublic(examRequest.getIsPublic());

        LocalDateTime now = LocalDateTime.now();

        // Mặc định tạo bài kiểm tra ở trạng thái DRAFT
        exam.setStatus(Exam.Status.DRAFT);
        exam.setStartAt(null);
        exam.setEndAt(null);

        // Thông tin người tạo
        exam.setCreatedBy(user);
        exam.setModifiedBy(user);
        exam.setClassSubject(classSubject);
        exam.setCreatedAt(now);
        exam.setModifiedAt(now);

        exam.setSubject(classSubject.getSubject());

        return examMapper.toFullDTO(examRepository.save(exam));
    }

    
   // Lấy danh sách bài kiểm tra trong lớp theo môn học
    public List<ExamDTO> getExamsInClass(Long classId, Long subjectId, Long userId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Lớp học không tồn tại!"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        List<Exam> exams;

        if ("admin".equals(user.getRole().getName())) {
            exams = examRepository.findByClassSubject_ClassEntity_IdAndClassSubject_Subject_Id(classId, subjectId);
        } else if ("teacher".equals(user.getRole().getName())) {
            exams = examRepository.findByClassSubject_ClassEntity_IdAndClassSubject_Subject_Id(classId, subjectId);
        } else if ("student".equals(user.getRole().getName())) {
            boolean isStudentInClass = classEntity.getUsers().contains(user);
            if (isStudentInClass) {
                exams = examRepository.findByClassSubject_ClassEntity_IdAndClassSubject_Subject_Id(classId, subjectId);
            } else {
                exams = Collections.emptyList();
            }
        } else {
            exams = Collections.emptyList();
        }

        return exams.stream().map(examMapper::toSimpleDTO).collect(Collectors.toList());
    }

    
    //Lấy chi tiết bài kiểm tra theo id
    public Optional<ExamDTO> getExamById(Long examId, Long userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài kiểm tra không tồn tại!"));
    
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
    
        // Nếu user là admin, họ có thể xem tất cả bài kiểm tra
        if ("admin".equals(user.getRole().getName())) {
            return Optional.of(examMapper.toFullDTO(exam));
        }
        // Nếu user là teacher, họ chỉ có thể xem bài kiểm tra do họ tạo
        if ("teacher".equals(user.getRole().getName())) {
            if (!exam.getCreatedBy().equals(user)) {
                return Optional.empty();
            }
            return Optional.of(examMapper.toFullDTO(exam));
        }
        // Nếu user là student 
        if ("student".equals(user.getRole().getName())) {
            // Nếu bài kiểm tra không thuộc lớp nào (bài kiểm tra tự do)
            if (exam.getClassSubject() == null) {
                return exam.getIsPublic() ? Optional.of(examMapper.toFullDTO(exam)) : Optional.empty();
            }
    
            ClassEntity classEntity = exam.getClassSubject().getClassEntity();
            boolean isInClass = classEntity.getUsers().contains(user) || classEntity.getTeachers().contains(user);
    
            if (!isInClass && !exam.getIsPublic()) {
                return Optional.empty();
            }
            return Optional.of(examMapper.toFullDTO(exam));
        }

        return Optional.empty();
    }

    // Hoàn thành bài kiểm tra
    @Transactional
    public ExamDTO updateExamStatus(Long examId, Exam.Status newStatus, LocalDateTime startAt, LocalDateTime endAt, Long userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài kiểm tra không tồn tại!"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        // Kiểm tra quyền cập nhật bài kiểm tra
        if (!exam.getCreatedBy().equals(user) && !"admin".equals(user.getRole().getName())) {
            throw new RuntimeException("Bạn không có quyền cập nhật bài kiểm tra này!");
        }

        if(exam.getTotalQuestions() < 10) {
            throw new RuntimeException("Bài kiểm tra phải có ít nhất 10 câu hỏi!");
        }

        if (newStatus == Exam.Status.PUBLISHED) {
            boolean hasIncomplete = exam.getExamQuestions().stream()
            .map(ExamQuestion::getQuestion)
            .anyMatch(q -> !Boolean.TRUE.equals(q.getIsCompleted()));

            if (hasIncomplete) {
                throw new RuntimeException("Không thể hoàn thành: Có câu hỏi chưa hoàn thành (chưa có đáp án đầy đủ)!");
            }
            
            if(Boolean.FALSE.equals(exam.getIsPublic())) {
                if (startAt == null || startAt.isBefore(exam.getCreatedAt().plusDays(1))) {
                    throw new RuntimeException("Thời gian bắt đầu phải cách thời gian tạo ít nhất 1 ngày!");
                }                     
                exam.setStartAt(startAt);
                exam.setEndAt(startAt.plusMinutes(exam.getDurationTime()));
            } else {
                exam.setStartAt(LocalDateTime.MIN);
                exam.setEndAt(LocalDateTime.MAX);
            } 
        } 

        exam.setStatus(newStatus);
        exam.setModifiedAt(LocalDateTime.now());

        return examMapper.toFullDTO(examRepository.save(exam));
    }

    // Tạo bài kiểm tra bên ngoài lớp học
    @Transactional
    public ExamDTO createExamWithoutClass(Long subjectId, Long userId, Exam examRequest) {
        // Kiểm tra môn học có tồn tại không
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Môn học không tồn tại!"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        // Kiểm tra quyền tạo bài kiểm tra (Chỉ admin hoặc giáo viên mới có quyền)
        if (!"admin".equals(user.getRole().getName()) && !"teacher".equals(user.getRole().getName())) {
            throw new RuntimeException("Bạn không có quyền tạo bài kiểm tra!");
        }

        // Tạo bài kiểm tra
        Exam exam = new Exam();
        exam.setName(examRequest.getName());
        exam.setExamCode(examRequest.getExamCode() != null ? examRequest.getExamCode() : "E-" + subjectId + "-" + System.currentTimeMillis());
        exam.setDescription(examRequest.getDescription());

        exam.setDurationTime(examRequest.getDurationTime());
        if (examRequest.getDurationTime() < 0) {
            throw new RuntimeException("Thời gian làm bài không hợp lệ!");
        }

        exam.setPassScore(examRequest.getPassScore());

        LocalDateTime now = LocalDateTime.now();

        // Bài kiểm tra không thuộc lớp nào, chỉ gắn với môn học
        exam.setClassSubject(null);
        exam.setSubject(subject);

        exam.setIsPublic(true);

        // Mặc định ở trạng thái DRAFT
        exam.setStatus(Exam.Status.DRAFT);
        exam.setStartAt(null);
        exam.setEndAt(null);

        // Thông tin người tạo
        exam.setCreatedBy(user);
        exam.setModifiedBy(user);
        exam.setCreatedAt(now);
        exam.setModifiedAt(now);

        return examMapper.toFullDTO(examRepository.save(exam));
    }

    //Lấy danh sách bài kiểm tra theo user và theo môn học
    public List<ExamDTO> getExamsBySubjectAndUser(Long subjectId, Long userId) {
        // Kiểm tra xem môn học có tồn tại không
        boolean exists = subjectRepository.existsById(subjectId);
        if (!exists) {
            throw new RuntimeException("Môn học không tồn tại!");
        }
    
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
        List<Exam> exams;
    
        // Nếu là student, chỉ lấy bài kiểm tra public
        if ("student".equals(user.getRole().getName())) {
            exams = examRepository.findBySubject_IdAndIsPublicTrue(subjectId);
        } 
        // Nếu là teacher, lấy tất cả bài kiểm tra do họ tạo
        else if ("teacher".equals(user.getRole().getName())) {
            exams = examRepository.findBySubject_IdAndCreatedBy(subjectId, user);
        }
        // Nếu là admin, lấy tất cả bài kiểm tra của môn học đó
        else if ("admin".equals(user.getRole().getName())) {
            exams = examRepository.findBySubject_Id(subjectId);
        } 
        // Các vai trò khác không có quyền truy cập danh sách bài kiểm tra
        else {
            throw new RuntimeException("Bạn không có quyền truy cập danh sách bài kiểm tra!");
        }
    
        return exams.stream().map(examMapper::toSimpleDTO).collect(Collectors.toList());
    }

    //Sinh viên vào làm bài kiểm tra
    @Transactional
    public ResultDTO startExam(Long examId, Long studentId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài kiểm tra không tồn tại"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (!student.getRole().getName().equals("student")) {
            throw new RuntimeException("Chỉ sinh viên mới có thể làm bài kiểm tra!");
        }

        if (exam.getStatus() != Exam.Status.PUBLISHED) {
            throw new RuntimeException("Bài kiểm tra chưa được công bố!");
        }

        Optional<Result> existingResultOpt = resultRepository.findByUserIdAndExamId(studentId, examId);
        
        if (existingResultOpt.isPresent()) {
            Result existingResult = existingResultOpt.get();
            if (!Boolean.TRUE.equals(existingResult.getAllowRetake())) {
                throw new RuntimeException("Bạn đã bắt đầu bài kiểm tra này rồi!");
            } else {
                // Reset trạng thái cho phép làm lại sau khi dùng
                existingResult.setAllowRetake(false);
                resultRepository.save(existingResult);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now;
        LocalDateTime endTime;

        if (Boolean.TRUE.equals(exam.getIsPublic())) {
            endTime = now.plusMinutes(exam.getDurationTime());
        } else {
            LocalDateTime examStart = exam.getStartAt();
            LocalDateTime examEnd = exam.getEndAt();
            int delayAllowedMinutes = 5;
            LocalDateTime latestAllowedStart = examStart.plusMinutes(delayAllowedMinutes);

            if (now.isBefore(examStart)) {
                throw new RuntimeException("Chưa đến thời gian bắt đầu làm bài!");
            }

            if (now.isAfter(latestAllowedStart)) {
                throw new RuntimeException("Bạn đã vào trễ quá 5 phút, không thể làm bài!");
            }

            if (now.isAfter(examEnd)) {
                throw new RuntimeException("Bài kiểm tra đã kết thúc!");
            }

            long minutesLate = java.time.Duration.between(examStart, now).toMinutes();
            long remainingTime = exam.getDurationTime() - minutesLate;

            if (remainingTime <= 0) {
                throw new RuntimeException("Không còn đủ thời gian để làm bài!");
            }

            endTime = now.plusMinutes(remainingTime);
        }

        Result result = new Result();
        result.setUser(student);
        result.setExam(exam);
        result.setStartedAt(startTime);
        result.setEndedAt(endTime);
        result.setIsSubmitted(false);
        result.setScore(0);
        result.setPassTest(Result.PassTest.FAIL);
        result.setAllowRetake(false); // Mặc định không được làm lại nữa

        return resultMapper.toFullDTO(resultRepository.save(result));
    }

    //Cho phép sinh viên làm lại bài kiểm tra
    @Transactional
    public ResultDTO allowStudentToRetake(Long examId, Long studentId, Long userId) {
        Result result = resultRepository.findByUserIdAndExamId(studentId, examId)
            .orElseThrow(() -> new RuntimeException("Sinh viên chưa từng làm bài này"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        if(!user.getRole().getName().equals("admin") && !user.getRole().getName().equals("teacher")) {
            throw new RuntimeException("Chỉ admin hoặc giáo viên mới có quyền cho phép làm lại bài kiểm tra");
        }

        result.setAllowRetake(true);
        return resultMapper.toFullDTO(resultRepository.save(result));
    }

    //Sinh viên chọn đáp án
    @Transactional
    public UserAnswerDTO chooseAnswer(Long examId, Long studentId, Long questionId, Long answerId) {
        // Kiểm tra sinh viên và bài kiểm tra
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài kiểm tra"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đáp án"));

        // Kiểm tra nếu sinh viên đã bắt đầu bài thi
        Result result = resultRepository.findByExamAndUser(exam, student)
                .orElseThrow(() -> new RuntimeException("Sinh viên chưa bắt đầu bài kiểm tra"));

        if (result.getIsSubmitted()) {
            throw new IllegalStateException("Bài thi đã được nộp, không thể thay đổi đáp án");
        }

        // Kiểm tra nếu đã chọn đáp án cho câu hỏi này
        UserAnswer userAnswer = userAnswerRepository.findByExamAndUserAndQuestion(exam, student, question)
                .orElse(new UserAnswer());

        userAnswer.setUser(student);
        userAnswer.setExam(exam);
        userAnswer.setQuestion(question);
        userAnswer.setAnswer(answer);
        userAnswer.setResult(result); 
        userAnswer.setIsSubmitted(false); // Đánh dấu là chưa nộp

        // Lưu đáp án tạm thời
        return userAnswerMapper.toDTO(userAnswerRepository.save(userAnswer));
    }

    //Nộp bài kiểm tra
    @Transactional
    public ResultDTO submitExam(Long examId, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên!"));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài kiểm tra!"));

        if (!student.getRole().getName().equals("student")) {
            throw new RuntimeException("Chỉ sinh viên mới có thể nộp bài kiểm tra!");
        }

        if (exam.getStatus() != Exam.Status.PUBLISHED) {
            throw new RuntimeException("Bài kiểm tra chưa được công bố!");
        }

        Result result = resultRepository.findByExamAndUser(exam, student)
                .orElseThrow(() -> new RuntimeException("Sinh viên chưa bắt đầu bài kiểm tra"));

        if (result.getIsSubmitted()) {
            throw new IllegalStateException("Bài thi đã được nộp trước đó");
        }

        List<UserAnswer> userAnswers = userAnswerRepository.findByExamAndUser(exam, student);

        int totalScore = 0;
        List<ExamQuestion> examQuestions = exam.getExamQuestions();

        for (ExamQuestion eq : examQuestions) {
            Question question = eq.getQuestion();
            int point = eq.getPoint();

            boolean isCorrect = userAnswers.stream().anyMatch(ua ->
                    ua.getQuestion().getId().equals(question.getId()) &&
                    ua.getAnswer().getIsCorrect());

            if (isCorrect) {
                totalScore += point;
            }
        }

        // Cập nhật điểm và nộp bài
        result.setScore(totalScore);
        result.setIsSubmitted(true);
        result.setSubmittedAt(LocalDateTime.now());
        result.setPassTest(totalScore >= exam.getPassScore() ? Result.PassTest.PASS : Result.PassTest.FAIL);

        // Cập nhật lại trạng thái của các đáp án
        userAnswers.forEach(userAnswer -> {
            userAnswer.setIsSubmitted(true);  // Đánh dấu là đã nộp
            userAnswerRepository.save(userAnswer);
        });

        return resultMapper.toFullDTO(resultRepository.save(result));
    }

    //Lấy danh sách kết quả của sinh viên
    public List<ResultDTO> getResults(Long studentId, Long examId) {
        List<Result> results = resultRepository.findByUserIdAndIsSubmittedTrue(studentId);
        return results.stream()
                .map(resultMapper::toSimpleDTO) 
                .collect(Collectors.toList());
    }   
}
