package org.mangala.portfolio.portfolio.adapter.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PortfolioResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private boolean isDefault;
    private List<UUID> walletIds;
}
