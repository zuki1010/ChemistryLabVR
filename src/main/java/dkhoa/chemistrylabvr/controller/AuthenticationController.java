package dkhoa.chemistrylabvr.controller;

import dkhoa.chemistrylabvr.dto.request.LoginRequest;
import dkhoa.chemistrylabvr.dto.request.RegisterRequest;
import dkhoa.chemistrylabvr.entity.User;
import dkhoa.chemistrylabvr.service.AuthenticationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Login, register, logout")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest dto) {
        return ResponseEntity.ok(authenticationService.login(dto));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest dto) {
        authenticationService.register(dto);
        return ResponseEntity.ok("Register Successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(null);
    }
}
