package dkhoa.chemistrylabvr.dto.request.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    @Email
    @NotNull
    String email;
    @NotNull
    String password;
    String fullName;
}
