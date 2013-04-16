BEGIN TRAN;

  -- update the version
  UPDATE sec_schema_version SET version_value='64' WHERE version_key='schema_patch';

  CREATE TABLE sec_credit_default_swap_index (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    buy bit NOT NULL,
    buyer_scheme varchar(255) NOT NULL,
    buyer_identifier varchar(255) NOT NULL,
    seller_scheme varchar(255) NOT NULL,
    seller_identifier varchar(255) NOT NULL,
    index_scheme varchar(255) NOT NULL,
    index_identifier varchar(255) NOT NULL,
    start_date DATETIME2(6) NOT NULL,
    start_date_zone varchar(50) NOT NULL,
    effective_date DATETIME2(6) NOT NULL,
    effective_date_zone varchar(50) NOT NULL,
    maturity_date DATETIME2(6) NOT NULL,
    maturity_date_zone varchar(50) NOT NULL,
    settlement_date DATETIME2(6) NOT NULL,
    settlement_date_zone varchar(50) NOT NULL,
    stub_type_id bigint NOT NULL,
    frequency_id bigint NOT NULL,
    daycount_convention_id bigint NOT NULL,
    businessday_convention_id bigint NOT NULL,
    imm_adjust_maturity_date bit NOT NULL,
    adjust_effective_date bit NOT NULL,
    adjust_maturity_date bit NOT NULL,
    adjust_settlement_date bit NOT NULL,
    notional_type varchar(32) NOT NULL,
    notional_currency_id bigint NOT NULL,
    notional_amount double precision,
    notional_scheme varchar(255),
    notional_id varchar(255),
    include_accrued_premium bit NOT NULL,
    protection_start bit NOT NULL,
    upfrontamt_notional_type varchar(32),
    upfrontamt_notional_currency_id bigint,
    upfrontamt_notional_amount double precision,
    upfrontamt_notional_scheme varchar(255),
    upfrontamt_notional_id varchar(255),
    index_coupon double precision,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_cdsindex2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_cdsindex2stubtype FOREIGN KEY (stub_type_id) REFERENCES sec_stubtype (id),
    CONSTRAINT sec_fk_cdsindex2frequency FOREIGN KEY (frequency_id) REFERENCES sec_frequency (id),
    CONSTRAINT sec_fk_cdsindex2daycount FOREIGN KEY (daycount_convention_id) REFERENCES sec_daycount (id),
    CONSTRAINT sec_fk_cdsindex2businessdayconvention FOREIGN KEY (businessday_convention_id) REFERENCES sec_businessdayconvention (id),
    CONSTRAINT sec_fk_cdsindex2currency FOREIGN KEY (notional_currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_cdsindex2upfrontamtcurrency FOREIGN KEY (upfrontamt_notional_currency_id) REFERENCES sec_currency (id)
  );
  CREATE INDEX ix_sec_cdsindex_security_id ON sec_credit_default_swap_index(security_id);

  -- Migrate security type for any existing cds_index_definitions
  UPDATE sec_security
  SET sec_type = 'CDS_INDEX_DEFINITION'
  WHERE sec_type = 'CDS_INDEX';

COMMIT;
