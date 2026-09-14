package dkhoa.chemistrylabvr.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    String email;
    String password;
    String fullName;
}
