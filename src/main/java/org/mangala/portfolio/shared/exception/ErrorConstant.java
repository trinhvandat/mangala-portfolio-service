package org.mangala.portfolio.shared.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.mangala.exception.ErrorDefinition;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorConstant implements ErrorDefinition {
    // Portfolio errors (03xxxxx)
    PORTFOLIO_NOT_FOUND(HttpStatus.NOT_FOUND, "0300001", "Portfolio not found."),
    PORTFOLIO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "0300002", "Access denied to this portfolio."),
    PORTFOLIO_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "0300003", "Portfolio limit exceeded. Maximum 10 portfolios per user."),
    WALLET_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "0300004", "Wallet limit exceeded. Maximum 10 wallets per portfolio."),
    WALLET_ALREADY_IN_PORTFOLIO(HttpStatus.BAD_REQUEST, "0300005", "Wallet is already in this portfolio."),
    WALLET_NOT_IN_PORTFOLIO(HttpStatus.BAD_REQUEST, "0300006", "Wallet is not in this portfolio."),
    INVALID_PERIOD(HttpStatus.BAD_REQUEST, "0300007", "Invalid period parameter. Valid values: 24h, 7d, 30d, 90d, 1y, all."),
    SNAPSHOT_NOT_FOUND(HttpStatus.NOT_FOUND, "0300008", "No snapshot data available for the requested period."),
    PRICE_FEED_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "0300009", "Price feed service is temporarily unavailable.");

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String errorMessage;
}
