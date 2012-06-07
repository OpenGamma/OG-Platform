BEGIN TRAN;
  -- update the version
  UPDATE sec_schema_version SET version_value='48' WHERE version_key='schema_patch';
  
  CREATE TABLE sec_contract_category (
      id bigint NOT NULL,
      name varchar(255) NOT NULL UNIQUE,
      description varchar(255),
      PRIMARY KEY (id)
  );
  
  INSERT INTO sec_contract_category (ID, NAME)
  SELECT sec_bondfuturetype.id, sec_bondfuturetype.name 
  FROM sec_future
  INNER JOIN sec_bondfuturetype ON sec_future.bondtype_id = sec_bondfuturetype.id;
  
  INSERT INTO sec_contract_category (ID, NAME)
  SELECT sec_commodityfuturetype.id, sec_commodityfuturetype.name 
  FROM sec_future
  INNER JOIN sec_commodityfuturetype ON sec_future.commoditytype_id = sec_commodityfuturetype.id;
    
  
  ALTER TABLE sec_future ADD COLUMN contract_category_id bigint; -- most of the current future has no category defined so the column needs to stay nullable
  ALTER TABLE sec_future ADD CONSTRAINT sec_fk_future2contract_category FOREIGN KEY (contract_category_id) REFERENCES sec_contract_category (id);
 
  UPDATE sec_future SET contract_category_id = bondtype_id WHERE bondtype_id is not null;
  UPDATE sec_future SET contract_category_id = commoditytype_id WHERE commoditytype_id is not null;  
  
  ALTER TABLE sec_future DROP CONSTRAINT sec_fk_future2bondfuturetype;
  ALTER TABLE sec_future DROP CONSTRAINT sec_fk_future2commodityfuturetype;
  
  ALTER TABLE sec_future DROP COLUMN bondtype_id;
  ALTER TABLE sec_future DROP COLUMN commoditytype_id;
  
  
  DROP TABLE sec_commodityfuturetype;  
  DROP TABLE sec_bondfuturetype;  
  
COMMIT;
