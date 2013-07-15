START TRANSACTION;

  -- update the version
  UPDATE sec_schema_version SET version_value='63' WHERE version_key='schema_patch';

  CREATE TABLE sec_cdsid_family (
     id bigint NOT NULL,
     name varchar(255) NOT NULL UNIQUE,
     PRIMARY KEY (id)
  );

  CREATE TABLE sec_credit_default_swap_index_definition (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    version varchar(255) NOT NULL,
    series varchar(255) NOT NULL,
    family_id bigint NOT NULL,
    currency_id bigint NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_credit_index_defn2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_credit_index_defn2family FOREIGN KEY (family_id) REFERENCES sec_cdsid_family (id),
    CONSTRAINT sec_credit_index_defn2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id)
  );

  CREATE TABLE sec_cdsid_tenor (
    cdsid_id bigint NOT NULL,
    tenor_id bigint NOT NULL,
    PRIMARY KEY (cdsid_id, tenor_id),
    CONSTRAINT sec_fk_sec_cdsid_tenor2cdsid FOREIGN KEY (cdsid_id) REFERENCES sec_credit_default_swap_index_definition (id),
    CONSTRAINT sec_fk_sec_cdsid_tenor2tenor FOREIGN KEY (tenor_id) REFERENCES sec_tenor (id)
  );

  CREATE TABLE sec_cdsid_component (
    cdsid_id bigint NOT NULL,
    obligor_scheme varchar(255) NOT NULL,
    obligor_identifier varchar(255) NOT NULL,
    name varchar(255) NOT NULL,
    weight double precision NOT NULL,
    bond_scheme varchar(255),
    bond_identifier varchar(255),
    PRIMARY KEY (cdsid_id, obligor_scheme, obligor_identifier),
    CONSTRAINT sec_fk_cdsid_component2cdsid FOREIGN KEY (cdsid_id) REFERENCES sec_credit_default_swap_index_definition (id)
  );

  -- Migrate any date from old name to new name
  INSERT INTO sec_cdsid_family (id, name)
  SELECT id, name
  FROM sec_cdsi_family;

  INSERT INTO sec_credit_default_swap_index_definition (id, security_id, version, series, family_id, currency_id)
  SELECT id, security_id, version, series, family_id, currency_id
  FROM sec_credit_default_swap_index;

  INSERT INTO sec_cdsid_tenor (cdsid_id, tenor_id)
  SELECT cdsi_id, tenor_id
  FROM sec_cdsi_tenor;

  INSERT INTO sec_cdsid_component (cdsid_id, obligor_scheme, obligor_identifier, name, weight, bond_scheme, bond_identifier)
  SELECT cdsi_id, obligor_scheme, obligor_identifier, name, weight, bond_scheme, bond_identifier
  FROM sec_cdsi_component;

  -- Now drop the old names
  DROP TABLE sec_cdsi_component;
  DROP TABLE sec_cdsi_tenor;
  DROP TABLE sec_credit_default_swap_index;
  DROP TABLE sec_cdsi_family;

COMMIT;
