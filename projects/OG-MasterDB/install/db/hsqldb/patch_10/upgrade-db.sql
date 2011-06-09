CREATE SEQUENCE pos_trade_attr_seq as bigint
    start with 1000 increment by 1 no cycle;

CREATE TABLE pos_trade_attribute (
    id bigint not null,
    trade_id bigint not null,
    trade_oid bigint not null,
    key varchar(255) not null,
    value varchar(255) not null,
    primary key (id),
    constraint pos_fk_tradeattr2trade foreign key (trade_id) references pos_trade (id),
    constraint pos_chk_uq_trade_attribute unique (trade_id, key, value)
);
-- trade_oid is an optimization
-- pos_trade_attribute is fully dependent of pos_trade
CREATE INDEX ix_pos_trade_attr_trade_oid ON pos_trade_attribute(trade_oid);
CREATE INDEX ix_pos_trade_attr_key ON pos_trade_attribute(key);