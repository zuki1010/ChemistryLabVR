package dkhoa.chemistrylabvr.service;

import dkhoa.chemistrylabvr.dto.request.authentication.LoginRequest;
import dkhoa.chemistrylabvr.dto.request.authentication.RegisterRequest;
import dkhoa.chemistrylabvr.dto.response.authentication.AuthResponse;
import dkhoa.chemistrylabvr.dto.response.authentication.UserResponse;
import dkhoa.chemistrylabvr.entity.RevokedAccessToken;
import dkhoa.chemistrylabvr.entity.User;
import dkhoa.chemistrylabvr.enums.UserRole;
import dkhoa.chemistrylabvr.exception.AppException;
import dkhoa.chemistrylabvr.repository.RevokedAccessTokenRepository;
import dkhoa.chemistrylabvr.repository.UserRepository;
import dkhoa.chemistrylabvr.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RevokedAccessTokenRepository revokedAccessTokenRepository;

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

    public void logout(String authorization) {
        String token = authorization.substring(7); // bỏ "Bearer "

        Claims claims = jwtService.parseClaims(token);
        String tokenId = claims.getId();

        if (tokenId == null) {
            throw new AppException("Token không có jti; hãy đăng nhập lại");
        }

        UUID jti = UUID.fromString(tokenId);

        // Gọi logout lần nữa với cùng token thì không tạo bản ghi trùng
        if (revokedAccessTokenRepository.existsById(jti)) {
            return;
        }

        User user = new User();
        user.setId(UUID.fromString(claims.getSubject()));

        RevokedAccessToken revoked = new RevokedAccessToken();
        revoked.setJti(jti);
        revoked.setUser(user);
        revoked.setExpiresAt(claims.getExpiration().toInstant());
        revoked.setRevokedAt(Instant.now());

        revokedAccessTokenRepository.save(revoked);
    }

}
