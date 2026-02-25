CREATE TABLE portfolio_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    total_value_usd DECIMAL(20,8) NOT NULL,
    snapshot_time TIMESTAMP NOT NULL,
    holdings JSONB NOT NULL,
    chain_breakdown JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_snapshots_portfolio_time ON portfolio_snapshots(portfolio_id, snapshot_time DESC);
CREATE INDEX idx_snapshots_time ON portfolio_snapshots(snapshot_time DESC);
