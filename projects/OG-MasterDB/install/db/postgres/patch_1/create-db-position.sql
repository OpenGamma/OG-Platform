-- create-db-position.sql: Position Master

-- design has two documents
--  portfolio and tree of nodes (nested set model)
--  position and associated security key
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

create sequence pos_master_seq
    start with 1000 increment by 1 no cycle;
create sequence pos_idkey_seq
    start with 1000 increment by 1 no cycle;
-- "as bigint" required by Derby, not accepted by Postgresql

create table pos_portfolio (
    id bigint not null,
    oid bigint not null,
    ver_from_instant timestamp not null,
    ver_to_instant timestamp not null,
    corr_from_instant timestamp not null,
    corr_to_instant timestamp not null,
    name varchar(255) not null,
    primary key (id),
    constraint pos_fk_port2port foreign key (oid) references pos_portfolio (id),
    constraint pos_chk_port_ver_order check (ver_from_instant <= ver_to_instant),
    constraint pos_chk_port_corr_order check (corr_from_instant <= corr_to_instant)
);

create table pos_node (
    id bigint not null,
    oid bigint not null,
    portfolio_id bigint not null,
    portfolio_oid bigint not null,
    parent_node_id bigint,
    depth int,
    tree_left bigint not null,
    tree_right bigint not null,
    name varchar(255),
    primary key (id),
    constraint pos_fk_node2node foreign key (oid) references pos_node (id),
    constraint pos_fk_node2portfolio foreign key (portfolio_id) references pos_portfolio (id),
    constraint pos_fk_node2parentnode foreign key (parent_node_id) references pos_node (id)
);
-- pos_node is fully dependent of pos_portfolio
-- portfolio_oid is an optimization (can be derived via portfolio_id)
-- parent_node_id is an optimization (tree_left/tree_right hold all the tree structure)
-- depth is an optimization (tree_left/tree_right hold all the tree structure)

create table pos_position (
    id bigint not null,
    oid bigint not null,
    portfolio_oid bigint not null,
    parent_node_oid bigint not null,
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
-- portfolio_oid is an optimization

create table pos_trade (
    id bigint not null,
    oid bigint not null,
    position_id bigint not null,
    position_oid bigint not null,
    quantity decimal(31,8) not null,
    trade_date date not null,
    trade_time time null,
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
