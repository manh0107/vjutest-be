package com.example.vjutest.Controller;

import com.example.vjutest.Model.JoinRequest;
import com.example.vjutest.Service.JoinRequestService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/join-request")
@CrossOrigin(origins = "*")
public class JoinRequestController {

    @Autowired
    private JoinRequestService joinRequestService;

    @PostMapping("/request")
    public ResponseEntity<?> requestToJoin(@RequestParam Long userId, @RequestParam Long classId) {
        try {
            String message = joinRequestService.requestToJoin(userId, classId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/approve")
    public ResponseEntity<?> approveRequest(@RequestParam Long requestId, @RequestParam Long userId) {
        try {
            String message = joinRequestService.approveRequest(requestId, userId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reject")
    public ResponseEntity<?> rejectRequest(@RequestParam Long requestId, @RequestParam Long userId) {
        try {
            String message = joinRequestService.rejectRequest(requestId, userId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<JoinRequest>> getAllJoinRequests() {
        List<JoinRequest> requests = joinRequestService.getAllRequests();
        return ResponseEntity.ok(requests);
    }
}
