package dkhoa.chemistrylabvr.controller;

import dkhoa.chemistrylabvr.dto.request.education.EducationDtos.ClassroomInput;
import dkhoa.chemistrylabvr.dto.request.education.EducationDtos.JoinInput;
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
@Tag(name = "Education - Lớp học", description = "Quản lý lớp, mã tham gia và danh sách học viên")
public class ClassroomController {
    private final EducationService service;

    @GetMapping("/classrooms")
    public List<Map<String, Object>> classrooms(Authentication auth) { return service.classrooms(user(auth), admin(auth)); }

    @PostMapping("/classrooms")
    public ResponseEntity<Map<String, Object>> createClassroom(@RequestBody ClassroomInput input, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createClassroom(input, user(auth), admin(auth)));
    }

    @PutMapping("/classrooms/{id}")
    public Map<String, Object> updateClassroom(@PathVariable Long id, @RequestBody ClassroomInput input, Authentication auth) {
        return service.updateClassroom(id, input, user(auth), admin(auth));
    }

    @PutMapping("/classrooms/{id}/active")
    public Map<String, Object> classroomActive(@PathVariable Long id, @RequestParam boolean active, Authentication auth) {
        return service.setClassroomActive(id, active, user(auth), admin(auth));
    }

    @PostMapping("/classrooms/join")
    public Map<String, Object> join(@RequestBody JoinInput input, Authentication auth) {
        return service.joinClass(input == null ? null : input.joinCode(), user(auth));
    }

    @GetMapping("/classrooms/{id}/students")
    public List<Map<String, Object>> roster(@PathVariable Long id, Authentication auth) {
        return service.roster(id, user(auth), admin(auth));
    }

    @DeleteMapping("/classrooms/{id}/students/{studentId}")
    public ResponseEntity<Void> removeStudent(@PathVariable Long id, @PathVariable UUID studentId, Authentication auth) {
        service.removeStudent(id, studentId, user(auth), admin(auth));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/classrooms/{id}/assignments")
    public List<Map<String, Object>> classAssignments(@PathVariable Long id, Authentication auth) {
        return service.classAssignments(id, user(auth), admin(auth));
    }

    private UUID user(Authentication auth) { return UUID.fromString(auth.getName()); }
    private boolean admin(Authentication auth) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("ROLE_ADMIN"::equals);
    }
}
