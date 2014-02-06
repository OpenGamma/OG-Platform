START TRANSACTION;
    -- update the version
    UPDATE sec_schema_version SET version_value='68' WHERE version_key='schema_patch';
  CREATE TABLE sec_indexweightingtype (
    id bigint NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
  );

  -- Indices
  CREATE TABLE sec_iborindex (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    description VARCHAR(255),
    indexfamily_scheme VARCHAR(255),
    indexfamily_identifier VARCHAR(255),
    tenor_id bigint NOT NULL,
    convention_scheme VARCHAR(255) NOT NULL,
    convention_identifier VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_iborindex2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_iborindex2tenor FOREIGN KEY (tenor_id) REFERENCES sec_tenor (id)
  );

  CREATE TABLE sec_overnightindex (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    description VARCHAR(255),
    indexfamily_scheme VARCHAR(255),
    indexfamily_identifier VARCHAR(255),
    convention_scheme VARCHAR(255) NOT NULL,
    convention_identifier VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_overnightindex2sec FOREIGN KEY (security_id) REFERENCES sec_security (id)
  );

  CREATE TABLE sec_bondindex (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    description VARCHAR(255),
    indexfamily_scheme VARCHAR(255),
    indexfamily_identifier VARCHAR(255),
    indexweightingtype_id bigint NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_bondindex2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_bondindex2indexweightingtype FOREIGN KEY (indexweightingtype_id) REFERENCES sec_indexweightingtype (id)
  );

  CREATE TABLE sec_bondindexcomponent (
    id bigint NOT NULL,
    position bigint NOT NULL,
    bondindex_id bigint NOT NULL,
    weight decimal(31,8) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_bondindexcomponent2bondindex FOREIGN KEY (bondindex_id) REFERENCES sec_bondindex (id)
  );

  CREATE TABLE sec_bondindexcomponentidentifier (
    bundle_id bigint NOT NULL,
    scheme varchar(255) NOT NULL,
    identifier varchar(255) NOT NULL,
    PRIMARY KEY (bundle_id, scheme, identifier),
    CONSTRAINT sec_fk_bondindexcomponentidentifier2bondindexcomponent FOREIGN KEY (bundle_id) REFERENCES sec_bondindexcomponent (id)
  );

  CREATE TABLE sec_equityindex (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    description VARCHAR(255),
    indexfamily_scheme VARCHAR(255),
    indexfamily_identifier VARCHAR(255),
    indexweightingtype_id bigint NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_equityindex2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_equityindex2indexweightingtype FOREIGN KEY (indexweightingtype_id) REFERENCES sec_indexweightingtype (id)
  );

  CREATE TABLE sec_equityindexcomponent (
    id bigint NOT NULL,
    position bigint NOT NULL,
    equityindex_id bigint NOT NULL,
    weight decimal(31,8) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_equityindexcomponent2equityindex FOREIGN KEY (equityindex_id) REFERENCES sec_equityindex (id)
  );

  CREATE TABLE sec_equityindexcomponentidentifier (
    bundle_id bigint NOT NULL,
    scheme varchar(255) NOT NULL,
    identifier varchar(255) NOT NULL,
    PRIMARY KEY (bundle_id, scheme, identifier),
    CONSTRAINT sec_fk_equityindexcomponentidentifier2equityindexcomponent FOREIGN KEY (bundle_id) REFERENCES sec_equityindexcomponent (id)
  );

  CREATE TABLE sec_indexfamily (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_indexfamily2sec FOREIGN KEY (security_id) REFERENCES sec_security (id)
  );

  CREATE TABLE sec_indexfamilyentry (
    indexfamily_id bigint NOT NULL,
    tenor_id bigint NOT NULL,
    scheme varchar(255) NOT NULL,
    identifier varchar(255) NOT NULL,
    PRIMARY KEY (indexfamily_id, tenor_id),
    CONSTRAINT sec_fk_indexfamilyentry2indexfamily FOREIGN KEY (indexfamily_id) REFERENCES sec_indexfamily (id),
    CONSTRAINT sec_fk_indexfamilyentry2tenor FOREIGN KEY (tenor_id) REFERENCES sec_tenor (id)
  );

COMMIT;
