package com.example.vjutest.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vjutest.Model.Chapter;
import com.example.vjutest.Model.Subject;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long>{
    List<Chapter> findBySubject(Subject subject);
}
