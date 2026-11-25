package dkhoa.chemistrylabvr.controller;

import dkhoa.chemistrylabvr.dto.request.reaction.CreateReaction;
import dkhoa.chemistrylabvr.dto.response.reaction.ReactionList;
import dkhoa.chemistrylabvr.service.ReactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/reaction")
@Tag(name = "Reaction", description = "CRUD reaction")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ReactionController {
    private final ReactionService reactionService;

    @GetMapping("/all")
    public ResponseEntity<List<ReactionList>> getAll() {
        return ResponseEntity.ok(reactionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReactionList> getOne(@PathVariable("id") Long reactionId) {
        return ResponseEntity.ok(reactionService.getOne(reactionId));
    }

    @PostMapping("/create")
    public ResponseEntity<ReactionList> create(@Valid @RequestBody CreateReaction dto) {
        return ResponseEntity.status(201).body(reactionService.create(dto));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ReactionList> update(@PathVariable Long id,
                                               @Valid @RequestBody CreateReaction dto) {
        return ResponseEntity.ok(reactionService.update(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
