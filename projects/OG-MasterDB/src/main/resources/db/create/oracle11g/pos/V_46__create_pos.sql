-- Position Master

-- design has one document
--  position, trades and associated security ids
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE TABLE pos_schema_version (
    version_key NVARCHAR2(32) NOT NULL,
    version_value NVARCHAR2(255) NOT NULL
);
INSERT INTO pos_schema_version (version_key, version_value) VALUES ('schema_patch', '46');

CREATE SEQUENCE pos_master_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE pos_idkey_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;

CREATE TABLE pos_position (
    id NUMBER(19) NOT NULL,
    oid NUMBER(19) NOT NULL,
    ver_from_instant TIMESTAMP NOT NULL,
    ver_to_instant TIMESTAMP NOT NULL,
    corr_from_instant TIMESTAMP NOT NULL,
    corr_to_instant TIMESTAMP NOT NULL,
    provider_scheme NVARCHAR2(255),
    provider_value NVARCHAR2(255),
    quantity NUMBER(31,8) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT pos_fk_posi2posi FOREIGN KEY (oid) REFERENCES pos_position (id),
    CONSTRAINT pos_chk_posi_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT pos_chk_posi_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_pos_position_oid ON pos_position(oid);
CREATE INDEX ix_pos_position_ver_from ON pos_position(ver_from_instant);
CREATE INDEX ix_pos_position_ver_to ON pos_position(ver_to_instant);
CREATE INDEX ix_pos_position_corr_from ON pos_position(corr_from_instant);
CREATE INDEX ix_pos_position_corr_to ON pos_position(corr_to_instant);
CREATE INDEX ix_pos_position_quantity ON pos_position(quantity);

CREATE TABLE pos_trade (
    id NUMBER(19) NOT NULL,
    oid NUMBER(19) NOT NULL,
    position_id NUMBER(19) NOT NULL,
    position_oid NUMBER(19) NOT NULL,
    quantity NUMBER(31,8) NOT NULL,
    trade_date TIMESTAMP NOT NULL,
    trade_time TIMESTAMP,
    zone_offset INT,
    cparty_scheme NVARCHAR2(255) NOT NULL,
    cparty_value NVARCHAR2(255) NOT NULL,
    provider_scheme NVARCHAR2(255),
    provider_value NVARCHAR2(255),
    premium_value DOUBLE PRECISION,
    premium_currency NVARCHAR2(255),
    premium_date TIMESTAMP,
    premium_time TIMESTAMP,
    premium_zone_offset INT,
    PRIMARY KEY (id),
    CONSTRAINT pos_fk_trade2position FOREIGN KEY (position_id) REFERENCES pos_position (id),
    CONSTRAINT pos_fk_tradei2tradei FOREIGN KEY (oid) REFERENCES pos_trade(id)
);
-- position_oid is an optimization
-- pos_trade is fully dependent of pos_position
CREATE INDEX ix_pos_trade_oid ON pos_trade(oid);
CREATE INDEX ix_pos_trade_position_id ON pos_trade(position_id);
CREATE INDEX ix_pos_trade_position_oid ON pos_trade(position_oid);

CREATE SEQUENCE pos_trade_attr_seq
    start with 1000 increment by 1 NOCYCLE;

CREATE TABLE pos_trade_attribute (
    id NUMBER(19) NOT NULL,
    trade_id NUMBER(19) NOT NULL,
    trade_oid NUMBER(19) NOT NULL,
    attr_key NVARCHAR2(255) NOT NULL,
    attr_value NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT pos_fk_tradeattr2trade FOREIGN KEY (trade_id) REFERENCES pos_trade (id),
    CONSTRAINT pos_chk_uq_trade_attribute UNIQUE (trade_id, attr_key, attr_value)
);
-- trade_oid is an optimization
-- pos_trade_attribute is fully dependent of pos_trade
CREATE INDEX ix_pos_trade_attr_trade_oid ON pos_trade_attribute(trade_oid);
CREATE INDEX ix_pos_trade_attr_key ON pos_trade_attribute(attr_key);

CREATE TABLE pos_attribute (
    id NUMBER(19) NOT NULL,
    position_id NUMBER(19) NOT NULL,
    position_oid NUMBER(19) NOT NULL,
    attr_key NVARCHAR2(255) NOT NULL,
    attr_value NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT pos_fk_posattr2pos FOREIGN KEY (position_id) REFERENCES pos_position (id),
    CONSTRAINT pos_chk_uq_pos_attribute UNIQUE (position_id, attr_key, attr_value)
);
-- position_oid is an optimization
-- pos_attribute is fully dependent of pos_position
CREATE INDEX ix_pos_attr_position_oid ON pos_attribute(position_oid);
CREATE INDEX ix_pos_attr_key ON pos_attribute(attr_key);

CREATE TABLE pos_idkey (
    id NUMBER(19) NOT NULL,
    key_scheme NVARCHAR2(255) NOT NULL,
    key_value NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT pos_chk_idkey UNIQUE (key_scheme, key_value)
);

CREATE TABLE pos_position2idkey (
    position_id NUMBER(19) NOT NULL,
    idkey_id NUMBER(19) NOT NULL,
    PRIMARY KEY (position_id, idkey_id),
    CONSTRAINT pos_fk_posidkey2pos FOREIGN KEY (position_id) REFERENCES pos_position (id),
    CONSTRAINT pos_fk_posidkey2idkey FOREIGN KEY (idkey_id) REFERENCES pos_idkey (id)
);
CREATE INDEX ix_pos_pos2idkey_idkey ON pos_position2idkey(idkey_id);

CREATE TABLE pos_trade2idkey (
    trade_id NUMBER(19) NOT NULL,
    idkey_id NUMBER(19) NOT NULL,
    PRIMARY KEY (trade_id, idkey_id),
    CONSTRAINT pos_fk_tradeidkey2trade FOREIGN KEY (trade_id) REFERENCES pos_trade (id),
    CONSTRAINT pos_fk_tradeidkey2idkey FOREIGN KEY (idkey_id) REFERENCES pos_idkey (id)
);
CREATE INDEX ix_pos_trd2idkey_idkey ON pos_trade2idkey(idkey_id);
