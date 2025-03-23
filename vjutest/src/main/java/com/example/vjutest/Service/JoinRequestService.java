package com.example.vjutest.Service;

import com.example.vjutest.Model.JoinRequest;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.JoinRequestRepository;
import com.example.vjutest.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JoinRequestService {

    private JoinRequestRepository joinRequestRepository;
    private UserRepository userRepository;

    @Autowired
    public JoinRequestService(JoinRequestRepository joinRequestRepository, UserRepository userRepository) {
        this.joinRequestRepository = joinRequestRepository;
        this.userRepository = userRepository;
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

    public String approveTeacherInvite(Long requestId, Long inviteeId) {
        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời!"));
    
        if (!request.getUser().getId().equals(inviteeId)) {
            throw new RuntimeException("Bạn không có quyền phê duyệt lời mời này!");
        }
    
        request.setStatus(JoinRequest.Status.APPROVED);
        request.getClassEntity().getTeachers().add(request.getUser());
        joinRequestRepository.save(request);
    
        return "Bạn đã chấp nhận lời mời và trở thành giáo viên của lớp!";
    }
    
    public String rejectTeacherInvite(Long requestId, Long inviteeId) {
        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời!"));
    
        if (!request.getUser().getId().equals(inviteeId)) {
            throw new RuntimeException("Bạn không có quyền từ chối lời mời này!");
        }
    
        request.setStatus(JoinRequest.Status.REJECTED);
        joinRequestRepository.save(request);
    
        return "Bạn đã từ chối lời mời tham gia lớp!";
    }
}
