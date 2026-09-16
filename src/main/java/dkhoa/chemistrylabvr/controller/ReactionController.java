package dkhoa.chemistrylabvr.controller;

import dkhoa.chemistrylabvr.entity.Reaction;
import dkhoa.chemistrylabvr.service.ReactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reaction")
@Tag(name = "Reaction", description = "CRUD reaction")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ReactionController {
    private final ReactionService reactionService;

    @GetMapping("/all")
    public ResponseEntity<List<Reaction>> getAll() {
        return ResponseEntity.ok(reactionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reaction> getOne(@PathVariable("id") Long reactionId) {
        return ResponseEntity.ok(reactionService.getOne(reactionId));
    }
}
