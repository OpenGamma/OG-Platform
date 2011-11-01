CREATE INDEX ix_sec_swap_security_id ON sec_swap(security_id);
CREATE INDEX ix_sec_bond_security_id ON sec_bond(security_id);
CREATE INDEX ix_sec_equity_security_id ON sec_equity(security_id);
CREATE INDEX ix_sec_future_security_id ON sec_future(security_id);
CREATE INDEX ix_sec_equityoption_security_id ON sec_equityoption(security_id);
CREATE INDEX ix_sec_fxforward_security_id ON sec_fxforward(security_id);