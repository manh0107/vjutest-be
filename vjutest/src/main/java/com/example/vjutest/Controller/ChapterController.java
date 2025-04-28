package com.example.vjutest.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.vjutest.Model.Chapter;
import com.example.vjutest.Service.ChapterService;
import com.example.vjutest.User.CustomUserDetails;
import com.example.vjutest.DTO.ChapterDTO;
import com.example.vjutest.Mapper.ChapterMapper;

@RestController
@RequestMapping("/chapters")
public class ChapterController {

    private final ChapterService chapterService;
    private final ChapterMapper chapterMapper;

    @Autowired
    public ChapterController(ChapterService chapterService, ChapterMapper chapterMapper) {
        this.chapterService = chapterService;
        this.chapterMapper = chapterMapper;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createChapter(@RequestParam String name,
                                         @RequestParam Long subjectId,
                                         Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
            Chapter chapter = chapterService.createChapter(name, subjectId, userId);
            ChapterDTO chapterDTO = chapterMapper.toDTO(chapter);
            return ResponseEntity.ok(chapterDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/subject/{subjectId}/all")
    public ResponseEntity<?> getAllChapters(@PathVariable Long subjectId,      
                                            Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
            List<Chapter> chapters = chapterService.getAllChapters(subjectId, userId);
            List<ChapterDTO> chapterDTOs = chapters.stream()
                    .map(chapterMapper::toDTO)
                    .toList();
            return ResponseEntity.ok(chapterDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<?> getChapterById(@PathVariable Long id,
                                            Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
            Optional<Chapter> chapterOpt = chapterService.getChapterById(id, userId);
            if (chapterOpt.isPresent()) {
                ChapterDTO chapterDTO = chapterMapper.toDTO(chapterOpt.get());
                return ResponseEntity.ok(chapterDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateChapter(@PathVariable Long id,
                                         @RequestParam String name,
                                         Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
            Chapter chapter = chapterService.updateChapter(id, name, userId);
            ChapterDTO chapterDTO = chapterMapper.toDTO(chapter);
            return ResponseEntity.ok(chapterDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteChapter(@PathVariable Long id,
                                        Authentication authentication) {
        try {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
            chapterService.deleteChapter(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 