package org.mangala.portfolio.shared.exception;

import org.mangala.exception.BaseException;

public class PortfolioAccessDeniedException extends BaseException {
    public PortfolioAccessDeniedException() {
        super(ErrorConstant.PORTFOLIO_ACCESS_DENIED);
    }
}
