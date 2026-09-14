package dkhoa.chemistrylabvr.controller;

import dkhoa.chemistrylabvr.dto.request.LoginRequestDTO;
import dkhoa.chemistrylabvr.dto.request.RegisterRequestDTO;
import dkhoa.chemistrylabvr.entity.User;
import dkhoa.chemistrylabvr.service.AuthenticationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication API", description = "Login, register, logout")
@CrossOrigin("*")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authenticationService.login(dto));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO dto) {
        authenticationService.register(dto);
        return ResponseEntity.ok("Register Successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(null);
    }
}
