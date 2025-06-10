package com.example.vjutest.Controller;

import com.example.vjutest.Service.UserService;
import com.example.vjutest.Service.ClassEntityService;
import com.example.vjutest.Service.ExamService;
import com.example.vjutest.Service.SubjectService;
import com.example.vjutest.Service.ResultService;
import com.example.vjutest.DTO.ResultDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    @Autowired
    private UserService userService;
    @Autowired
    private ClassEntityService classEntityService;
    @Autowired
    private ExamService examService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private ResultService resultService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.countAllUsers());
        stats.put("totalClasses", classEntityService.countAllClasses());
        stats.put("totalExams", examService.countAllExams());
        stats.put("totalSubjects", subjectService.countAllSubjects());
        List<ResultDTO> recentResults = resultService.findRecentResults(10); // Lấy 10 kết quả gần nhất
        stats.put("recentResults", recentResults);
        return ResponseEntity.ok(stats);
    }
} 