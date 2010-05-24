
-- upgrade-db-position.sql: Position Master

-- drop index pos_ix_nodehierarchy;
-- drop index pos_ix_nodeinclusion;
drop table pos_nodeinclusion;
drop table pos_nodehierarchy;
drop table pos_identifierassociation;
drop table pos_position;
drop table pos_portfolio;
drop table pos_portfolionode;

create table pos_portfolio (
    oid bigint not null,
    version bigint not null,
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
    primary key (oid, start_version)
);
--    constraint pos_fk_node2portfolio foreign key (portfolio_oid) references pos_portfolio (oid)

create table pos_nodetree (
    node_oid bigint not null,
    start_version bigint not null,
    end_version bigint not null,
    left_id bigint not null,
    right_id bigint not null,
    primary key (node_oid, start_version)
);
--    constraint pos_fk_tree2node foreign key (node_oid) references pos_node (oid)

create table pos_position (
    node_oid bigint not null,
    oid bigint not null,
    start_version bigint not null,
    end_version bigint not null,
    quantity decimal not null,
    primary key (oid, start_version)
);
--    constraint pos_fk_position2node foreign key (node_oid) references pos_node (oid)

create table pos_securitykey (
    position_oid bigint not null,
    start_version bigint not null,
    end_version bigint not null,
    id_scheme varchar(255) not null,
    id_value varchar(255) not null,
    primary key (position_oid, start_version)
);
--    constraint pos_fk_securitykey2position foreign key (position_oid) references pos_position (oid)
