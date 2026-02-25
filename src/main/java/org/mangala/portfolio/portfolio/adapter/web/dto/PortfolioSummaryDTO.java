package org.mangala.portfolio.portfolio.adapter.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PortfolioSummaryDTO {
    private UUID id;
    private String name;
    private String description;
    private boolean isDefault;
    private int walletCount;
}
