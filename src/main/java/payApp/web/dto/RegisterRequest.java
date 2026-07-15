package payApp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import payApp.user.model.Country;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank
    @Size(min = 6, max = 24, message = "Username must be between 6 and 24 symbols.")
    private String username;

    @NotBlank
    @Size(min = 6, max = 6, message = "Password must be 6 symbols.")
    private String password;

    @NotNull
    private Country country;

}
