START TRANSACTION;

  -- update the version
  UPDATE sec_schema_version SET version_value='54' WHERE version_key='schema_patch';

CREATE TABLE sec_cds (
  id bigint NOT NULL,
  security_id bigint NOT NULL,
  notional double precision NOT NULL,
  recovery_rate double precision NOT NULL,
  spread double precision NOT NULL,
  currency_id bigint NOT NULL,
  maturity_date timestamp without time zone NOT NULL,
  maturity_date_zone varchar(50) NOT NULL,
  start_date timestamp without time zone NOT NULL,
  start_date_zone varchar(50) NOT NULL,
  premium_frequency_id bigint NOT NULL,
  underlying_scheme varchar(255),
  underlying_identifier varchar(255),
  daycountconvention_id bigint NOT NULL,
  businessdayconvention_id bigint NOT NULL,
  
  PRIMARY KEY (id),
  CONSTRAINT sec_fk_cds2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
  CONSTRAINT sec_fk_cds2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
  CONSTRAINT sec_fk_cds2daycount FOREIGN KEY (daycountconvention_id) REFERENCES sec_daycount (id),
  CONSTRAINT sec_fk_cds2businessdayconvention FOREIGN KEY (businessdayconvention_id) REFERENCES sec_businessdayconvention (id),
  CONSTRAINT sec_fk_cds2frequency FOREIGN KEY (premium_frequency_id) REFERENCES sec_frequency (id)
);
    
COMMIT;
