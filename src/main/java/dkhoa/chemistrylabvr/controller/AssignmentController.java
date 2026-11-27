package dkhoa.chemistrylabvr.controller;

import dkhoa.chemistrylabvr.dto.request.education.EducationDtos.AssignmentInput;
import dkhoa.chemistrylabvr.dto.request.education.EducationDtos.DispatchInput;
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
@Tag(name = "Education - Bài tập", description = "Tạo quiz/thực hành, xuất bản, giao bài và xem bài được giao")
public class AssignmentController {
    private final EducationService service;

    @PostMapping("/assignments")
    public ResponseEntity<Map<String, Object>> createAssignment(@RequestBody AssignmentInput input, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveAssignment(null, input, user(auth), admin(auth)));
    }

    @PutMapping("/assignments/{id}")
    public Map<String, Object> updateAssignment(@PathVariable Long id, @RequestBody AssignmentInput input, Authentication auth) {
        return service.saveAssignment(id, input, user(auth), admin(auth));
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<Void> archiveAssignment(@PathVariable Long id, Authentication auth) {
        service.archiveAssignment(id, user(auth), admin(auth));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assignments")
    public List<Map<String, Object>> authoredAssignments(Authentication auth) { return service.authoredAssignments(user(auth), admin(auth)); }

    @GetMapping("/assignments/{id}")
    public Map<String, Object> assignment(@PathVariable Long id, Authentication auth) { return service.getAssignment(id, user(auth), admin(auth)); }

    @PostMapping("/assignments/{id}/dispatch")
    public ResponseEntity<Map<String, Object>> dispatch(@PathVariable Long id, @RequestBody DispatchInput input, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.dispatch(id, input, user(auth), admin(auth)));
    }

    @GetMapping("/me/assignments")
    public List<Map<String, Object>> myAssignments(Authentication auth) { return service.myAssignments(user(auth)); }

    private UUID user(Authentication auth) { return UUID.fromString(auth.getName()); }
    private boolean admin(Authentication auth) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("ROLE_ADMIN"::equals);
    }
}
