BEGIN TRAN;

  -- update the version
  UPDATE sec_schema_version SET version_value='59' WHERE version_key='schema_patch';

  -- update any securities from sec_type EQUITY_INDEX_FUTURE_OPTION to EQUITY_INDEX_DIVIDEND_FUTURE_OPTION
  UPDATE sec_security
  SET sec_type = 'EQUITY_INDEX_DIVIDEND_FUTURE_OPTION'
  WHERE sec_type = 'EQUITY_INDEX_FUTURE_OPTION';

  CREATE TABLE sec_equity_index_futureoption (
    id BIGINT NOT NULL,
    security_id BIGINT NOT NULL,
    option_exercise_type VARCHAR(32) NOT NULL,
    option_type VARCHAR(32) NOT NULL,
    strike double precision NOT NULL,
    expiry_date DATETIME2(6) NOT NULL,
    expiry_zone VARCHAR(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    underlying_scheme VARCHAR(255) NOT NULL,
    underlying_identifier VARCHAR(255) NOT NULL,
    currency_id BIGINT NOT NULL,
    exchange_id BIGINT NOT NULL,
    margined BIT NOT NULL,
    pointValue double precision NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_equity_index_futureoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_equity_index_futureoption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_equity_index_futureoption2exchange FOREIGN KEY (exchange_id) REFERENCES sec_exchange (id)
  );
  CREATE INDEX ix_sec_equity_index_futureoption_security_id ON sec_equity_index_futureoption(security_id);

COMMIT;
