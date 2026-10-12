package dkhoa.chemistrylabvr.dto.response.authentication;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UserResponse user
) {
}
