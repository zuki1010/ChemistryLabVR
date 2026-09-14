package dkhoa.chemistrylabvr.service;

import dkhoa.chemistrylabvr.dto.request.LoginRequestDTO;
import dkhoa.chemistrylabvr.dto.request.RegisterRequestDTO;
import dkhoa.chemistrylabvr.entity.User;
import dkhoa.chemistrylabvr.enums.UserRole;
import dkhoa.chemistrylabvr.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;

    public User login(LoginRequestDTO dto) {
        return userRepository.findByEmailAndPassword(dto.getEmail(), dto.getPassword());
    }

    public void register(RegisterRequestDTO dto) {
        User u = new User();
        u.setEmail(dto.getEmail());
        u.setPassword(dto.getPassword());
        u.setFullName(dto.getFullName());
        u.setRole(UserRole.STUDENT);
        userRepository.save(u);
    }

}
