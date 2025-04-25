package com.example.vjutest.Initializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.vjutest.Model.Role;
import com.example.vjutest.Model.Department;
import com.example.vjutest.Model.Major;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.RoleRepository;
import com.example.vjutest.Repository.DepartmentRepository;
import com.example.vjutest.Repository.MajorRepository;
import com.example.vjutest.Repository.UserRepository;

import jakarta.annotation.PostConstruct;

@Component
public class SystemInitializer {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void init() {
        System.out.println("Bắt đầu khởi tạo hệ thống...");

        // 1. Khởi tạo roles
        System.out.println("Đang khởi tạo các role...");
        String[] roleNames = {"admin", "teacher", "student"};
        for (String roleName : roleNames) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                System.out.println("Đã tạo role: " + roleName);
            } else {
                System.out.println("Role đã tồn tại: " + roleName);
            }
        }

        // 2. Khởi tạo department mặc định
        System.out.println("Đang khởi tạo khoa mặc định...");
        String defaultDeptName = "Khoa Quản lý người dùng";
        Department defaultDepartment = null;
        
        if (!departmentRepository.existsByName(defaultDeptName)) {
            defaultDepartment = new Department();
            defaultDepartment.setName(defaultDeptName);
            departmentRepository.save(defaultDepartment);
            System.out.println("Đã tạo khoa mặc định: " + defaultDeptName);
        } else {
            defaultDepartment = departmentRepository.findByName(defaultDeptName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa mặc định"));
            System.out.println("Khoa mặc định đã tồn tại: " + defaultDeptName);
        }

        // 3. Khởi tạo major mặc định
        System.out.println("Đang khởi tạo ngành học mặc định...");
        String defaultMajorName = "Ngành Quản lý người dùng";
        Major defaultMajor = null;

        if (!majorRepository.existsByName(defaultMajorName)) {
            defaultMajor = new Major();
            defaultMajor.setName(defaultMajorName);
            defaultMajor.setDepartment(defaultDepartment);
            majorRepository.save(defaultMajor);
            System.out.println("Đã tạo ngành học mặc định: " + defaultMajorName);
        } else {
            defaultMajor = majorRepository.findByName(defaultMajorName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ngành học mặc định"));
            System.out.println("Ngành học mặc định đã tồn tại: " + defaultMajorName);
        }

        // 4. Khởi tạo admin user
        System.out.println("Đang khởi tạo admin user...");
        if (!userRepository.existsByName("Administrator")) {
            Role adminRole = roleRepository.findByName("admin")
                .orElseThrow(() -> new RuntimeException("Role admin không tồn tại"));

            User admin = new User();
            admin.setName("Administrator");
            admin.setEmail("vjutestapp@gmail.com");
            admin.setPassword(passwordEncoder.encode("vjutestadmin"));
            admin.setPhoneNumber(Long.parseLong("9999999999"));
            admin.setCode(Long.parseLong("99999999"));
            admin.setGender("Other");
            admin.setImage("https://static.vecteezy.com/system/resources/thumbnails/020/765/399/small/default-profile-account-unknown-icon-black-silhouette-free-vector.jpg");
            admin.setRole(adminRole);
            admin.setDepartment(defaultDepartment);
            admin.setMajor(defaultMajor);
            admin.setIsEnabled(true);
            userRepository.save(admin);
            System.out.println("Đã tạo admin user");

            // 5. Update createdBy và modifiedBy cho department và major
            defaultDepartment.setCreatedBy(admin);
            defaultDepartment.setModifiedBy(admin);
            departmentRepository.save(defaultDepartment);
            System.out.println("Đã cập nhật createdBy cho khoa mặc định");

            defaultMajor.setCreatedBy(admin);
            defaultMajor.setModifiedBy(admin);
            majorRepository.save(defaultMajor);
            System.out.println("Đã cập nhật createdBy cho ngành học mặc định");
        } else {
            System.out.println("Admin user đã tồn tại");
        }

        System.out.println("Hoàn thành khởi tạo hệ thống");
    }
} 