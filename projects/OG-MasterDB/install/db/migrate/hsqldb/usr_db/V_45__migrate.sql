-- create-db-user.sql : User Master

-- User has one primary table: usr_oguser.
-- Bitemporal versioning exists at the document level

CREATE TABLE usr_schema_version (
    version_key VARCHAR(32) NOT NULL,
    version_value VARCHAR(255) NOT NULL
);
INSERT INTO usr_schema_version (version_key, version_value) VALUES ('schema_patch', '45');

CREATE SEQUENCE usr_oguser_seq AS bigint
    START WITH 1000 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE usr_idkey_seq as bigint
    START WITH 1000 INCREMENT BY 1 NO CYCLE;

CREATE TABLE usr_oguser (
    id bigint NOT NULL,
    oid bigint NOT NULL,
    ver_from_instant timestamp without time zone NOT NULL,
    ver_to_instant timestamp without time zone NOT NULL,
    corr_from_instant timestamp without time zone NOT NULL,
    corr_to_instant timestamp without time zone NOT NULL,
    name varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT usr_chk_oguser_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT usr_chk_oguser_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_usr_oguser_oid ON usr_oguser(oid);
CREATE INDEX ix_usr_oguser_ver_from_instant ON usr_oguser(ver_from_instant);
CREATE INDEX ix_usr_oguser_ver_to_instant ON usr_oguser(ver_to_instant);
CREATE INDEX ix_usr_oguser_corr_from_instant ON usr_oguser(corr_from_instant);
CREATE INDEX ix_usr_oguser_corr_to_instant ON usr_oguser(corr_to_instant);
CREATE INDEX ix_usr_oguser_name ON usr_oguser(name);

CREATE TABLE usr_idkey (
    id bigint NOT NULL,
    key_scheme varchar(255) NOT NULL,
    key_value varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT usr_chk_idkey UNIQUE (key_scheme, key_value)
);

CREATE TABLE usr_oguser2idkey (
    oguser_id bigint NOT NULL,
    idkey_id bigint NOT NULL,
    PRIMARY KEY (oguser_id, idkey_id),
    CONSTRAINT usr_fk_oguseridkey2exg FOREIGN KEY (oguser_id) REFERENCES usr_oguser (id) ON DELETE CASCADE,
    CONSTRAINT usr_fk_oguseridkey2idkey FOREIGN KEY (idkey_id) REFERENCES usr_idkey (id) ON DELETE CASCADE
);
CREATE INDEX ix_usr_oguser2idkey_idkey ON usr_oguser2idkey(idkey_id);

CREATE TABLE usr_entitlement (
    oguser_id bigint NOT NULL,
    entitlement_index int NOT NULL,
    entitlement_pattern varchar(255) NOT NULL,
    PRIMARY KEY (oguser_id, entitlement_index),
    CONSTRAINT usr_fk_entit2oguser FOREIGN KEY (oguser_id) REFERENCES usr_oguser (id) ON DELETE CASCADE
);
