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

ALTER TABLE sec_security ADD COLUMN detail_type char;
BEGIN;
UPDATE sec_security SET detail_type = 'D';
ALTER TABLE sec_security ALTER COLUMN detail_type SET NOT NULL;
COMMIT;
ALTER TABLE sec_security ADD constraint sec_chk_detail_type check (detail_type in ('D', 'M', 'R'));

CREATE TABLE sec_raw (
    security_id bigint not null,
    raw_data bytea not null,
    constraint sec_fk_raw2sec foreign key (security_id) references sec_security (id)
);

