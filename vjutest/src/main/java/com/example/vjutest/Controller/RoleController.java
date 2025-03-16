package com.example.vjutest.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.vjutest.Model.Role;
import com.example.vjutest.Repository.RoleRepository;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {
    
    @Autowired
    private final RoleRepository roleRepository;

    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
