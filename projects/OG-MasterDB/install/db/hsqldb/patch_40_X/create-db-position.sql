-- create-db-position.sql: Position Master

-- design has one document
--  position, trades and associated security ids
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE SEQUENCE pos_master_seq AS bigint
    START WITH 1000 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE pos_idkey_seq AS bigint
    START WITH 1000 INCREMENT BY 1 NO CYCLE;
-- "as bigint" required by Derby, not accepted by Postgresql

CREATE TABLE pos_position (
    id bigint NOT NULL,
    oid bigint NOT NULL,
    ver_from_instant timestamp without time zone NOT NULL,
    ver_to_instant timestamp without time zone NOT NULL,
    corr_from_instant timestamp without time zone NOT NULL,
    corr_to_instant timestamp without time zone NOT NULL,
    provider_scheme varchar(255),
    provider_value varchar(255),
    quantity decimal(31,8) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT pos_fk_posi2posi FOREIGN KEY (oid) REFERENCES pos_position (id),
    CONSTRAINT pos_chk_posi_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT pos_chk_posi_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_pos_position_oid ON pos_position(oid);
CREATE INDEX ix_pos_position_ver_from_instant ON pos_position(ver_from_instant);
CREATE INDEX ix_pos_position_ver_to_instant ON pos_position(ver_to_instant);
CREATE INDEX ix_pos_position_corr_from_instant ON pos_position(corr_from_instant);
CREATE INDEX ix_pos_position_corr_to_instant ON pos_position(corr_to_instant);
CREATE INDEX ix_pos_position_quantity ON pos_position(quantity);

CREATE TABLE pos_trade (
    id bigint NOT NULL,
    oid bigint NOT NULL,
    position_id bigint NOT NULL,
    position_oid bigint NOT NULL,
    quantity decimal(31,8) NOT NULL,
    trade_date date NOT NULL,
    trade_time time(6) null,
    zone_offset int null,
    cparty_scheme varchar(255) NOT NULL,
    cparty_value varchar(255) NOT NULL,
    provider_scheme varchar(255),
    provider_value varchar(255),
    premium_value double precision,
    premium_currency varchar(255),
    premium_date date,
    premium_time time(6),
    premium_zone_offset int,
    PRIMARY KEY (id),
    CONSTRAINT pos_fk_trade2position FOREIGN KEY (position_id) REFERENCES pos_position (id),
    CONSTRAINT pos_fk_tradei2tradei FOREIGN KEY (oid) REFERENCES pos_trade(id)
);
-- position_oid is an optimization
-- pos_trade is fully dependent of pos_position
CREATE INDEX ix_pos_trade_oid ON pos_trade(oid);
CREATE INDEX ix_pos_trade_position_id ON pos_trade(position_id);
CREATE INDEX ix_pos_trade_position_oid ON pos_trade(position_oid);

CREATE SEQUENCE pos_trade_attr_seq as bigint
    start with 1000 increment by 1 no cycle;

CREATE TABLE pos_trade_attribute (
    id bigint NOT NULL,
    trade_id bigint NOT NULL,
    trade_oid bigint NOT NULL,
    key varchar(255) NOT NULL,
    value varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT pos_fk_tradeattr2trade FOREIGN KEY (trade_id) REFERENCES pos_trade (id),
    CONSTRAINT pos_chk_uq_trade_attribute UNIQUE (trade_id, key, value)
);
-- trade_oid is an optimization
-- pos_trade_attribute is fully dependent of pos_trade
CREATE INDEX ix_pos_trade_attr_trade_oid ON pos_trade_attribute(trade_oid);
CREATE INDEX ix_pos_trade_attr_key ON pos_trade_attribute(key);

CREATE TABLE pos_attribute (
    id bigint NOT NULL,
    position_id bigint NOT NULL,
    position_oid bigint NOT NULL,
    key varchar(255) NOT NULL,
    value varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT pos_fk_posattr2pos FOREIGN KEY (position_id) REFERENCES pos_position (id),
    CONSTRAINT pos_chk_uq_pos_attribute UNIQUE (position_id, key, value)
);
-- position_oid is an optimization
-- pos_attribute is fully dependent of pos_position
CREATE INDEX ix_pos_attr_position_oid ON pos_attribute(position_oid);
CREATE INDEX ix_pos_attr_key ON pos_attribute(key);

CREATE TABLE pos_idkey (
    id bigint NOT NULL,
    key_scheme varchar(255) NOT NULL,
    key_value varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT pos_chk_idkey UNIQUE (key_scheme, key_value)
);

CREATE TABLE pos_position2idkey (
    position_id bigint NOT NULL,
    idkey_id bigint NOT NULL,
    PRIMARY KEY (position_id, idkey_id),
    CONSTRAINT pos_fk_posidkey2pos FOREIGN KEY (position_id) REFERENCES pos_position (id),
    CONSTRAINT pos_fk_posidkey2idkey FOREIGN KEY (idkey_id) REFERENCES pos_idkey (id)
);
CREATE INDEX ix_pos_pos2idkey_idkey ON pos_position2idkey(idkey_id);

CREATE TABLE pos_trade2idkey (
    trade_id bigint NOT NULL,
    idkey_id bigint NOT NULL,
    PRIMARY KEY (trade_id, idkey_id),
    CONSTRAINT pos_fk_tradeidkey2trade FOREIGN KEY (trade_id) REFERENCES pos_trade (id),
    CONSTRAINT pos_fk_tradeidkey2idkey FOREIGN KEY (idkey_id) REFERENCES pos_idkey (id)
);
CREATE INDEX ix_pos_trd2idkey_idkey ON pos_trade2idkey(idkey_id);
