package payApp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NonNull
    private UUID walletId;

    @NotBlank
    private String recipientUserName;

    @NonNull
    @Positive
    private BigDecimal amount;
}
