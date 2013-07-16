BEGIN TRAN;

  -- update the version
  UPDATE sec_schema_version SET version_value='54' WHERE version_key='schema_patch';
  
  CREATE TABLE sec_stubtype (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
  );

	CREATE TABLE sec_cds (
	  id bigint NOT NULL,
	  security_id bigint NOT NULL,
	  notional double precision NOT NULL,
	  recovery_rate double precision NOT NULL,
	  spread double precision NOT NULL,
	  currency_id bigint NOT NULL,
	  maturity_date DATETIME2(6) NOT NULL,
	  maturity_date_zone varchar(50) NOT NULL,
	  start_date DATETIME2(6) NOT NULL,
	  start_date_zone varchar(50) NOT NULL,
	  premium_frequency_id bigint NOT NULL,
	  daycountconvention_id bigint NOT NULL,
	  businessdayconvention_id bigint NOT NULL,
	  stubtype_id bigint NOT NULL,
	  settlement_days int NOT NULL,
	  underlying_issuer varchar(255) NOT NULL,
	  underlying_currency_id bigint NOT NULL,
	  underlying_seniority varchar(255) NOT NULL,
	  restructuring_clause varchar(255) NOT NULL,
	  
	  PRIMARY KEY (id),
	  CONSTRAINT sec_fk_cds2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
	  CONSTRAINT sec_fk_cds2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
	  CONSTRAINT sec_fk_cds2daycount FOREIGN KEY (daycountconvention_id) REFERENCES sec_daycount (id),
	  CONSTRAINT sec_fk_cds2businessdayconvention FOREIGN KEY (businessdayconvention_id) REFERENCES sec_businessdayconvention (id),
	  CONSTRAINT sec_fk_cds2frequency FOREIGN KEY (premium_frequency_id) REFERENCES sec_frequency (id),
	  CONSTRAINT sec_fk_cds2stubtype FOREIGN KEY (stubtype_id) REFERENCES sec_stubtype (id),
	  CONSTRAINT sec_fk_cds_underlying2currency FOREIGN KEY (underlying_currency_id) REFERENCES sec_currency (id)
	);
    
COMMIT;
