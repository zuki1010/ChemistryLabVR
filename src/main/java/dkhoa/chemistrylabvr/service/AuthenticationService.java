package dkhoa.chemistrylabvr.service;

import dkhoa.chemistrylabvr.dto.request.authentication.LoginRequest;
import dkhoa.chemistrylabvr.dto.request.authentication.RegisterRequest;
import dkhoa.chemistrylabvr.dto.response.authentication.AuthResponse;
import dkhoa.chemistrylabvr.dto.response.authentication.UserResponse;
import dkhoa.chemistrylabvr.entity.User;
import dkhoa.chemistrylabvr.enums.UserRole;
import dkhoa.chemistrylabvr.exception.AppException;
import dkhoa.chemistrylabvr.repository.UserRepository;
import dkhoa.chemistrylabvr.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest dto) {
        User user = userRepository.findByEmail(dto.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new AppException("Email hoặc password không hợp lệ!"));

        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new AppException("Email hoặc password không hợp lệ!");
        }

        String accessToken = jwtService.generateAccessToken(user);
        return new AuthResponse(
                accessToken,
                "Bearer",
                new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole()
                )
        );
    }

    public void register(RegisterRequest dto) {
        User u = new User();
        if(userRepository.existsByEmail(dto.getEmail())) {
            throw new AppException("Email này đã tồn tại!");
        }
        u.setEmail(dto.getEmail().trim());
        u.setPassword(passwordEncoder.encode(dto.getPassword()));
        u.setFullName(dto.getFullName().trim());
        u.setRole(UserRole.STUDENT);
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());
        u.setIsActive(true);
        userRepository.save(u);
    }

}
