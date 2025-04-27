package com.example.vjutest.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vjutest.Model.ClassEntity;
import com.example.vjutest.Model.User;

@Repository
public interface ClassEntityRepository extends JpaRepository<ClassEntity, Long> {
    List<ClassEntity> findByUsers(User user);
    boolean existsByClassCode(String classCode);
    boolean existsByIdAndUsers_Id(Long classId, Long userId);
    Optional<ClassEntity> findByClassCode(String classCode);
    List<ClassEntity> findByDepartments_Id(Long departmentId);
    List<ClassEntity> findByMajors_Id(Long majorId);
    List<ClassEntity> findByCreatedBy(User createdBy);
}
