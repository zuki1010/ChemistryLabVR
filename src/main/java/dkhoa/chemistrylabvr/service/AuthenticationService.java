package dkhoa.chemistrylabvr.service;

import dkhoa.chemistrylabvr.dto.request.LoginRequestDTO;
import dkhoa.chemistrylabvr.entity.User;
import dkhoa.chemistrylabvr.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;

    public User login(LoginRequestDTO dto) {
        return userRepository.findByEmailAndPassword(dto.getEmail(), dto.getPassword());
    }
}
