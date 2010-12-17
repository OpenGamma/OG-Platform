-- create-db-position.sql: Position Master

-- design has one document
--  position, trades and associated security ids
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

create sequence pos_master_seq as bigint
    start with 1000 increment by 1 no cycle;
create sequence pos_idkey_seq as bigint
    start with 1000 increment by 1 no cycle;
-- "as bigint" required by Derby, not accepted by Postgresql

create table pos_position (
    id bigint not null,
    oid bigint not null,
    ver_from_instant timestamp not null,
    ver_to_instant timestamp not null,
    corr_from_instant timestamp not null,
    corr_to_instant timestamp not null,
    quantity decimal(31,8) not null,
    primary key (id),
    constraint pos_fk_posi2posi foreign key (oid) references pos_position (id),
    constraint pos_chk_posi_ver_order check (ver_from_instant <= ver_to_instant),
    constraint pos_chk_posi_corr_order check (corr_from_instant <= corr_to_instant)
);

create table pos_trade (
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
    primary key (id),
    constraint pos_fk_trade2position foreign key (position_id) references pos_position (id)
);
-- position_oid is an optimization
-- pos_trade is fully dependent of pos_position

create table pos_idkey (
    id bigint not null,
    key_scheme varchar(255) not null,
    key_value varchar(255) not null,
    primary key (id),
    constraint pos_chk_idkey unique (key_scheme, key_value)
);

create table pos_position2idkey (
    position_id bigint not null,
    idkey_id bigint not null,
    constraint pos_fk_posidkey2pos foreign key (position_id) references pos_position (id),
    constraint pos_fk_posidkey2idkey foreign key (idkey_id) references pos_idkey (id)
);

create table pos_trade2idkey (
    trade_id bigint not null,
    idkey_id bigint not null,
    constraint pos_fk_tradeidkey2trade foreign key (trade_id) references pos_trade (id),
    constraint pos_fk_tradeidkey2idkey foreign key (idkey_id) references pos_idkey (id)
);
