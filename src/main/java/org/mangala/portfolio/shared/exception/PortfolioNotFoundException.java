package org.mangala.portfolio.shared.exception;

import org.mangala.exception.BaseException;

public class PortfolioNotFoundException extends BaseException {
    public PortfolioNotFoundException() {
        super(ErrorConstant.PORTFOLIO_NOT_FOUND);
    }
}
