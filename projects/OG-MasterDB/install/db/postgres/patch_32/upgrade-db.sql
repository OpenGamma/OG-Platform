CREATE SEQUENCE prt_portfolio_attr_seq
    start with 1000 increment by 1 no cycle;

CREATE TABLE prt_portfolio_attribute (
    id bigint not null,
    portfolio_id bigint not null,
    portfolio_oid bigint not null,
    key varchar(255) not null,
    value varchar(255) not null,
    primary key (id),
    constraint prt_fk_prtattr2portfolio foreign key (portfolio_id) references prt_portfolio (id),
    constraint prt_chk_uq_prt_attribute unique (portfolio_id, key, value)
);
-- portfolio_oid is an optimization
-- prt_portfolio_attribute is fully dependent of prt_portfolio
CREATE INDEX ix_prt_attr_portfolio_oid ON prt_portfolio_attribute(portfolio_oid);
CREATE INDEX ix_prt_attr_key ON prt_portfolio_attribute(key);