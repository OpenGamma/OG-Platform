BEGIN TRAN;

  -- update the version
  UPDATE sec_schema_version SET version_value='58' WHERE version_key='schema_patch';

  CREATE TABLE sec_cashflow (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    currency_id bigint NOT NULL,
    settlement_date DATETIME2(6) NOT NULL,
    settlement_zone VARCHAR(50) NOT NULL,
    amount double precision NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT sec_fk_cashflow2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_cashflow2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id)
  );


COMMIT;
