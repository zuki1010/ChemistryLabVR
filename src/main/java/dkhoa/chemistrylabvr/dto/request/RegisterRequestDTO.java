package dkhoa.chemistrylabvr.dto.request;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    String email;
    String password;
    String fullName;
}
