package com.example.vjutest.Initializer;

import com.example.vjutest.Model.Role;
import com.example.vjutest.Model.User;
import com.example.vjutest.Repository.RoleRepository;
import com.example.vjutest.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class UserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleInitializer roleInitializer;

    @Autowired
    public UserInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, RoleInitializer roleInitializer) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleInitializer = roleInitializer;
    }

    @Override
    public void run(String... args) {

        roleInitializer.run();

        // Lấy role "admin" từ database (đã có từ RoleInitializer)
        Optional<Role> adminRoleOpt = roleRepository.findByName("admin");
        
        if (adminRoleOpt.isPresent()) {
            // Kiểm tra nếu user admin chưa tồn tại thì tạo mới
            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                User adminUser = new User();
                adminUser.setName("Administrator");
                adminUser.setEmail("vjutestapp@gmail.com");
                adminUser.setCode(00000000L);
                adminUser.setPhoneNumber(0000000000L);
                adminUser.setPassword(passwordEncoder.encode("vjutestadmin"));
                adminUser.setImage("https://static.vecteezy.com/system/resources/thumbnails/020/765/399/small/default-profile-account-unknown-icon-black-silhouette-free-vector.jpg");
                adminUser.setRole(adminRoleOpt.get());
                adminUser.setEnabled(true); // Kích hoạt tài khoản ngay lập tức

                userRepository.save(adminUser);
                System.out.println("Admin user đã được tạo!");
            }
        } else {
            System.out.println("Chưa có role 'admin', hãy kiểm tra lại RoleInitializer!");
        }
    }
}
