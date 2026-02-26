package org.mangala.portfolio.shared.exception;

import org.mangala.exception.BaseException;
import org.mangala.exception.ErrorDefinition;

public class PortfolioException extends BaseException {

    public PortfolioException(ErrorDefinition errorDefinition) {
        super(errorDefinition);
    }
}
