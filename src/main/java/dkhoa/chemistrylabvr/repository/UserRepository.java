package dkhoa.chemistrylabvr.repository;

import dkhoa.chemistrylabvr.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByEmailAndPassword(String email, String password);
}