package dkhoa.chemistrylabvr.service;

import dkhoa.chemistrylabvr.dto.request.LoginRequestDTO;
import dkhoa.chemistrylabvr.dto.request.RegisterRequestDTO;
import dkhoa.chemistrylabvr.entity.User;
import dkhoa.chemistrylabvr.enums.UserRole;
import dkhoa.chemistrylabvr.exception.AppException;
import dkhoa.chemistrylabvr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;

    public User login(LoginRequestDTO dto) {
        return userRepository.findByEmailAndPassword(dto.getEmail(), dto.getPassword());
    }

    public void register(RegisterRequestDTO dto) {
        User u = new User();
        if(userRepository.existsByEmail(dto.getEmail())) {
            throw new AppException("Email này đã tồn tại!");
        }
        u.setEmail(dto.getEmail());
        u.setPassword(dto.getPassword());
        u.setFullName(dto.getFullName());
        u.setRole(UserRole.STUDENT);
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());
        u.setIsActive(true);
        userRepository.save(u);
    }

}
