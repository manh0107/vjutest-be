package com.example.vjutest.Controller;

import com.example.vjutest.DTO.JoinRequestDTO;
import com.example.vjutest.Mapper.JoinRequestMapper;
import com.example.vjutest.Model.JoinRequest;
import com.example.vjutest.Service.JoinRequestService;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/join-request")
@CrossOrigin(origins = "*")
public class JoinRequestController {

    private JoinRequestService joinRequestService;
    private JoinRequestMapper joinRequestMapper;

    @Autowired
    public JoinRequestController(JoinRequestService joinRequestService, JoinRequestMapper joinRequestMapper) {
        this.joinRequestService = joinRequestService;
        this.joinRequestMapper = joinRequestMapper;
    }

    @PostMapping("/approve")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> approveRequest(@RequestParam Long requestId, @RequestParam Long userId) {
        try {
            String message = joinRequestService.approveRequest(requestId, userId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reject")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<?> rejectRequest(@RequestParam Long requestId, @RequestParam Long userId) {
        try {
            String message = joinRequestService.rejectRequest(requestId, userId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_TEACHER')")
    public ResponseEntity<List<JoinRequestDTO>> getAllRequests() {
        List<JoinRequest> requests = joinRequestService.getAllRequests();
        List<JoinRequestDTO> joinRequestDTOs = requests.stream()
                .map(joinRequestMapper::toSimpleDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(joinRequestDTOs);
    }

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @PostMapping("/{requestId}/approve")
    public ResponseEntity<String> approveTeacherInvite(
            @PathVariable Long requestId,
            @RequestParam Long inviteeId) {
        String response = joinRequestService.approveTeacherInvite(requestId, inviteeId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<String> rejectTeacherInvite(
            @PathVariable Long requestId,
            @RequestParam Long inviteeId) {
        String response = joinRequestService.rejectTeacherInvite(requestId, inviteeId);
        return ResponseEntity.ok(response);
    }
}
