package com.example.vjutest.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vjutest.Model.ClassEntity;
import com.example.vjutest.Model.User;

@Repository
public interface ClassEntityRepository extends JpaRepository<ClassEntity, Long> {
    List<ClassEntity> findByUsers(User user);
}
