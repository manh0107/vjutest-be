package com.example.vjutest.Service;

import com.example.vjutest.DTO.ExamDTO;
import com.example.vjutest.Mapper.ExamMapper;
import com.example.vjutest.Model.*;
import com.example.vjutest.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final ClassEntityRepository classEntityRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ExamMapper examMapper;

    @Autowired
    public ExamService(ExamRepository examRepository,
                       ClassEntityRepository classEntityRepository,
                       ClassSubjectRepository classSubjectRepository,
                       SubjectRepository subjectRepository,
                       UserRepository userRepository,
                       ExamMapper examMapper) {
        this.examRepository = examRepository;
        this.classEntityRepository = classEntityRepository;
        this.classSubjectRepository = classSubjectRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.examMapper = examMapper;
    }

    @Transactional
    public ExamDTO createExam(Long classId, Long subjectId, Long userId, Exam examRequest) {
        // Kiểm tra lớp học có tồn tại không
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Lớp học không tồn tại!"));

        // Kiểm tra môn học có tồn tại không
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Môn học không tồn tại!"));

        // Kiểm tra user có quyền tạo bài kiểm tra không
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
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

        // Gán thông tin từ request vào đối tượng Exam
        Exam exam = new Exam();
        exam.setName(examRequest.getName());
        exam.setExamCode(examRequest.getExamCode() != null ? examRequest.getExamCode() : "E-" + subjectId + "-" + System.currentTimeMillis());
        exam.setDescription(examRequest.getDescription());
        exam.setDurationTime(examRequest.getDurationTime());
        exam.setPassQuit(examRequest.getPassQuit());
        exam.setPublic(examRequest.isPublic());

        // Kiểm tra startAt và endAt hợp lệ
        exam.setStartAt(examRequest.getStartAt() != null ? examRequest.getStartAt() : LocalDateTime.now());
        if (examRequest.getEndAt() != null && examRequest.getEndAt().isBefore(exam.getStartAt())) {
            throw new RuntimeException("Thời gian kết thúc không thể trước thời gian bắt đầu!");
        }
        exam.setEndAt(examRequest.getEndAt());

        // Gán trạng thái và người tạo
        exam.setStatus(Exam.Status.DRAFT);
        exam.setCreatedBy(user);
        exam.setModifiedBy(user);
        exam.setClassSubject(classSubject);

        return examMapper.toFullDTO(examRepository.save(exam));
    }

    public List<ExamDTO> getExamsInClass(Long classId, Long userId) {
        ClassEntity classEntity = classEntityRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Lớp học không tồn tại!"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        List<Exam> exams;
        if (!classEntity.getUsers().contains(user) && !classEntity.getTeachers().contains(user)) {
            exams = examRepository.findByClassSubject_ClassEntity_IdAndIsPublicTrue(classId);
        } else {
            exams = examRepository.findByClassSubject_ClassEntity_Id(classId);
        }

        return exams.stream().map(examMapper::toSimpleDTO).collect(Collectors.toList());
    }

    public Optional<ExamDTO> getExamById(Long examId, Long userId) {
        Optional<Exam> examOptional = examRepository.findById(examId);
        if (examOptional.isEmpty()) {
            return Optional.empty();
        }

        Exam exam = examOptional.get();
        ClassEntity classEntity = exam.getClassSubject().getClassEntity();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        if (!classEntity.getUsers().contains(user) && !classEntity.getTeachers().contains(user) && !exam.isPublic()) {
            return Optional.empty();
        }

        return Optional.of(examMapper.toFullDTO(exam));
    }
}
