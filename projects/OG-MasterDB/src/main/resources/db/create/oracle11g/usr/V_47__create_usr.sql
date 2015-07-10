-- create-db-user.sql : User Master

-- User has one primary table: usr_oguser.
-- Bitemporal versioning exists at the document level

CREATE TABLE usr_schema_version (
    version_key NVARCHAR2(32) NOT NULL,
    version_value NVARCHAR2(255) NOT NULL
);
INSERT INTO usr_schema_version (version_key, version_value) VALUES ('schema_patch', '47');

CREATE SEQUENCE usr_oguser_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE usr_idkey_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;

CREATE TABLE usr_oguser (
    id NUMBER(19) NOT NULL,
    oid NUMBER(19) NOT NULL,
    ver_from_instant TIMESTAMP NOT NULL,
    ver_to_instant TIMESTAMP NOT NULL,
    corr_from_instant TIMESTAMP NOT NULL,
    corr_to_instant TIMESTAMP NOT NULL,
    userid NVARCHAR2(255) NOT NULL,
    password NVARCHAR2(255),
    name NVARCHAR2(255),
    time_zone NVARCHAR2(255) NOT NULL,
    email_address NVARCHAR2(255),
    PRIMARY KEY (id),
    CONSTRAINT usr_chk_oguser_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT usr_chk_oguser_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_usr_oguser_oid ON usr_oguser(oid);
CREATE INDEX ix_usr_oguser_ver_from ON usr_oguser(ver_from_instant);
CREATE INDEX ix_usr_oguser_ver_to ON usr_oguser(ver_to_instant);
CREATE INDEX ix_usr_oguser_corr_from ON usr_oguser(corr_from_instant);
CREATE INDEX ix_usr_oguser_corr_to ON usr_oguser(corr_to_instant);
CREATE INDEX ix_usr_oguser_userid ON usr_oguser(userid);
CREATE INDEX ix_usr_oguser_name ON usr_oguser(name);
CREATE INDEX ix_usr_oguser_zone ON usr_oguser(time_zone);
CREATE INDEX ix_usr_oguser_email ON usr_oguser(email_address);

CREATE TABLE usr_idkey (
    id NUMBER(19) NOT NULL,
    key_scheme NVARCHAR2(255) NOT NULL,
    key_value NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT usr_chk_idkey UNIQUE (key_scheme, key_value)
);

CREATE TABLE usr_oguser2idkey (
    oguser_id NUMBER(19) NOT NULL,
    idkey_id NUMBER(19) NOT NULL,
    PRIMARY KEY (oguser_id, idkey_id),
    CONSTRAINT usr_fk_oguseridkey2exg FOREIGN KEY (oguser_id) REFERENCES usr_oguser (id) ON DELETE CASCADE,
    CONSTRAINT usr_fk_oguseridkey2idkey FOREIGN KEY (idkey_id) REFERENCES usr_idkey (id) ON DELETE CASCADE
);
CREATE INDEX ix_usr_oguser2idkey_idkey ON usr_oguser2idkey(idkey_id);

CREATE TABLE usr_entitlement (
    oguser_id NUMBER(19) NOT NULL,
    entitlement_index INT NOT NULL,
    entitlement_pattern NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (oguser_id, entitlement_index),
    CONSTRAINT usr_fk_entit2oguser FOREIGN KEY (oguser_id) REFERENCES usr_oguser (id) ON DELETE CASCADE
);
