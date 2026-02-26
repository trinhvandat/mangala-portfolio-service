package org.mangala.portfolio.shared.exception;

import org.mangala.exception.BaseException;

public class WalletLimitExceededException extends BaseException {
    public WalletLimitExceededException() {
        super(ErrorConstant.WALLET_LIMIT_EXCEEDED);
    }
}
