package com.example.vjutest.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vjutest.Model.Chapter;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long>{

}
