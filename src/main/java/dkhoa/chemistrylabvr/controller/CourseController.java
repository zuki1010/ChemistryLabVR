package dkhoa.chemistrylabvr.controller;

import dkhoa.chemistrylabvr.dto.request.education.EducationDtos.CourseInput;
import dkhoa.chemistrylabvr.dto.request.education.EducationDtos.UnitInput;
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
@Tag(name = "Education - Học phần", description = "Quản lý học phần và chủ đề")
public class CourseController {
    private final EducationService service;

    @GetMapping("/courses")
    public List<Map<String, Object>> courses(Authentication auth) {
        return service.courses(user(auth), admin(auth));
    }

    @PostMapping("/courses")
    public ResponseEntity<Map<String, Object>> createCourse(@RequestBody CourseInput input, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveCourse(null, input, user(auth), admin(auth)));
    }

    @PutMapping("/courses/{id}")
    public Map<String, Object> updateCourse(@PathVariable Long id, @RequestBody CourseInput input, Authentication auth) {
        return service.saveCourse(id, input, user(auth), admin(auth));
    }

    @GetMapping("/courses/{id}/units")
    public List<Map<String, Object>> units(@PathVariable Long id, Authentication auth) {
        return service.units(id, user(auth), admin(auth));
    }

    @PostMapping("/courses/{id}/units")
    public ResponseEntity<Map<String, Object>> createUnit(@PathVariable Long id, @RequestBody UnitInput input, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveUnit(id, null, input, user(auth), admin(auth)));
    }

    @PutMapping("/courses/{courseId}/units/{unitId}")
    public Map<String, Object> updateUnit(@PathVariable Long courseId, @PathVariable Long unitId, @RequestBody UnitInput input, Authentication auth) {
        return service.saveUnit(courseId, unitId, input, user(auth), admin(auth));
    }

    private UUID user(Authentication auth) { return UUID.fromString(auth.getName()); }
    private boolean admin(Authentication auth) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("ROLE_ADMIN"::equals);
    }
}
