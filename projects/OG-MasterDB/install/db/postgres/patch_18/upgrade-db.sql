CREATE TABLE pos_attribute (
    id bigint not null,
    position_id bigint not null,
    position_oid bigint not null,
    key varchar(255) not null,
    value varchar(255) not null,
    primary key (id),
    constraint pos_fk_posattr2pos foreign key (position_id) references pos_position (id),
    constraint pos_chk_uq_pos_attribute unique (position_id, key, value)
);
-- position_oid is an optimization
-- pos_attribute is fully dependent of pos_position
CREATE INDEX ix_pos_attr_position_oid ON pos_attribute(position_oid);
CREATE INDEX ix_pos_attr_key ON pos_attribute(key);