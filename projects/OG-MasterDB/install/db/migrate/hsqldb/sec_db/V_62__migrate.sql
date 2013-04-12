START TRANSACTION;

  -- update the version
  UPDATE sec_schema_version SET version_value='62' WHERE version_key='schema_patch';

  CREATE TABLE sec_credit_default_swap_option (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    buy boolean NOT NULL,
    buyer_scheme varchar(255) NOT NULL,
    buyer_identifier varchar(255) NOT NULL,
    seller_scheme varchar(255) NOT NULL,
    seller_identifier varchar(255) NOT NULL,
    start_date timestamp without time zone NOT NULL,
    start_date_zone varchar(50) NOT NULL,
    maturity_date timestamp without time zone NOT NULL,
    maturity_date_zone varchar(50) NOT NULL,
    currency_id bigint NOT NULL,
    notional double precision NOT NULL,
    strike double precision NOT NULL,
    knock_out boolean NOT NULL,
    payer boolean NOT NULL,
    exercise_type varchar(32) NOT NULL,
    underlying_scheme varchar(255) NOT NULL,
    underlying_identifier varchar(255) NOT NULL,
    
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_creditdefaultswapoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_creditdefaultswapoption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency(id),
  );
  
COMMIT;
