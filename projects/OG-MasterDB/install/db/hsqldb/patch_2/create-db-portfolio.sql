-- create-db-portfolio.sql: Portfolio Master

-- design has one document
--  portfolio, tree of nodes (nested set model) and position ids
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

create sequence prt_master_seq as bigint
    start with 1000 increment by 1 no cycle;
-- "as bigint" required by Derby, not accepted by Postgresql

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
-- prt_node is fully dependent of prt_portfolio
-- portfolio_oid is an optimization (can be derived via portfolio_id)
-- parent_node_id is an optimization (tree_left/tree_right hold all the tree structure)
-- depth is an optimization (tree_left/tree_right hold all the tree structure)

create table prt_position (
    node_id bigint not null,
    key_scheme varchar(255) not null,
    key_value varchar(255) not null,
    constraint prt_fk_pos2node foreign key (node_id) references prt_node (id)
);
-- prt_position is fully dependent of prt_portfolio
