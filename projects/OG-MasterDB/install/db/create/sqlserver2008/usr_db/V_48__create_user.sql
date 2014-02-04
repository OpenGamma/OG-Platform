-- create-db-user.sql : User Master

-- User has one primary table: usr_oguser.
-- Bitemporal versioning exists at the document level

CREATE TABLE usr_schema_version (
    version_key VARCHAR(32) NOT NULL,
    version_value VARCHAR(255) NOT NULL
);
INSERT INTO usr_schema_version (version_key, version_value) VALUES ('schema_patch', '48');

--CREATE SEQUENCE usr_oguser_seq AS bigint
--    START WITH 1000 INCREMENT BY 1 NO CYCLE;
--CREATE SEQUENCE usr_idkey_seq as bigint
--    START WITH 1000 INCREMENT BY 1 NO CYCLE;
CREATE TABLE usr_oguser_seq (
  SeqID INT identity(1000,1) PRIMARY KEY,
  SeqVal VARCHAR(1)
);
CREATE TABLE usr_idkey_seq (
  SeqID INT identity(1000,1) PRIMARY KEY,
  SeqVal VARCHAR(1)
);

CREATE TABLE usr_oguser (
    id bigint NOT NULL,
    oid bigint NOT NULL,
    ver_from_instant DATETIME2(6)  NOT NULL,
    ver_to_instant DATETIME2(6)  NOT NULL,
    corr_from_instant DATETIME2(6)  NOT NULL,
    corr_to_instant DATETIME2(6)  NOT NULL,
    userid varchar(255) NOT NULL,
    password varchar(255),
    name varchar(255),
    time_zone varchar(255) NOT NULL,
    email_address varchar(255),
    PRIMARY KEY (id),
    CONSTRAINT usr_chk_oguser_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT usr_chk_oguser_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_usr_oguser_oid ON usr_oguser(oid);
CREATE INDEX ix_usr_oguser_ver_from_instant ON usr_oguser(ver_from_instant);
CREATE INDEX ix_usr_oguser_ver_to_instant ON usr_oguser(ver_to_instant);
CREATE INDEX ix_usr_oguser_corr_from_instant ON usr_oguser(corr_from_instant);
CREATE INDEX ix_usr_oguser_corr_to_instant ON usr_oguser(corr_to_instant);
CREATE INDEX ix_usr_oguser_username ON usr_oguser(userid);
CREATE INDEX ix_usr_oguser_name ON usr_oguser(name);
CREATE INDEX ix_usr_oguser_zone ON usr_oguser(time_zone);
CREATE INDEX ix_usr_oguser_email ON usr_oguser(email_address);

-- roles
CREATE TABLE usr_ogrole (
    id bigint NOT NULL,
    oid bigint NOT NULL,
    ver_from_instant DATETIME2(6)  NOT NULL,
    ver_to_instant DATETIME2(6)  NOT NULL,
    corr_from_instant DATETIME2(6)  NOT NULL,
    corr_to_instant DATETIME2(6)  NOT NULL,    
    name varchar(255), -- it should be somehow unique ?!
    cryptokey varchar(255), -- key used to cryptographically sign the capability
    PRIMARY KEY (id),
    CONSTRAINT usr_chk_ogrole_ver_order CHECK (ver_from_instant <= ver_to_instant),
    CONSTRAINT usr_chk_ogrole_corr_order CHECK (corr_from_instant <= corr_to_instant)
);
CREATE INDEX ix_usr_ogrole_oid ON usr_ogrole(oid);
CREATE INDEX ix_usr_ogrole_ver_from_instant ON usr_ogrole(ver_from_instant);
CREATE INDEX ix_usr_ogrole_ver_to_instant ON usr_ogrole(ver_to_instant);
CREATE INDEX ix_usr_ogrole_corr_from_instant ON usr_ogrole(corr_from_instant);
CREATE INDEX ix_usr_ogrole_corr_to_instant ON usr_ogrole(corr_to_instant);
CREATE INDEX ix_usr_ogrole_name ON usr_ogrole(name);

-- many-to-many relationship between roles and their children (ancestry tree)
CREATE TABLE usr_role2parent (
    role_oid bigint NOT NULL,
    parent_oid bigint NOT NULL,
    PRIMARY KEY (role_oid, parent_oid)
);
CREATE INDEX ix_usr_role2parent_role ON usr_role2parent(role_oid); -- TODO do we need that ?
CREATE INDEX ix_usr_role2parent_parent ON usr_role2parent(parent_oid);



-- many-to-many relationship between users and roles
CREATE TABLE usr_oguser2ogrole (
    oguser_id bigint NOT NULL,
    ogrole_oid bigint NOT NULL,
    PRIMARY KEY (oguser_id, ogrole_oid),
    CONSTRAINT usr_fk_oguser_ogrole2oguser FOREIGN KEY (oguser_id) REFERENCES usr_oguser (id) ON DELETE CASCADE
);
CREATE INDEX ix_usr_oguser2ogrole_oguser ON usr_oguser2ogrole(oguser_id); -- TODO do we need that ?
CREATE INDEX ix_usr_oguser2ogrole_ogrole ON usr_oguser2ogrole(ogrole_oid);


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
    id bigint NOT NULL,
    ogrole_id bigint NOT NULL, -- the role this entitlement entitles the resource.
    resource_oid varchar(255) NOT NULL, -- the identifier of the resource, this entitlement deals with.
    resource_type varchar(255) NOT NULL, -- type of the resource this entitlement deals with. E.g. 'portfolio', 'datasource' etc.
    resource_access varchar(255) NOT NULL, -- access type this entitlement gives to the resoure. E.g 'rw' - read-write, 'rd' - read-delete, 'r' - read-only
    PRIMARY KEY (id),
    CONSTRAINT usr_chk_entitlement UNIQUE (ogrole_id, resource_oid, resource_type, resource_access),
    CONSTRAINT usr_fk_entit2ogrole FOREIGN KEY (ogrole_id) REFERENCES usr_ogrole(id) ON DELETE CASCADE
);
CREATE INDEX ix_usr_entitlement__resource_oid ON usr_entitlement(resource_oid); -- used to query entitlement by resource oid, efficent building capabilities
CREATE INDEX ix_usr_entitlement__ogrole_id__resource_type ON usr_entitlement(ogrole_id, resource_type); -- used to list all entitlements by role and resource type
