package dkhoa.chemistrylabvr.repository;

import dkhoa.chemistrylabvr.entity.RevokedAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RevokedAccessTokenRepository extends JpaRepository<RevokedAccessToken, UUID> {
}