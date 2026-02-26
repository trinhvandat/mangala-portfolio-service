CREATE TABLE portfolio_wallets (
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    wallet_id UUID NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (portfolio_id, wallet_id)
);

CREATE INDEX idx_portfolio_wallets_wallet_id ON portfolio_wallets(wallet_id);
