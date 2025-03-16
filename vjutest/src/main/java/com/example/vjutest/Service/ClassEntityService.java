package com.example.vjutest.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.vjutest.Model.ClassEntity;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.ClassEntityRepository;
import com.example.vjutest.Repository.UserRepository;

@Service
public class ClassEntityService {

    @Autowired
    private final ClassEntityRepository classEntityRepository;
    private final UserRepository userRepository;

    public ClassEntityService(ClassEntityRepository classEntityRepository, UserRepository userRepository) {
        this.classEntityRepository = classEntityRepository;
        this.userRepository = userRepository;
    }

    public ClassEntity createClass(String name, String description, String classCode, Long userId) {
        User createBy = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + userId));
    
        if (!createBy.getRole().getName().equalsIgnoreCase("teacher") && !createBy.getRole().getName().equalsIgnoreCase("admin")) {
            throw new RuntimeException("Chỉ giáo viên và quản trị viên mới có thể tạo lớp học");
        }

        ClassEntity newClassEntity = new ClassEntity(name, classCode, description, createBy);

        return classEntityRepository.save(newClassEntity);
    }
    
}
