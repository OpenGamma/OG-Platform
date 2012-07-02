START TRANSACTION;

  -- update the version
  UPDATE sec_schema_version SET version_value='48' WHERE version_key='schema_patch';
  
  CREATE TABLE sec_contract_category (
      id bigint NOT NULL,
      name varchar(255) NOT NULL UNIQUE,
      description varchar(255),
      PRIMARY KEY (id)
  );
  
  ALTER TABLE sec_future ADD COLUMN contract_category_id bigint; -- most of the current future has no category defined so the column needs to stay nullable
  ALTER TABLE sec_future ADD CONSTRAINT sec_fk_future2contract_category FOREIGN KEY (contract_category_id) REFERENCES sec_contract_category (id);
  
  INSERT INTO sec_contract_category (ID, NAME)
  VALUES 
    (nextval('sec_security_seq'), 'Precious Matal'),
    (nextval('sec_security_seq'), 'Crude Oil'),
    (nextval('sec_security_seq'), 'Natural Gas'),
    (nextval('sec_security_seq'), 'Wheat'),
    (nextval('sec_security_seq'), 'Livestock'),
    (nextval('sec_security_seq'), 'Equity Index'),
    (nextval('sec_security_seq'), 'Interest Rate'),
    (nextval('sec_security_seq'), 'Bond'),
    (nextval('sec_security_seq'), 'Cross Currency'),
    (nextval('sec_security_seq'), 'STOCK FUTURE');
              
  -- setting contract categories from commodity type                    
                      
  UPDATE sec_future SET contract_category_id = C.id
  FROM sec_commodityfuturetype T, sec_contract_category C
  WHERE
    C.name = 'Precious Matal'
  AND   
    T.id = sec_future.commoditytype_id AND T.name = 'Precious Metal'; 
    
  UPDATE sec_future SET contract_category_id = C.id
  FROM sec_commodityfuturetype T, sec_contract_category C
  WHERE
    C.name = 'Crude Oil'
  AND   
    T.id = sec_future.commoditytype_id AND T.name = 'Crude Oil'; 
    
  UPDATE sec_future SET contract_category_id = C.id
  FROM sec_commodityfuturetype T, sec_contract_category C
  WHERE
    C.name = 'Natural Gas'
  AND   
    T.id = sec_future.commoditytype_id AND T.name = 'Natural Gas';
    
  UPDATE sec_future SET contract_category_id = C.id
  FROM sec_commodityfuturetype T, sec_contract_category C
  WHERE
    C.name = 'Wheat'
  AND   
    T.id = sec_future.commoditytype_id AND T.name = 'Wheat';  
  
  UPDATE sec_future SET contract_category_id = C.id
  FROM sec_commodityfuturetype T, sec_contract_category C
  WHERE
    C.name = 'Livestock'
  AND   
    T.id = sec_future.commoditytype_id AND T.name = 'Livestock';  
          
  -- setting contract categories from future type
    
  UPDATE sec_future SET contract_category_id = C.id
  FROM sec_contract_category C
  WHERE
    C.name = 'Equity Index'
  AND   
    sec_future.future_type = 'Index';
  
  
  UPDATE sec_future SET contract_category_id = C.id
  FROM sec_contract_category C
  WHERE
    C.name = 'Equity Index'
  AND   
    sec_future.future_type = 'Equity Index Dividend';
  
  
  UPDATE sec_future SET contract_category_id = C.id
  FROM sec_contract_category C
  WHERE
    C.name = 'Interest Rate'
  AND   
    sec_future.future_type = 'Interest Rate';
  
  
  UPDATE sec_future SET contract_category_id = C.id
  FROM sec_contract_category C
  WHERE
    C.name = 'Bond'
  AND   
    sec_future.future_type = 'Bond';
  
  
  UPDATE sec_future SET contract_category_id = C.id
  FROM sec_contract_category C
  WHERE
    C.name = 'STOCK FUTURE'
  AND   
    sec_future.future_type = 'Equity';
  
  
  UPDATE sec_future SET contract_category_id = C.id
  FROM sec_contract_category C
  WHERE
    C.name = 'Cross Currency'
  AND   
    sec_future.future_type = 'FX';
  
  --------------------------------------------------------------                       
  
  ALTER TABLE sec_future DROP CONSTRAINT sec_fk_future2bondfuturetype;
  ALTER TABLE sec_future DROP CONSTRAINT sec_fk_future2commodityfuturetype;
  
  ALTER TABLE sec_future DROP COLUMN bondtype_id;
  ALTER TABLE sec_future DROP COLUMN commoditytype_id;
  
  
  DROP TABLE sec_commodityfuturetype;  
  DROP TABLE sec_bondfuturetype;  
  
COMMIT;
