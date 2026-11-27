package dkhoa.chemistrylabvr.controller;

import dkhoa.chemistrylabvr.dto.request.education.EducationDtos.GradeInput;
import dkhoa.chemistrylabvr.dto.request.education.EducationDtos.PracticeSubmitInput;
import dkhoa.chemistrylabvr.dto.request.education.EducationDtos.QuizSubmitInput;
import dkhoa.chemistrylabvr.service.EducationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/education")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Education - Bài làm & chấm điểm", description = "Tạo lượt làm, nộp quiz/thực hành, xem bài và chấm điểm")
public class SubmissionController {
    private final EducationService service;

    @PostMapping("/class-assignments/{id}/attempts")
    public ResponseEntity<Map<String, Object>> startAttempt(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.startAttempt(id, user(auth)));
    }

    @PostMapping("/attempts/{id}/quiz-submit")
    public Map<String, Object> submitQuiz(@PathVariable Long id, @RequestBody QuizSubmitInput input, Authentication auth) {
        return service.submitQuiz(id, input, user(auth));
    }

    @PostMapping("/attempts/{id}/practice-submit")
    public Map<String, Object> submitPractice(@PathVariable Long id, @RequestBody PracticeSubmitInput input, Authentication auth) {
        return service.submitPractice(id, input, user(auth));
    }

    @GetMapping("/class-assignments/{id}/attempts")
    public List<Map<String, Object>> attempts(@PathVariable Long id, Authentication auth) {
        return service.attempts(id, user(auth), admin(auth));
    }

    @GetMapping("/attempts/{id}")
    public Map<String, Object> attemptDetails(@PathVariable Long id, Authentication auth) {
        return service.attemptDetails(id, user(auth), admin(auth));
    }

    @PutMapping("/attempts/{id}/grade")
    public Map<String, Object> grade(@PathVariable Long id, @RequestBody GradeInput input, Authentication auth) {
        return service.grade(id, input, user(auth), admin(auth));
    }

    private UUID user(Authentication auth) { return UUID.fromString(auth.getName()); }
    private boolean admin(Authentication auth) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("ROLE_ADMIN"::equals);
    }
}
