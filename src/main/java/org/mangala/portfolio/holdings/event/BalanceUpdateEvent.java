package org.mangala.portfolio.holdings.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceUpdateEvent {
    private UUID walletId;
    private UUID userId;
    private String chainType;
    private String address;
    private List<TokenBalanceData> balances;
    private Instant timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenBalanceData {
        private String contractAddress;
        private String symbol;
        private String name;
        private int decimals;
        private BigDecimal balance;
    }
}
