package com.example.vjutest.Controller;

import com.example.vjutest.DTO.ResultDTO;
import com.example.vjutest.Service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/results")
public class ResultController {
    private final ResultService resultService;

    @Autowired
    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ResultDTO> getAllResults() {
        return resultService.findAllResults();
    }
} 