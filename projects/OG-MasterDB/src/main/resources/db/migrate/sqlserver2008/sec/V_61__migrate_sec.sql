BEGIN TRAN;

  -- update the version
  UPDATE sec_schema_version SET version_value='61' WHERE version_key='schema_patch';

  CREATE TABLE sec_cdsi_family (
     id bigint NOT NULL,
     name varchar(255) NOT NULL UNIQUE,
     PRIMARY KEY (id)
  );

  CREATE TABLE sec_credit_default_swap_index (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    version varchar(255) NOT NULL,
    series varchar(255) NOT NULL,
    family_id bigint NOT NULL,
    currency_id bigint NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_credit_index2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_credit_index2family FOREIGN KEY (family_id) REFERENCES sec_cdsi_family (id),
    CONSTRAINT sec_credit_index2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id)
  );

  CREATE TABLE sec_tenor (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
  );

  CREATE TABLE sec_cdsi_tenor (
    cdsi_id bigint NOT NULL,
    tenor_id bigint NOT NULL,
    PRIMARY KEY (cdsi_id, tenor_id),
    CONSTRAINT sec_fk_sec_cdsi_tenor2cdsi FOREIGN KEY (cdsi_id) REFERENCES sec_credit_default_swap_index (id),
    CONSTRAINT sec_fk_sec_cdsi_tenor2tenor FOREIGN KEY (tenor_id) REFERENCES sec_tenor (id)
  );

  CREATE TABLE sec_cdsi_component (
    cdsi_id bigint NOT NULL,
    obligor_scheme varchar(255) NOT NULL,
    obligor_identifier varchar(255) NOT NULL,
    name varchar(255) NOT NULL,
    weight double precision NOT NULL,
    bond_scheme varchar(255),
    bond_identifier varchar(255),
    PRIMARY KEY (cdsi_id, obligor_scheme, obligor_identifier),
    CONSTRAINT sec_fk_cdsicomponent2cdsi FOREIGN KEY (cdsi_id) REFERENCES sec_credit_default_swap_index (id)
  );

COMMIT;
