create table pos_portfolio (
    oid bigint not null,
    version bigint not null,
    status char(1) not null,
    start_instant timestamp,
    end_instant timestamp,
    name varchar(255) not null,
    primary key (oid, version)
);

create table pos_node (
    portfolio_oid bigint not null,
    oid bigint not null,
    start_version bigint not null,
    end_version bigint not null,
    name varchar(255),
    primary key (oid, start_version),
    constraint pos_fk_node2portfolio foreign key (portfolio_oid, start_version) references pos_portfolio (oid, version)
);

create table pos_nodetree (
    portfolio_oid bigint not null,
    parent_node_oid bigint,
    node_oid bigint not null,
    start_version bigint not null,
    end_version bigint not null,
    left_id bigint not null,
    right_id bigint not null,
    primary key (node_oid, start_version),
    constraint pos_fk_nodetree2portfolio foreign key (portfolio_oid, start_version) references pos_portfolio (oid, version)
);
-- portfolio_oid is an optimization
-- parent_node_oid is an optimization (left_id/right_id hold all the tree structure)

create table pos_position (
    portfolio_oid bigint not null,
    node_oid bigint not null,
    oid bigint not null,
    start_version bigint not null,
    end_version bigint not null,
    quantity decimal not null,
    primary key (oid, start_version),
    constraint pos_fk_position2portfolio foreign key (portfolio_oid, start_version) references pos_portfolio (oid, version)
);
-- portfolio_oid is an optimization

create table pos_securitykey (
    position_oid bigint not null,
    position_version bigint not null,
    id_scheme varchar(255) not null,
    id_value varchar(255) not null,
    primary key (position_oid, position_version, id_scheme, id_value),
    constraint pos_fk_securitykey2position foreign key (position_oid, position_version) references pos_position (oid, start_version)
);
-- pos_securitykey is fully dependent of pos_position
-- pos_securitykey.position_version = pos_position.start_version
