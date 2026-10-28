package dkhoa.chemistrylabvr.dto.request.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {
    @Email
    @NotNull(message = "Email không được để trống!")
    String email;
    @NotNull(message = "Password không được để trống")
    String password;
}
