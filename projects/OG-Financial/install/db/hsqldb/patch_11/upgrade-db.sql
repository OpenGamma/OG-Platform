
create table pos_trade (
    id bigint not null,
    oid bigint not null,
    position_id bigint not null,
    quantity decimal(31,8) not null,
    trade_instant timestamp not null,
    cparty_scheme varchar(255) not null,
    cparty_value varchar(255) not null,
    primary key (id),
    constraint pos_fk_trade2position foreign key (position_id) references pos_position (id)
);