package org.mangala.portfolio.portfolio.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreatePortfolioRequestDTO {
    @NotBlank(message = "Portfolio name is required")
    @Size(max = 100, message = "Portfolio name must be at most 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @Size(max = 10, message = "Maximum 10 wallets per portfolio")
    private List<UUID> walletIds;
}
