package com.example.vjutest.Repository;

import com.example.vjutest.Model.JoinRequest;
import com.example.vjutest.Model.ClassEntity;
import com.example.vjutest.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    Optional<JoinRequest> findByUserAndClassEntity(User user, ClassEntity classEntity);
}
