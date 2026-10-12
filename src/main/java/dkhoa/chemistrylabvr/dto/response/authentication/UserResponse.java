package dkhoa.chemistrylabvr.dto.response.authentication;

import dkhoa.chemistrylabvr.enums.UserRole;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String fullName,
    UserRole role
) {
}
