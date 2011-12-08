-- create-db-portfolio.sql: Portfolio Master

-- design has one document
--  portfolio, tree of nodes (nested set model) and position ids
-- bitemporal versioning exists at the document level
-- each time a document is changed, a new row is written
-- with only the end instant being changed on the old row

CREATE SEQUENCE prt_master_seq
    START WITH 1000 INCREMENT BY 1 NO CYCLE;
-- "as bigint" required by Derby, not accepted by Postgresql

CREATE TABLE prt_portfolio (
    id bigint NOT NULL,
    oid bigint NOT NULL,
    ver_from_instant timestamp without time zone NOT NULL,
    ver_to_instant timestamp without time zone NOT NULL,
    corr_from_instant timestamp without time zone NOT NULL,
    corr_to_instant timestamp without time zone NOT NULL,
    name varchar(255) NOT NULL,
    visibility smallint NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT prt_fk_port2port FOREIGN KEY (oid) REFERENCES prt_portfolio (id),
    CONSTRAINT prt_chk_port_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT prt_chk_port_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_prt_portfolio_oid ON prt_portfolio(oid);
CREATE INDEX ix_prt_portfolio_ver_from_instant ON prt_portfolio(ver_from_instant);
CREATE INDEX ix_prt_portfolio_ver_to_instant ON prt_portfolio(ver_to_instant);
CREATE INDEX ix_prt_portfolio_corr_from_instant ON prt_portfolio(corr_from_instant);
CREATE INDEX ix_prt_portfolio_corr_to_instant ON prt_portfolio(corr_to_instant);
CREATE INDEX ix_prt_portfolio_name ON prt_portfolio(name);
CREATE INDEX ix_prt_portfolio_nameu ON prt_portfolio(UPPER(name));
CREATE INDEX ix_prt_portfolio_visibility ON prt_portfolio(visibility);

CREATE TABLE prt_node (
    id bigint NOT NULL,
    oid bigint NOT NULL,
    portfolio_id bigint NOT NULL,
    portfolio_oid bigint NOT NULL,
    parent_node_id bigint,
    parent_node_oid bigint,
    depth int,
    tree_left bigint NOT NULL,
    tree_right bigint NOT NULL,
    name varchar(255),
    PRIMARY KEY (id),
    CONSTRAINT prt_fk_node2node FOREIGN KEY (oid) REFERENCES prt_node (id),
    CONSTRAINT prt_fk_node2portfolio FOREIGN KEY (portfolio_id) REFERENCES prt_portfolio (id),
    CONSTRAINT prt_fk_node2parentnode FOREIGN KEY (parent_node_id) REFERENCES prt_node (id)
);
-- prt_node is fully dependent of prt_portfolio
-- portfolio_oid is an optimization (can be derived via portfolio_id)
-- parent_node_id is an optimization (tree_left/tree_right hold all the tree structure)
-- depth is an optimization (tree_left/tree_right hold all the tree structure)
CREATE INDEX ix_prt_node_oid ON prt_node(oid);
CREATE INDEX ix_prt_node_portfolio_id ON prt_node(portfolio_id);
CREATE INDEX ix_prt_node_portfolio_oid ON prt_node(portfolio_oid);
CREATE INDEX ix_prt_node_parent_node_id ON prt_node(parent_node_id);
CREATE INDEX ix_prt_node_parent_node_oid ON prt_node(parent_node_oid);
CREATE INDEX ix_prt_node_depth ON prt_node(depth);

CREATE TABLE prt_position (
    node_id bigint NOT NULL,
    key_scheme varchar(255) NOT NULL,
    key_value varchar(255) NOT NULL,
    CONSTRAINT prt_fk_pos2node FOREIGN KEY (node_id) REFERENCES prt_node (id)
);
-- prt_position is fully dependent of prt_portfolio
CREATE INDEX ix_prt_position_node_id ON prt_position(node_id);

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
