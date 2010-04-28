
-- new-db-position.sql: Position Master

create table pos_position (
    id bigint not null,
    identifier varchar(255),
    startDate date,
    endDate date,
    quantity decimal not null,
    counterparty varchar(255),
    trader varchar(255),
    primary key (id),
    unique (identifier, startDate, endDate)
);

create table pos_identifierassociation (
    id bigint not null,
    startDate date,
    endDate date,
    position_id bigint not null,
    scheme varchar(255) not null,
    identifier varchar(255) not null,
    primary key (id),
    constraint pos_fk_identifierassocation2position foreign key (position_id) references pos_position (id),
    unique (position_id, scheme, identifier, startDate, endDate)
);

create table pos_portfolionode (
    id bigint not null,
    identifier varchar(255),
    startDate date,
    endDate date,
    name varchar(255),
    ancestor_id bigint,
    primary key (id),
    constraint pos_fk_portfolionode2portfolionode foreign key (ancestor_id) references pos_portfolionode (id),
    unique (identifier, startDate, endDate)
);

create table pos_nodehierarchy (
    ancestor_id bigint not null,
    descendant_id bigint not null,
    primary key (ancestor_id, descendant_id),
    constraint pos_fk_nodehierarchy2portfolionode1 foreign key (ancestor_id) references pos_portfolionode (id),
    constraint pos_fk_nodehierarchy2portfolionode2 foreign key (descendant_id) references pos_portfolionode (id)
);
create index pos_ix_nodehierarchy on pos_nodehierarchy (descendant_id);

create table pos_nodeinclusion (
    position_id bigint not null,
    node_id bigint not null,
    primary key (position_id, node_id),
    constraint fk_nodeinclusion2position foreign key (position_id) references pos_position (id),
    constraint fk_nodeinclusion2portfolionode foreign key (node_id) references pos_portfolionode (id)
);
create index pos_ix_nodeinclusion on pos_nodeinclusion (node_id);

create table pos_portfolio (
    id bigint not null,
    identifier varchar(255),
    startDate date,
    endDate date,
    name varchar(255) not null,
    root_id bigint not null,
    primary key (id),
    constraint fk_portfolio2portfolionode foreign key (root_id) references pos_portfolionode (id),
    unique (identifier, startDate, endDate)
);
