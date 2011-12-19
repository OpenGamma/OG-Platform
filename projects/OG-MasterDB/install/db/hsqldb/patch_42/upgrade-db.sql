ALTER TABLE prt_portfolio ADD COLUMN visibility smallint;
UPDATE prt_portfolio SET visibility = 25;
ALTER TABLE prt_portfolio ALTER COLUMN visibility SET NOT NULL;
CREATE INDEX ix_prt_portfolio_visibility ON prt_portfolio(visibility);
