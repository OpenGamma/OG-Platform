
DROP SEQUENCE IF EXISTS pos_master_seq;
DROP SEQUENCE IF EXISTS pos_idkey_seq;
DROP TABLE IF EXISTS pos_trade2idkey;
DROP TABLE IF EXISTS pos_position2idkey;
DROP TABLE IF EXISTS pos_idkey;
DROP TABLE IF EXISTS pos_trade;
DROP TABLE IF EXISTS pos_position;
DROP TABLE IF EXISTS pos_node;
DROP TABLE IF EXISTS pos_portfolio;

create sequence prt_master_seq as bigint
    start with 1000 increment by 1 no cycle;

create table prt_portfolio (
    id bigint not null,
    oid bigint not null,
    ver_from_instant timestamp not null,
    ver_to_instant timestamp not null,
    corr_from_instant timestamp not null,
    corr_to_instant timestamp not null,
    name varchar(255) not null,
    primary key (id),
    constraint prt_fk_port2port foreign key (oid) references prt_portfolio (id),
    constraint prt_chk_port_ver_order check (ver_from_instant <= ver_to_instant),
    constraint prt_chk_port_corr_order check (corr_from_instant <= corr_to_instant)
);

create table prt_node (
    id bigint not null,
    oid bigint not null,
    portfolio_id bigint not null,
    portfolio_oid bigint not null,
    parent_node_id bigint,
    parent_node_oid bigint,
    depth int,
    tree_left bigint not null,
    tree_right bigint not null,
    name varchar(255),
    primary key (id),
    constraint prt_fk_node2node foreign key (oid) references prt_node (id),
    constraint prt_fk_node2portfolio foreign key (portfolio_id) references prt_portfolio (id),
    constraint prt_fk_node2parentnode foreign key (parent_node_id) references prt_node (id)
);

create table prt_position (
    node_id bigint not null,
    key_scheme varchar(255) not null,
    key_value varchar(255) not null,
    constraint prt_fk_pos2node foreign key (node_id) references prt_node (id)
);

create sequence pos_master_seq as bigint
    start with 1000 increment by 1 no cycle;
create sequence pos_idkey_seq as bigint
    start with 1000 increment by 1 no cycle;

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
