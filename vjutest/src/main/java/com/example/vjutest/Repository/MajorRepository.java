package com.example.vjutest.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vjutest.Model.Major;
import com.example.vjutest.Model.Department;
import com.example.vjutest.Model.Subject;

@Repository
public interface MajorRepository extends JpaRepository<Major, Long> {
    List<Major> findByDepartment(Department department);
    int countByDepartment(Department department);
    int countBySubjectsContaining(Subject subject);
} 