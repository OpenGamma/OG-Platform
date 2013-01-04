START TRANSACTION;

	-- update the version
	UPDATE sec_schema_version SET version_value='55' WHERE version_key='schema_patch';
	  
  CREATE TABLE sec_debt_seniority (
     id bigint NOT NULL,
     name varchar(255) NOT NULL UNIQUE,
     PRIMARY KEY (id)
  );
  
  CREATE TABLE  sec_restructuring_clause (
     id bigint NOT NULL,
     name varchar(255) NOT NULL UNIQUE,
     PRIMARY KEY (id)
  );
  
  CREATE TABLE sec_credit_default_swap (
   id bigint NOT NULL,
   security_id bigint NOT NULL,
   cds_type varchar(255) NOT NULL,
   buy boolean NOT NULL,
   buyer_scheme varchar(255) NOT NULL,
   buyer_identifier varchar(255) NOT NULL,
   seller_scheme varchar(255) NOT NULL,
   seller_identifier varchar(255) NOT NULL,
   entity_scheme varchar(255) NOT NULL,
   entity_identifier varchar(255) NOT NULL,
   debt_seniority_id bigint NOT NULL,
   restructuring_clause_id bigint NOT NULL,
   region_scheme varchar(255) NOT NULL,
   region_identifier varchar(255) NOT NULL,
   start_date timestamp without time zone NOT NULL,
   start_date_zone varchar(50) NOT NULL,
   effective_date timestamp without time zone NOT NULL,
   effective_date_zone varchar(50) NOT NULL,
   maturity_date timestamp without time zone NOT NULL,
   maturity_date_zone varchar(50) NOT NULL,
   stub_type_id bigint NOT NULL,
   frequency_id bigint NOT NULL,
   daycount_convention_id bigint NOT NULL,
   businessday_convention_id bigint NOT NULL,
   imm_adjust_maturity_date boolean NOT NULL,
   adjust_effective_date boolean NOT NULL,
   adjust_maturity_date boolean NOT NULL,
   notional_type varchar(32) NOT NULL,
   notional_currency_id bigint NOT NULL,
   notional_amount double precision,
   notional_scheme varchar(255),
   notional_id varchar(255),
   recovery_rate double precision NOT NULL,
   include_accrued_premium boolean NOT NULL,
   protection_start boolean NOT NULL,
   legacy_par_spread double precision,
   std_quoted_spread double precision,
   std_upfrontamt_notional_type varchar(32),
   std_upfrontamt_notional_currency_id bigint,
   std_upfrontamt_notional_amount double precision,
   std_upfrontamt_notional_scheme varchar(255),
   std_upfrontamt_notional_id varchar(255),
   stdvanilla_coupon double precision,
   stdvanilla_cashsettlement_date timestamp without time zone,
   stdvanilla_ashsettlement_date_zone varchar(50),
   stdvanilla_adjust_cashsettlement_date boolean,
   
   PRIMARY KEY (id),
   CONSTRAINT sec_fk_creditdefaultswap2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
   CONSTRAINT sec_fk_creditdefaultswap2debtseniority FOREIGN KEY (debt_seniority_id) REFERENCES sec_debt_seniority(id),
   CONSTRAINT sec_fk_creditdefaultswap2restructuringclause FOREIGN KEY (restructuring_clause_id) REFERENCES sec_restructuring_clause(id),
   CONSTRAINT sec_fk_creditdefaultswap2stubtype FOREIGN KEY (stub_type_id) REFERENCES sec_stubtype (id),
   CONSTRAINT sec_fk_creditdefaultswap2frequency FOREIGN KEY (frequency_id) REFERENCES sec_frequency (id),
   CONSTRAINT sec_fk_creditdefaultswap2daycount FOREIGN KEY (daycount_convention_id) REFERENCES sec_daycount (id),
   CONSTRAINT sec_fk_creditdefaultswap2businessdayconvention FOREIGN KEY (businessday_convention_id) REFERENCES sec_businessdayconvention (id),
   CONSTRAINT sec_fk_creditdefaultswap2currency FOREIGN KEY (notional_currency_id) REFERENCES sec_currency (id)
 );
 CREATE INDEX ix_sec_creditdefaultswap_security_id ON sec_credit_default_swap(security_id);
  

COMMIT;
