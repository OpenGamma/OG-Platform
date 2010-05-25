
-- upgrade-db-position.sql: Position Master

drop table pos_nodeinclusion;
drop table pos_nodehierarchy;
drop table pos_identifierassociation;
drop table pos_position;
drop table pos_portfolio;
drop table pos_portfolionode;

create table pos_portfolio (
    oid int8 not null,
    version int8 not null,
    start_instant timestamp,
    end_instant timestamp,
    name varchar(255) not null,
    primary key (oid, version)
);

create table pos_node (
    portfolio_oid int8 not null,
    oid int8 not null,
    start_version int8 not null,
    end_version int8 not null,
    name varchar(255),
    primary key (oid, start_version)
);

create table pos_nodetree (
    node_oid int8 not null,
    start_version int8 not null,
    end_version int8 not null,
    left_id int8 not null,
    right_id int8 not null,
    primary key (node_oid, start_version)
);

create table pos_position (
    node_oid int8 not null,
    oid int8 not null,
    start_version int8 not null,
    end_version int8 not null,
    quantity decimal not null,
    primary key (oid, start_version)
);

create table pos_securitykey (
    position_oid int8 not null,
    start_version int8 not null,
    end_version int8 not null,
    id_scheme varchar(255) not null,
    id_value varchar(255) not null,
    primary key (position_oid, start_version)
);
