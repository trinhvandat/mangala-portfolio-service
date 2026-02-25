package org.mangala.portfolio.portfolio.adapter.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AddWalletsRequestDTO {
    @NotEmpty(message = "At least one wallet ID is required")
    @Size(max = 10, message = "Maximum 10 wallets can be added at once")
    private List<UUID> walletIds;
}
