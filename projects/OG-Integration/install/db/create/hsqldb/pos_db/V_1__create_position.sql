-- create-db-position.sql: Position Master

-- design has one document
--  position, trades and associated security ids
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE SEQUENCE pos_master_seq as bigint
    start with 1000 increment by 1 no cycle;
CREATE SEQUENCE pos_idkey_seq as bigint
    start with 1000 increment by 1 no cycle;
-- "as bigint" required by Derby, not accepted by Postgresql

CREATE TABLE pos_position (
    id bigint not null,
    oid bigint not null,
    ver_from_instant timestamp not null,
    ver_to_instant timestamp not null,
    corr_from_instant timestamp not null,
    corr_to_instant timestamp not null,
    provider_scheme varchar(255),
    provider_value varchar(255),
    quantity decimal(31,8) not null,
    primary key (id),
    constraint pos_fk_posi2posi foreign key (oid) references pos_position (id),
    constraint pos_chk_posi_ver_order check (ver_from_instant <= ver_to_instant),
    constraint pos_chk_posi_corr_order check (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_pos_position_oid ON pos_position(oid);
CREATE INDEX ix_pos_position_ver_from_instant ON pos_position(ver_from_instant);
CREATE INDEX ix_pos_position_ver_to_instant ON pos_position(ver_to_instant);
CREATE INDEX ix_pos_position_corr_from_instant ON pos_position(corr_from_instant);
CREATE INDEX ix_pos_position_corr_to_instant ON pos_position(corr_to_instant);
CREATE INDEX ix_pos_position_quantity ON pos_position(quantity);

CREATE TABLE pos_trade (
    id bigint not null,
    oid bigint not null,
    position_id bigint not null,
    position_oid bigint not null,
    quantity decimal(31,8) not null,
    trade_date date not null,
    trade_time time(6) null,
    zone_offset int null,
    cparty_scheme varchar(255) not null,
    cparty_value varchar(255) not null,
    provider_scheme varchar(255),
    provider_value varchar(255),
    premium_value double precision,
    premium_currency varchar(255),
    premium_date date,
    premium_time time(6),
    premium_zone_offset int,
    primary key (id),
    constraint pos_fk_trade2position foreign key (position_id) references pos_position (id)
);
-- position_oid is an optimization
-- pos_trade is fully dependent of pos_position
CREATE INDEX ix_pos_trade_oid ON pos_trade(oid);
CREATE INDEX ix_pos_trade_position_id ON pos_trade(position_id);
CREATE INDEX ix_pos_trade_position_oid ON pos_trade(position_oid);

CREATE SEQUENCE pos_trade_attr_seq as bigint
    start with 1000 increment by 1 no cycle;

CREATE TABLE pos_trade_attribute (
    id bigint not null,
    trade_id bigint not null,
    trade_oid bigint not null,
    key varchar(255) not null,
    value varchar(255) not null,
    primary key (id),
    constraint pos_fk_tradeattr2trade foreign key (trade_id) references pos_trade (id),
    constraint pos_chk_uq_trade_attribute unique (trade_id, key, value)
);
-- trade_oid is an optimization
-- pos_trade_attribute is fully dependent of pos_trade
CREATE INDEX ix_pos_trade_attr_trade_oid ON pos_trade_attribute(trade_oid);
CREATE INDEX ix_pos_trade_attr_key ON pos_trade_attribute(key);

CREATE TABLE pos_attribute (
    id bigint not null,
    position_id bigint not null,
    position_oid bigint not null,
    key varchar(255) not null,
    value varchar(255) not null,
    primary key (id),
    constraint pos_fk_posattr2pos foreign key (position_id) references pos_position (id),
    constraint pos_chk_uq_pos_attribute unique (position_id, key, value)
);
-- position_oid is an optimization
-- pos_attribute is fully dependent of pos_position
CREATE INDEX ix_pos_attr_position_oid ON pos_attribute(position_oid);
CREATE INDEX ix_pos_attr_key ON pos_attribute(key);

CREATE TABLE pos_idkey (
    id bigint not null,
    key_scheme varchar(255) not null,
    key_value varchar(255) not null,
    primary key (id),
    constraint pos_chk_idkey unique (key_scheme, key_value)
);

CREATE TABLE pos_position2idkey (
    position_id bigint not null,
    idkey_id bigint not null,
    primary key (position_id, idkey_id),
    constraint pos_fk_posidkey2pos foreign key (position_id) references pos_position (id),
    constraint pos_fk_posidkey2idkey foreign key (idkey_id) references pos_idkey (id)
);

CREATE TABLE pos_trade2idkey (
    trade_id bigint not null,
    idkey_id bigint not null,
    primary key (trade_id, idkey_id),
    constraint pos_fk_tradeidkey2trade foreign key (trade_id) references pos_trade (id),
    constraint pos_fk_tradeidkey2idkey foreign key (idkey_id) references pos_idkey (id)
);