
<<<<<<< HEAD
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
=======
drop sequence pos_master_seq;
drop table pos_securitykey;
drop table pos_position;
drop table pos_node;
drop table pos_portfolio;
>>>>>>> 3d89945a8edfe752de5093c51ec1c03c3a324c3d
