package com.example.vjutest.Service;

import com.example.vjutest.DTO.ExamDTO;
import com.example.vjutest.DTO.ResultDTO;
import com.example.vjutest.DTO.UserAnswerDTO;
import com.example.vjutest.DTO.ExamStatisticsDTO;
import com.example.vjutest.Mapper.ExamMapper;
import com.example.vjutest.Mapper.ResultMapper;
import com.example.vjutest.Mapper.UserAnswerMapper;
import com.example.vjutest.Model.*;
import com.example.vjutest.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
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
    private final ExamTakingService examTakingService;
    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;
    private final ChapterRepository chapterRepository;
    @Autowired
    private QuestionService questionService;

    //Tạo bài kiểm tra trong lớp học
    @Transactional
    public ExamDTO createExam(Long classId, Long subjectId, Long userId, ExamDTO examRequest) {
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

        // Kiểm tra examCode không được trùng
        if (examRequest.getExamCode() != null && examRepository.existsByExamCode(examRequest.getExamCode())) {
            throw new RuntimeException("Mã bài kiểm tra đã tồn tại!");
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
        
        // Thời gian làm bài sẽ được set sau khi hoàn thành bài kiểm tra
        exam.setDurationTime(0L);
        exam.setIsPublic(examRequest.getIsPublic());
        
        // Lấy phạm vi hiển thị từ lớp học
        exam.setVisibility(classEntity.getVisibility() == ClassEntity.VisibilityScope.PUBLIC ? 
            Exam.ExamVisibility.PUBLIC : 
            (classEntity.getVisibility() == ClassEntity.VisibilityScope.DEPARTMENT ? 
                Exam.ExamVisibility.DEPARTMENT : 
                Exam.ExamVisibility.MAJOR));

        // Lấy danh sách khoa và ngành từ lớp học
        if (classEntity.getDepartments() != null && !classEntity.getDepartments().isEmpty()) {
            exam.setDepartments(new HashSet<>(classEntity.getDepartments()));
        }
        if (classEntity.getMajors() != null && !classEntity.getMajors().isEmpty()) {
            exam.setMajors(new HashSet<>(classEntity.getMajors()));
        }

        if (examRequest.getChapterIds() != null && !examRequest.getChapterIds().isEmpty()) {
            exam.setChapters(new HashSet<>(chapterRepository.findAllById(examRequest.getChapterIds())));
        }

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
            // Admin có thể xem tất cả bài kiểm tra trong lớp
            exams = examRepository.findByClassSubject_ClassEntity_IdAndClassSubject_Subject_Id(classId, subjectId);
        } else if ("teacher".equals(user.getRole().getName())) {
            // Giáo viên chỉ xem được bài kiểm tra do mình tạo trong lớp
            exams = examRepository.findByClassSubject_ClassEntity_IdAndClassSubject_Subject_IdAndCreatedBy(classId, subjectId, user);
        } else if ("student".equals(user.getRole().getName())) {
            // Sinh viên chỉ xem được bài kiểm tra trong lớp của mình
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
    public ExamDTO updateExamStatus(Long examId, Exam.Status newStatus, LocalDateTime startAt, LocalDateTime endAt, Long userId, Integer passPercent, Long durationTime) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài kiểm tra không tồn tại!"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        // Kiểm tra quyền cập nhật bài kiểm tra
        if (!exam.getCreatedBy().equals(user) && !"admin".equals(user.getRole().getName())) {
            throw new RuntimeException("Bạn không có quyền cập nhật bài kiểm tra này!");
        }

        int totalQuestions = exam.getExamQuestions().size();
        if(totalQuestions < 10) {
            throw new RuntimeException("Bài kiểm tra phải có ít nhất 10 câu hỏi!");
        }
        exam.setQuestionsCount(totalQuestions); // luôn cập nhật số lượng câu hỏi

        if (newStatus == Exam.Status.PUBLISHED) {
            boolean hasIncomplete = exam.getExamQuestions().stream()
            .map(ExamQuestion::getQuestion)
            .anyMatch(q -> !Boolean.TRUE.equals(q.getIsCompleted()));

            if (hasIncomplete) {
                throw new RuntimeException("Không thể hoàn thành: Có câu hỏi chưa hoàn thành (chưa có đáp án đầy đủ)!");
            }
            // Tính tổng điểm và điểm đạt
            int totalScore = exam.getExamQuestions().stream()
                .mapToInt(ExamQuestion::getPoint)
                .sum();
            exam.setMaxScore(totalScore);
            int percent = (passPercent != null && passPercent > 0 && passPercent < 100) ? passPercent : 60;
            exam.setPassScore((int) Math.ceil(totalScore * percent / 100.0));

            // Kiểm tra và set thời gian làm bài
            long minDuration = (long) Math.ceil(totalQuestions * 1.5); // 1.5 phút cho mỗi câu hỏi
            if (durationTime != null && durationTime > 0) {
                if (durationTime < minDuration) {
                    throw new RuntimeException("Thời gian làm bài phải lớn hơn hoặc bằng " + minDuration + " phút (1.5 phút mỗi câu)");
                }
                exam.setDurationTime(durationTime);
            } else {
                if (exam.getDurationTime() < minDuration) {
                    exam.setDurationTime(minDuration);
                }
            }

            if(Boolean.FALSE.equals(exam.getIsPublic())) {
                if (startAt == null || startAt.isBefore(exam.getCreatedAt().plusMinutes(30))) {
                    throw new RuntimeException("Thời gian bắt đầu phải cách thời gian tạo ít nhất 30 phút!");
                }                     
                exam.setStartAt(startAt);
                exam.setEndAt(startAt.plusMinutes(exam.getDurationTime()));
            } else {
                exam.setStartAt(LocalDateTime.of(1000, 1, 1, 0, 0));
                exam.setEndAt(LocalDateTime.of(9999, 12, 31, 23, 59));
            } 
        } 

        exam.setStatus(newStatus);
        exam.setModifiedAt(LocalDateTime.now());

        return examMapper.toFullDTO(examRepository.save(exam));
    }

    // Tạo bài kiểm tra bên ngoài lớp học
    @Transactional
    public ExamDTO createExamWithoutClass(Long subjectId, Long userId, ExamDTO examRequest, List<Long> departmentIds, List<Long> majorIds) {
        // Kiểm tra môn học có tồn tại không
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Môn học không tồn tại!"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        // Kiểm tra quyền tạo bài kiểm tra (Chỉ admin hoặc giáo viên mới có quyền)
        if (!"admin".equals(user.getRole().getName()) && !"teacher".equals(user.getRole().getName())) {
            throw new RuntimeException("Bạn không có quyền tạo bài kiểm tra!");
        }

        // Kiểm tra examCode không được trùng
        if (examRequest.getExamCode() != null && examRepository.existsByExamCode(examRequest.getExamCode())) {
            throw new RuntimeException("Mã bài kiểm tra đã tồn tại!");
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

        exam.setIsPublic(true);
        exam.setVisibility(examRequest.getVisibility());
        exam.setMaxAttempts(examRequest.getMaxAttempts() != null ? examRequest.getMaxAttempts() : 1);
        exam.setRandomQuestions(examRequest.getRandomQuestions() != null ? examRequest.getRandomQuestions() : false);

        // Kiểm tra và xử lý phạm vi hiển thị
        if (exam.getVisibility() == Exam.ExamVisibility.DEPARTMENT && (departmentIds == null || departmentIds.isEmpty())) {
            throw new RuntimeException("Phải chọn ít nhất một khoa khi phạm vi truy cập là theo khoa");
        }
        if (exam.getVisibility() == Exam.ExamVisibility.MAJOR && (majorIds == null || majorIds.isEmpty())) {
            throw new RuntimeException("Phải chọn ít nhất một ngành khi phạm vi truy cập là theo ngành");
        }

        // Set departments và majors nếu có
        if (departmentIds != null && !departmentIds.isEmpty()) {
            Set<Department> departments = new HashSet<>(departmentRepository.findAllById(departmentIds));
            exam.setDepartments(departments);
        } else if (examRequest.getSelectedDepartments() != null && !examRequest.getSelectedDepartments().isEmpty()) {
            Set<Department> departments = new HashSet<>(departmentRepository.findAllById(examRequest.getSelectedDepartments()));
            exam.setDepartments(departments);
        }

        if (majorIds != null && !majorIds.isEmpty()) {
            Set<Major> majors = new HashSet<>(majorRepository.findAllById(majorIds));
            exam.setMajors(majors);
        } else if (examRequest.getSelectedMajors() != null && !examRequest.getSelectedMajors().isEmpty()) {
            Set<Major> majors = new HashSet<>(majorRepository.findAllById(examRequest.getSelectedMajors()));
            exam.setMajors(majors);
        }

        if (examRequest.getChapterIds() != null && !examRequest.getChapterIds().isEmpty()) {
            exam.setChapters(new HashSet<>(chapterRepository.findAllById(examRequest.getChapterIds())));
        }

        LocalDateTime now = LocalDateTime.now();

        // Bài kiểm tra không thuộc lớp nào, chỉ gắn với môn học
        exam.setClassSubject(null);
        exam.setSubject(subject);

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
    
        // Nếu là student, chỉ lấy bài kiểm tra public ngoài lớp học có status PUBLISHED
        if ("student".equals(user.getRole().getName())) {
            exams = examRepository.findBySubject_IdAndIsPublicTrueAndStatus(subjectId, Exam.Status.PUBLISHED)
                .stream()
                .filter(exam -> {
                    // Nếu là PUBLIC thì cho phép truy cập
                    if (exam.getVisibility() == Exam.ExamVisibility.PUBLIC) {
                        return true;
                    }
                    
                    // Nếu là DEPARTMENT, kiểm tra department của user
                    if (exam.getVisibility() == Exam.ExamVisibility.DEPARTMENT) {
                        return exam.getDepartments() != null && 
                               user.getDepartment() != null &&
                               exam.getDepartments().contains(user.getDepartment());
                    }
                    
                    // Nếu là MAJOR, kiểm tra major của user
                    if (exam.getVisibility() == Exam.ExamVisibility.MAJOR) {
                        return exam.getMajors() != null && 
                               user.getMajor() != null &&
                               exam.getMajors().contains(user.getMajor());
                    }
                    
                    return false;
                })
                .collect(Collectors.toList());
        } 
        // Nếu là teacher, lấy tất cả bài kiểm tra do họ tạo
        else if ("teacher".equals(user.getRole().getName())) {
            exams = examRepository.findBySubject_IdAndCreatedBy(subjectId, user)
                .stream()
                .filter(exam -> {
                    // Nếu là PUBLIC thì cho phép truy cập
                    if (exam.getVisibility() == Exam.ExamVisibility.PUBLIC) {
                        return true;
                    }
                    
                    // Nếu là DEPARTMENT, kiểm tra department của user
                    if (exam.getVisibility() == Exam.ExamVisibility.DEPARTMENT) {
                        return exam.getDepartments() != null && 
                               user.getDepartment() != null &&
                               exam.getDepartments().contains(user.getDepartment());
                    }
                    
                    // Nếu là MAJOR, kiểm tra major của user
                    if (exam.getVisibility() == Exam.ExamVisibility.MAJOR) {
                        return exam.getMajors() != null && 
                               user.getMajor() != null &&
                               exam.getMajors().contains(user.getMajor());
                    }
                    
                    return false;
                })
                .collect(Collectors.toList());
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

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now;
        LocalDateTime endTime;

        if (Boolean.TRUE.equals(exam.getIsPublic())) {
            // Cho phép làm lại không giới hạn lần: luôn tạo Result mới
            endTime = now.plusMinutes(exam.getDurationTime());
            Result result = new Result();
            result.setUser(student);
            result.setExam(exam);
            result.setStartTime(startTime);
            result.setEndTime(endTime);
            result.setIsSubmitted(false);
            result.setScore(0);
            result.setIsPassed(false);
            result.setAllowRetake(false);
            return resultMapper.toFullDTO(resultRepository.save(result));
        } else {
            // Logic cũ cho bài kiểm tra không phải public
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

            Result result = new Result();
            result.setUser(student);
            result.setExam(exam);
            result.setStartTime(startTime);
            result.setEndTime(endTime);
            result.setIsSubmitted(false);
            result.setScore(0);
            result.setIsPassed(false);
            result.setAllowRetake(false);
            return resultMapper.toFullDTO(resultRepository.save(result));
        }
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
        Result result = resultRepository.findTopByExamAndUserOrderByStartTimeDesc(exam, student)
                .orElseThrow(() -> new RuntimeException("Sinh viên chưa bắt đầu bài kiểm tra"));

        if (result.getIsSubmitted()) {
            throw new IllegalStateException("Bài thi đã được nộp, không thể thay đổi đáp án");
        }

        // Kiểm tra nếu đã chọn đáp án cho câu hỏi này
        UserAnswer userAnswer = userAnswerRepository.findByExamAndUserAndQuestion(exam, student, question)
                .orElseGet(() -> {
                    // Nếu có nhiều bản ghi, lấy bản ghi mới nhất theo id
                    List<UserAnswer> all = userAnswerRepository.findByExamAndUser(exam, student);
                    return all.stream()
                        .filter(ua -> ua.getQuestion().equals(question))
                        .max((a, b) -> Long.compare(a.getId(), b.getId()))
                        .orElse(new UserAnswer());
                });

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

        Result result = examTakingService.submitExam(examId, student);
        return resultMapper.toFullDTO(result);
    }

    //Lấy danh sách kết quả của sinh viên
    public List<ResultDTO> getResults(Long studentId, Long examId) {
        List<Result> results = resultRepository.findByUserIdAndIsSubmittedTrue(studentId);
        return results.stream()
                .map(resultMapper::toSimpleDTO) 
                .collect(Collectors.toList());
    }

    //Update bài kiểm tra trong lớp
    @Transactional
    public ExamDTO updateExamInClass(Long examId, Long userId, Long classId, Exam examRequest) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài kiểm tra không tồn tại!"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Lớp học không tồn tại!"));

        if (!exam.getCreatedBy().equals(user) && !classEntity.getCreatedBy().equals(user) && !"admin".equals(user.getRole().getName())) {
            throw new RuntimeException("Bạn không có quyền cập nhật bài kiểm tra này!");
        }

        // Kiểm tra examCode không được trùng với bài kiểm tra khác
        if (examRequest.getExamCode() != null && !examRequest.getExamCode().equals(exam.getExamCode()) 
            && examRepository.existsByExamCode(examRequest.getExamCode())) {
            throw new RuntimeException("Mã bài kiểm tra đã tồn tại!");
        }

        exam.setName(examRequest.getName());
        exam.setDescription(examRequest.getDescription());
        exam.setExamCode(examRequest.getExamCode());
        exam.setModifiedBy(user);
        exam.setModifiedAt(LocalDateTime.now());

        return examMapper.toFullDTO(examRepository.save(exam));
    }

    //Update bài kiểm tra không thuộc lớp
    @Transactional
    public ExamDTO updateExamWithoutClass(Long examId, Long userId, Exam examRequest) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài kiểm tra không tồn tại!"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        // Kiểm tra quyền cập nhật (admin hoặc teacher tạo bài này)
        if (!"admin".equals(user.getRole().getName()) && !user.equals(exam.getCreatedBy())) {
            throw new RuntimeException("Bạn không có quyền cập nhật bài kiểm tra này!");
        }

        // Bảo vệ không cho gắn vào lớp học nếu đang ở ngoài lớp
        if (exam.getClassSubject() != null) {
            throw new RuntimeException("Bài kiểm tra này thuộc lớp học, không thể cập nhật bằng phương thức này!");
        }

        // Kiểm tra examCode không được trùng với bài kiểm tra khác
        if (examRequest.getExamCode() != null && !examRequest.getExamCode().equals(exam.getExamCode()) 
            && examRepository.existsByExamCode(examRequest.getExamCode())) {
            throw new RuntimeException("Mã bài kiểm tra đã tồn tại!");
        }

        exam.setName(examRequest.getName());
        exam.setDescription(examRequest.getDescription());
        exam.setExamCode(examRequest.getExamCode());
        exam.setModifiedBy(user);
        exam.setModifiedAt(LocalDateTime.now());

        return examMapper.toFullDTO(examRepository.save(exam));
    }

    @Transactional
    public void deleteExam(Long id, Long userId) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài kiểm tra"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!exam.getCreatedBy().getId().equals(userId) && !user.getRole().getName().equals("admin")) {
            throw new RuntimeException("Bạn không có quyền xóa bài kiểm tra này");
        }

        examRepository.delete(exam);
    }

    public ExamStatisticsDTO getExamStatistics(Long examId, Long userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài kiểm tra"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!exam.getCreatedBy().getId().equals(userId) && !user.getRole().getName().equals("admin")) {
            throw new RuntimeException("Bạn không có quyền xem thống kê bài kiểm tra này");
        }

        ExamStatisticsDTO statistics = new ExamStatisticsDTO();
        statistics.setTotalStudents(exam.getResults().size());
        statistics.setAverageScore(exam.getResults().stream()
                .mapToDouble(Result::getScore)
                .average()
                .orElse(0.0));
        statistics.setHighestScore(exam.getResults().stream()
                .mapToDouble(Result::getScore)
                .max()
                .orElse(0.0));
        statistics.setLowestScore(exam.getResults().stream()
                .mapToDouble(Result::getScore)
                .min()
                .orElse(0.0));

        return statistics;
    }

    @Transactional
    public void closeExam(Long examId, Long userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài kiểm tra không tồn tại!"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
        
        if(!exam.getCreatedBy().equals(user) && !"admin".equals(user.getRole().getName())) {
            throw new RuntimeException("Bạn không có quyền đóng bài kiểm tra này!");
        }
        
        exam.setStatus(Exam.Status.CLOSED);
        exam.setModifiedBy(user);
        examRepository.save(exam);

        // Tự động thêm câu hỏi public vào chương tương ứng
        questionService.autoAddPublicQuestionsToChapters(exam);
    }

    @Transactional
    public void deleteQuestionFromExam(Long questionId, Long examId, Long userId) {
        // Kiểm tra bài kiểm tra có tồn tại không
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Bài kiểm tra không tồn tại!"));

        // Kiểm tra quyền xóa câu hỏi
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
        
        if (!exam.getCreatedBy().equals(user) && !"admin".equals(user.getRole().getName())) {
            throw new RuntimeException("Bạn không có quyền xóa câu hỏi khỏi bài kiểm tra này!");
        }

        // Kiểm tra trạng thái bài kiểm tra
        if (exam.getStatus() != Exam.Status.DRAFT) {
            throw new RuntimeException("Chỉ có thể xóa câu hỏi khi bài kiểm tra đang ở trạng thái DRAFT!");
        }

        // Tìm và xóa câu hỏi khỏi bài kiểm tra
        ExamQuestion examQuestion = exam.getExamQuestions().stream()
                .filter(eq -> eq.getQuestion().getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Câu hỏi không tồn tại trong bài kiểm tra!"));

        exam.getExamQuestions().remove(examQuestion);
        exam.setQuestionsCount(exam.getTotalQuestions() - 1);
        exam.setModifiedAt(LocalDateTime.now());
        exam.setModifiedBy(user);

        examRepository.save(exam);
    }

    @Transactional
    public ExamDTO revertToDraft(Long examId, Long userId) {
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài kiểm tra"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        if (!exam.getCreatedBy().equals(user) && !"admin".equals(user.getRole().getName())) {
            throw new RuntimeException("Bạn không có quyền chuyển trạng thái bài kiểm tra này!");
        }
        exam.setStatus(Exam.Status.DRAFT);
        exam.setModifiedBy(user);
        exam.setModifiedAt(LocalDateTime.now());
        examRepository.save(exam);
        return examMapper.toFullDTO(exam);
    }

    @Transactional
    public void autoAddExamToPublic(Exam exam) {
        if (exam.getStatus() != Exam.Status.CLOSED) {
            return;
        }

        if (exam.getMarkedAsPublic()) {
            exam.setIsPublic(true);
            examRepository.save(exam);
        }
    }

    // Lấy kết quả làm bài hiện tại (chưa nộp) của sinh viên cho một bài kiểm tra
    public ResultDTO getCurrentResult(Long examId, Long studentId) {
        Result result = resultRepository.findTopByUserIdAndExamIdOrderByStartTimeDesc(studentId, examId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bạn chưa bắt đầu làm bài kiểm tra này!"));
        if (Boolean.TRUE.equals(result.getIsSubmitted())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bạn đã nộp bài kiểm tra này rồi!");
        }
        return resultMapper.toFullDTO(result);
    }

    public ResultDTO getLatestSubmittedResult(Long studentId, Long examId) {
        Result result = resultRepository
            .findTopByUserIdAndExamIdAndIsSubmittedTrueOrderBySubmitTimeDesc(studentId, examId)
            .orElseThrow(() -> new RuntimeException("Chưa có kết quả đã nộp cho bài kiểm tra này!"));
        return resultMapper.toFullDTO(result);
    }

    public Map<String, Integer> getCorrectAndWrongAnswers(Long studentId, Long examId) {
        Result result = resultRepository
            .findTopByUserIdAndExamIdAndIsSubmittedTrueOrderBySubmitTimeDesc(studentId, examId)
            .orElseThrow(() -> new RuntimeException("Chưa có kết quả đã nộp cho bài kiểm tra này!"));
        List<UserAnswer> answers = userAnswerRepository.findByResultId(result.getId());
        int correct = 0, wrong = 0;
        for (UserAnswer ua : answers) {
            if (ua.getAnswer() != null && Boolean.TRUE.equals(ua.getAnswer().getIsCorrect())) {
                correct++;
            } else {
                wrong++;
            }
        }
        Map<String, Integer> map = new HashMap<>();
        map.put("correct", correct);
        map.put("wrong", wrong);
        return map;
    }

    public long countAllExams() {
        return examRepository.count();
    }
}
