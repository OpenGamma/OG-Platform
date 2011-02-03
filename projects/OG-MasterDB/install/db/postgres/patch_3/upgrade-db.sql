
DROP INDEX IF EXISTS ix_cfg_config_oid;
DROP INDEX IF EXISTS ix_cfg_config_ver_from_instant;
DROP INDEX IF EXISTS ix_cfg_config_ver_to_instant;
DROP INDEX IF EXISTS ix_cfg_config_corr_from_instant;
DROP INDEX IF EXISTS ix_cfg_config_corr_to_instant;
DROP INDEX IF EXISTS ix_cfg_config_name;
DROP INDEX IF EXISTS ix_cfg_config_nameu;
DROP INDEX IF EXISTS ix_cfg_config_config_type;

CREATE INDEX ix_cfg_config_oid ON cfg_config(oid);
CREATE INDEX ix_cfg_config_ver_from_instant ON cfg_config(ver_from_instant);
CREATE INDEX ix_cfg_config_ver_to_instant ON cfg_config(ver_to_instant);
CREATE INDEX ix_cfg_config_corr_from_instant ON cfg_config(corr_from_instant);
CREATE INDEX ix_cfg_config_corr_to_instant ON cfg_config(corr_to_instant);
CREATE INDEX ix_cfg_config_name ON cfg_config(name);
CREATE INDEX ix_cfg_config_nameu ON cfg_config(upper(name));
CREATE INDEX ix_cfg_config_config_type ON cfg_config(config_type);


DROP INDEX IF EXISTS ix_prt_portfolio_oid;
DROP INDEX IF EXISTS ix_prt_portfolio_ver_from_instant;
DROP INDEX IF EXISTS ix_prt_portfolio_ver_to_instant;
DROP INDEX IF EXISTS ix_prt_portfolio_corr_from_instant;
DROP INDEX IF EXISTS ix_prt_portfolio_corr_to_instant;
DROP INDEX IF EXISTS ix_prt_portfolio_name;
DROP INDEX IF EXISTS ix_prt_portfolio_nameu;
DROP INDEX IF EXISTS ix_prt_node_oid;
DROP INDEX IF EXISTS ix_prt_node_portfolio_id;
DROP INDEX IF EXISTS ix_prt_node_portfolio_oid;
DROP INDEX IF EXISTS ix_prt_node_parent_node_id;
DROP INDEX IF EXISTS ix_prt_node_parent_node_oid;
DROP INDEX IF EXISTS ix_prt_node_depth;
DROP INDEX IF EXISTS ix_prt_position_node_id;

CREATE INDEX ix_prt_portfolio_oid ON prt_portfolio(oid);
CREATE INDEX ix_prt_portfolio_ver_from_instant ON prt_portfolio(ver_from_instant);
CREATE INDEX ix_prt_portfolio_ver_to_instant ON prt_portfolio(ver_to_instant);
CREATE INDEX ix_prt_portfolio_corr_from_instant ON prt_portfolio(corr_from_instant);
CREATE INDEX ix_prt_portfolio_corr_to_instant ON prt_portfolio(corr_to_instant);
CREATE INDEX ix_prt_portfolio_name ON prt_portfolio(name);
CREATE INDEX ix_prt_portfolio_nameu ON prt_portfolio(upper(name));
CREATE INDEX ix_prt_node_oid ON prt_node(oid);
CREATE INDEX ix_prt_node_portfolio_id ON prt_node(portfolio_id);
CREATE INDEX ix_prt_node_portfolio_oid ON prt_node(portfolio_oid);
CREATE INDEX ix_prt_node_parent_node_id ON prt_node(parent_node_id);
CREATE INDEX ix_prt_node_parent_node_oid ON prt_node(parent_node_oid);
CREATE INDEX ix_prt_node_depth ON prt_node(depth);
CREATE INDEX ix_prt_position_node_id ON prt_position(node_id);


DROP INDEX IF EXISTS ix_pos_position_oid;
DROP INDEX IF EXISTS ix_pos_position_ver_from_instant;
DROP INDEX IF EXISTS ix_pos_position_ver_to_instant;
DROP INDEX IF EXISTS ix_pos_position_corr_from_instant;
DROP INDEX IF EXISTS ix_pos_position_corr_to_instant;
DROP INDEX IF EXISTS ix_pos_position_quantity;
DROP INDEX IF EXISTS ix_pos_trade_oid;
DROP INDEX IF EXISTS ix_pos_trade_position_id;
DROP INDEX IF EXISTS ix_pos_trade_position_oid;

CREATE INDEX ix_pos_position_oid ON pos_position(oid);
CREATE INDEX ix_pos_position_ver_from_instant ON pos_position(ver_from_instant);
CREATE INDEX ix_pos_position_ver_to_instant ON pos_position(ver_to_instant);
CREATE INDEX ix_pos_position_corr_from_instant ON pos_position(corr_from_instant);
CREATE INDEX ix_pos_position_corr_to_instant ON pos_position(corr_to_instant);
CREATE INDEX ix_pos_position_quantity ON pos_position(quantity);
CREATE INDEX ix_pos_trade_oid ON pos_trade(oid);
CREATE INDEX ix_pos_trade_position_id ON pos_trade(position_id);
CREATE INDEX ix_pos_trade_position_oid ON pos_trade(position_oid);
ALTER TABLE pos_position2idkey ADD PRIMARY KEY (position_id, idkey_id);
ALTER TABLE pos_trade2idkey ADD PRIMARY KEY (trade_id, idkey_id);


