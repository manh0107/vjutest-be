package com.example.vjutest.Initializer;

import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.example.vjutest.Model.Role;
import com.example.vjutest.Repository.RoleRepository;

@Component("roleInitializer") // Định danh tên Bean để DataInitializer phụ thuộc vào nó
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        List<String> roleNames = List.of("student", "teacher", "admin");
        for (String roleName : roleNames) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
                System.out.println("Role '" + roleName + "' đã được tạo!");
            }
        }
    }
}
