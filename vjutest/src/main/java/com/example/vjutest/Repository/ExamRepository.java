package com.example.vjutest.Repository;

import com.example.vjutest.Model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByClassSubject_ClassEntity_Id(Long classId); 
    List<Exam> findByClassSubject_ClassEntity_IdAndIsPublicTrue(Long classId);
}