DROP INDEX IF EXISTS ix_sec_security_oid;
DROP INDEX IF EXISTS ix_sec_security_ver_from_instant;
DROP INDEX IF EXISTS ix_sec_security_ver_to_instant;
DROP INDEX IF EXISTS ix_sec_security_corr_from_instant;
DROP INDEX IF EXISTS ix_sec_security_corr_to_instant;
DROP INDEX IF EXISTS ix_sec_security_name;
DROP INDEX IF EXISTS ix_sec_security_nameu;
DROP INDEX IF EXISTS ix_sec_security_sec_type;

CREATE INDEX ix_sec_security_oid ON sec_security(oid);
CREATE INDEX ix_sec_security_ver_from_instant ON sec_security(ver_from_instant);
CREATE INDEX ix_sec_security_ver_to_instant ON sec_security(ver_to_instant);
CREATE INDEX ix_sec_security_corr_from_instant ON sec_security(corr_from_instant);
CREATE INDEX ix_sec_security_corr_to_instant ON sec_security(corr_to_instant);
CREATE INDEX ix_sec_security_name ON sec_security(name);
CREATE INDEX ix_sec_security_nameu ON sec_security(upper(name));
CREATE INDEX ix_sec_security_sec_type ON sec_security(sec_type);
ALTER TABLE sec_security2idkey ADD PRIMARY KEY (security_id, idkey_id);


DROP INDEX IF EXISTS ix_hol_holiday_oid;
DROP INDEX IF EXISTS ix_hol_holiday_ver_from_instant;
DROP INDEX IF EXISTS ix_hol_holiday_ver_to_instant;
DROP INDEX IF EXISTS ix_hol_holiday_corr_from_instant;
DROP INDEX IF EXISTS ix_hol_holiday_corr_to_instant;
DROP INDEX IF EXISTS ix_hol_holiday_name;
DROP INDEX IF EXISTS ix_hol_holiday_nameu;
DROP INDEX IF EXISTS ix_hol_holiday_provider_scheme;
DROP INDEX IF EXISTS ix_hol_holiday_provider_value;
DROP INDEX IF EXISTS ix_hol_holiday_holiday_type;
DROP INDEX IF EXISTS ix_hol_holiday_region_scheme;
DROP INDEX IF EXISTS ix_hol_holiday_region_value;
DROP INDEX IF EXISTS ix_hol_holiday_exchange_scheme;
DROP INDEX IF EXISTS ix_hol_holiday_exchange_value;
DROP INDEX IF EXISTS ix_hol_holiday_currency_iso;
DROP INDEX IF EXISTS ix_hol_date_holiday_id;

CREATE INDEX ix_hol_holiday_oid ON hol_holiday(oid);
CREATE INDEX ix_hol_holiday_ver_from_instant ON hol_holiday(ver_from_instant);
CREATE INDEX ix_hol_holiday_ver_to_instant ON hol_holiday(ver_to_instant);
CREATE INDEX ix_hol_holiday_corr_from_instant ON hol_holiday(corr_from_instant);
CREATE INDEX ix_hol_holiday_corr_to_instant ON hol_holiday(corr_to_instant);
CREATE INDEX ix_hol_holiday_name ON hol_holiday(name);
CREATE INDEX ix_hol_holiday_nameu ON hol_holiday(upper(name));
CREATE INDEX ix_hol_holiday_provider_scheme ON hol_holiday(provider_scheme);
CREATE INDEX ix_hol_holiday_provider_value ON hol_holiday(provider_value);
CREATE INDEX ix_hol_holiday_holiday_type ON hol_holiday(hol_type);
CREATE INDEX ix_hol_holiday_region_scheme ON hol_holiday(region_scheme);
CREATE INDEX ix_hol_holiday_region_value ON hol_holiday(region_value);
CREATE INDEX ix_hol_holiday_exchange_scheme ON hol_holiday(exchange_scheme);
CREATE INDEX ix_hol_holiday_exchange_value ON hol_holiday(exchange_value);
CREATE INDEX ix_hol_holiday_currency_iso ON hol_holiday(currency_iso);
CREATE INDEX ix_hol_date_holiday_id ON hol_date(holiday_id);


DROP INDEX IF EXISTS ix_exg_exchange_oid;
DROP INDEX IF EXISTS ix_exg_exchange_ver_from_instant;
DROP INDEX IF EXISTS ix_exg_exchange_ver_to_instant;
DROP INDEX IF EXISTS ix_exg_exchange_corr_from_instant;
DROP INDEX IF EXISTS ix_exg_exchange_corr_to_instant;
DROP INDEX IF EXISTS ix_exg_exchange_name;
DROP INDEX IF EXISTS ix_exg_exchange_nameu;

CREATE INDEX ix_exg_exchange_oid ON exg_exchange(oid);
CREATE INDEX ix_exg_exchange_ver_from_instant ON exg_exchange(ver_from_instant);
CREATE INDEX ix_exg_exchange_ver_to_instant ON exg_exchange(ver_to_instant);
CREATE INDEX ix_exg_exchange_corr_from_instant ON exg_exchange(corr_from_instant);
CREATE INDEX ix_exg_exchange_corr_to_instant ON exg_exchange(corr_to_instant);
CREATE INDEX ix_exg_exchange_name ON exg_exchange(name);
CREATE INDEX ix_exg_exchange_nameu ON exg_exchange(upper(name));
