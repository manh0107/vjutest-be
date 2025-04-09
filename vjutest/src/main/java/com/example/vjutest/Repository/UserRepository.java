package com.example.vjutest.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vjutest.Model.Role;
import com.example.vjutest.Model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    List<User> findByRole(Role role);
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String token);

    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByCode(Long code);
    boolean existsByPhoneNumber(Long phoneNumber);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByCodeAndIdNot(Long code, Long id);
    boolean existsByPhoneNumberAndIdNot(Long phoneNumber, Long id);

}
