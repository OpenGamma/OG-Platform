SET XACT_ABORT ON;
BEGIN TRAN;

  -- update the version
  UPDATE sec_schema_version SET version_value='50' WHERE version_key='schema_patch';
    
  CREATE TABLE sec_commodity_forward (
      id BIGINT NOT NULL,
      security_id BIGINT NOT NULL,
      forward_type VARCHAR(32) NOT NULL,
      expiry_date DATETIME2(6) NOT NULL,
      expiry_zone VARCHAR(50) NOT NULL,
      expiry_accuracy smallint NOT NULL,
      currency_id BIGINT,    
      unitname_id BIGINT,
      unitnumber double precision,
      unit_amount double precision,
      underlying_scheme VARCHAR(255),
      underlying_identifier VARCHAR(255),     
      contract_category_id BIGINT NOT NULL,
      PRIMARY KEY (id),
      CONSTRAINT sec_fk_commodity_forward2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),    
      CONSTRAINT sec_fk_commodity_forward2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),    
      CONSTRAINT sec_fk_commodity_forward2unit FOREIGN KEY (unitname_id) REFERENCES sec_unit (id),
      CONSTRAINT sec_fk_commodity_forward2contract_category FOREIGN KEY (contract_category_id) REFERENCES sec_contract_category (id)
  );
  
  CREATE INDEX ix_sec_commodity_forward_security_id ON sec_commodity_forward(security_id);
    
COMMIT;
