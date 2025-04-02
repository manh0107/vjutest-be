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
import java.util.Collections;

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
        exam.setPassQuit(examRequest.getPassQuit());
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

        if (newStatus == Exam.Status.PUBLISHED) {
            if (startAt == null || startAt.isBefore(exam.getCreatedAt().plusDays(1))) {
                throw new RuntimeException("Thời gian bắt đầu phải cách thời gian tạo ít nhất 1 ngày!");
            }            
            if (endAt == null || endAt.isBefore(startAt.plusMinutes(30))) {
                throw new RuntimeException("Thời gian kết thúc phải cách thời gian bắt đầu ít nhất 30 phút!");
            }            
            exam.setStartAt(startAt);
            exam.setEndAt(endAt);
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
        exam.setPassQuit(examRequest.getPassQuit());

        LocalDateTime now = LocalDateTime.now();

        // Bài kiểm tra không thuộc lớp nào, chỉ gắn với môn học
        exam.setClassSubject(null);
        exam.setSubject(subject);

        exam.setPublic(true);

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
}
