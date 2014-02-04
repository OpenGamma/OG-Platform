BEGIN TRANSACTION;
  -- update the version
  UPDATE usr_schema_version SET version_value='48' WHERE version_key='schema_patch';

  CREATE TABLE usr_ogrole (
    id bigint NOT NULL,
    oid bigint NOT NULL,
    ver_from_instant DATETIME2(6) NOT NULL,
    ver_to_instant DATETIME2(6) NOT NULL,
    corr_from_instant DATETIME2(6) NOT NULL,
    corr_to_instant DATETIME2(6) NOT NULL,
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


  ALTER TABLE usr_entitlement DROP CONSTRAINT usr_fk_entit2oguser;
  DROP TABLE usr_entitlement;

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

COMMIT;
