package com.example.vjutest.Service;

import com.example.vjutest.DTO.ResultDTO;
import com.example.vjutest.Mapper.ResultMapper;
import com.example.vjutest.Model.Result;
import com.example.vjutest.Repository.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResultService {
    private final ResultRepository resultRepository;
    private final ResultMapper resultMapper;

    @Autowired
    public ResultService(ResultRepository resultRepository, ResultMapper resultMapper) {
        this.resultRepository = resultRepository;
        this.resultMapper = resultMapper;
    }

    public List<ResultDTO> findAllResults() {
        List<Result> results = resultRepository.findAll();
        return results.stream().map(resultMapper::toFullDTO).collect(Collectors.toList());
    }

    public List<ResultDTO> findRecentResults(int limit) {
        List<Result> results = resultRepository.findTop10ByOrderBySubmitTimeDesc();
        return results.stream().map(resultMapper::toFullDTO).collect(Collectors.toList());
    }
} 