package com.example.vjutest.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vjutest.Model.Subject;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

}
