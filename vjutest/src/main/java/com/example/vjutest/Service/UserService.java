package com.example.vjutest.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.vjutest.Model.Role;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.RoleRepository;
import com.example.vjutest.Repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public User createUser(String name, Long code, Long phoneNumber, String className, String gender, String email, String password, Long roleId, String image) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User(name, code, phoneNumber, className, gender, email, password, role, image);
        return userRepository.save(user);
    }

    public User createUser(User user) {
        // Validate required fields
        if (user.getName() == null || user.getName().isEmpty()) {
            throw new RuntimeException("Name is required");
        }
        
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        
        if (user.getRole() == null || user.getRole().getId() == null) {
            throw new RuntimeException("Role ID is required");
        }
        
        Role role = roleRepository.findById(user.getRole().getId())
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + user.getRole().getId()));
        
        user.setRole(role);
        return userRepository.save(user);
    }
}