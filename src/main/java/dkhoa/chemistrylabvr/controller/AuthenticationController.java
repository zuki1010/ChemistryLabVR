package dkhoa.chemistrylabvr.controller;

import dkhoa.chemistrylabvr.dto.request.authentication.LoginRequest;
import dkhoa.chemistrylabvr.dto.request.authentication.RegisterRequest;
import dkhoa.chemistrylabvr.dto.response.authentication.AuthResponse;
import dkhoa.chemistrylabvr.dto.response.authentication.UserResponse;
import dkhoa.chemistrylabvr.entity.User;
import dkhoa.chemistrylabvr.security.SecurityConfig;
import dkhoa.chemistrylabvr.service.AuthenticationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Login, register, logout")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest dto) {
        return ResponseEntity.ok(authenticationService.login(dto));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest dto) {
        authenticationService.register(dto);
        return ResponseEntity.ok("Register Successfully");
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorization) {
        authenticationService.logout(authorization);
        return ResponseEntity.noContent().build();
    }
}
