CREATE SEQUENCE sec_security_attr_seq
    start with 1000 increment by 1 no cycle;

CREATE TABLE sec_security_attribute (
    id bigint not null,
    security_id bigint not null,
    security_oid bigint not null,
    key varchar(255) not null,
    value varchar(255) not null,
    primary key (id),
    constraint sec_fk_securityattr2security foreign key (security_id) references sec_security (id),
    constraint sec_chk_uq_security_attribute unique (security_id, key, value)
);
-- security_oid is an optimization
-- sec_security_attribute is fully dependent of sec_security
CREATE INDEX ix_sec_security_attr_security_oid ON sec_security_attribute(security_oid);
CREATE INDEX ix_sec_security_attr_key ON sec_security_attribute(key);