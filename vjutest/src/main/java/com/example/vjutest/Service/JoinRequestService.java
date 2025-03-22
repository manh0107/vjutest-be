package com.example.vjutest.Service;

import com.example.vjutest.Model.JoinRequest;
import com.example.vjutest.Model.User;
import com.example.vjutest.Model.ClassEntity;
import com.example.vjutest.Repository.JoinRequestRepository;
import com.example.vjutest.Repository.ClassEntityRepository;
import com.example.vjutest.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JoinRequestService {

    private JoinRequestRepository joinRequestRepository;
    private UserRepository userRepository;
    private ClassEntityRepository classEntityRepository;

    @Autowired
    public JoinRequestService(JoinRequestRepository joinRequestRepository, ClassEntityRepository classEntityRepository, UserRepository userRepository) {
        this.joinRequestRepository = joinRequestRepository;
        this.classEntityRepository = classEntityRepository;
        this.userRepository = userRepository;
    }

    public String requestToJoin(Long userId, Long classId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        ClassEntity classEntity = classEntityRepository.findById(classId).orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học!"));

        if (!user.getRole().getName().equals("student")) {
            throw new RuntimeException("Chỉ sinh viên mới có thể tham gia lớp học!");
        }

        Optional<JoinRequest> existingRequest = joinRequestRepository.findByUserAndClassEntity(user, classEntity);
        if (existingRequest.isPresent()) {
            throw new RuntimeException("Bạn đã gửi yêu cầu tham gia lớp học này rồi!");
        }

        JoinRequest joinRequest = new JoinRequest(user, classEntity);
        joinRequestRepository.save(joinRequest);
        return "Yêu cầu tham gia lớp học đã được gửi!";
    }

    public String approveRequest(Long requestId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        JoinRequest request = joinRequestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu!"));

        if (!(user.getRole().getName().equals("admin") || user.getRole().getName().equals("teacher"))) {
            throw new RuntimeException("Chỉ giáo viên và quản trị viên mới có thể phê duyệt yêu cầu");
        }

        request.setStatus(JoinRequest.Status.APPROVED);
        request.getClassEntity().getUsers().add(request.getUser());
        joinRequestRepository.save(request);

        return "Sinh viên đã được thêm vào lớp học!";
    }

    public String rejectRequest(Long requestId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        JoinRequest request = joinRequestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu!"));

        if (!(user.getRole().getName().equals("admin") || user.getRole().getName().equals("teacher"))) {
            throw new RuntimeException("Chỉ giáo viên và quản trị viên mới có thể phê duyệt yêu cầu");
        }

        request.setStatus(JoinRequest.Status.REJECTED);
        joinRequestRepository.save(request);

        return "Yêu cầu đã bị từ chối!";
    }

    public List<JoinRequest> getAllRequests() {
        return joinRequestRepository.findAll();
    }
}
