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
    (next value for sec_security_seq, 'Precious Matal'),
    (next value for sec_security_seq, 'Crude Oil'),
    (next value for sec_security_seq, 'Natural Gas'),
    (next value for sec_security_seq, 'Wheat'),
    (next value for sec_security_seq, 'Livestock'),
    (next value for sec_security_seq, 'Equity Index'),
    (next value for sec_security_seq, 'Interest Rate'),
    (next value for sec_security_seq, 'Bond'),
    (next value for sec_security_seq, 'Cross Currency'),
    (next value for sec_security_seq, 'STOCK FUTURE');
              
  -- setting contract categories from commodity type                    
                      
  UPDATE sec_future AS F SET F.contract_category_id = (select C.id 
  FROM 
    sec_commodityfuturetype T, sec_contract_category C
  WHERE
    C.name = 'Precious Matal'
  AND   
    T.id = F.commoditytype_id AND T.name = 'Precious Metal'); 
  
  UPDATE sec_future AS F SET F.contract_category_id = (select C.id 
  FROM 
    sec_commodityfuturetype T, sec_contract_category C
  WHERE
    C.name = 'Crude Oil'
  AND   
    T.id = F.commoditytype_id AND T.name = 'Crude Oil'); 
 
  UPDATE sec_future AS F SET F.contract_category_id = (select C.id 
  FROM 
    sec_commodityfuturetype T, sec_contract_category C
  WHERE
    C.name = 'Natural Gas'
  AND   
    T.id = F.commoditytype_id AND T.name = 'Natural Gas'); 
        
  UPDATE sec_future AS F SET F.contract_category_id = (select C.id 
  FROM 
    sec_commodityfuturetype T, sec_contract_category C
  WHERE
    C.name = 'Wheat'
  AND   
    T.id = F.commoditytype_id AND T.name = 'Wheat'); 
            
  UPDATE sec_future AS F SET F.contract_category_id = (select C.id 
  FROM 
    sec_commodityfuturetype T, sec_contract_category C
  WHERE
    C.name = 'Livestock'
  AND   
    T.id = F.commoditytype_id AND T.name = 'Livestock');             

  -- setting contract categories from future type

  UPDATE sec_future AS F SET F.contract_category_id = (select C.id 
  FROM 
    sec_contract_category C
  WHERE
    C.name = 'Equity Index'
  AND   
    F.future_type = 'Index');             
  
  UPDATE sec_future AS F SET F.contract_category_id = (select C.id 
  FROM 
    sec_contract_category C
  WHERE
    C.name = 'Equity Index'
  AND   
    F.future_type = 'Equity Index Dividend');             
     
  UPDATE sec_future AS F SET F.contract_category_id = (select C.id 
  FROM 
    sec_contract_category C
  WHERE
    C.name = 'Interest Rate'
  AND   
    F.future_type = 'Interest Rate');             
 
  UPDATE sec_future AS F SET F.contract_category_id = (select C.id 
  FROM 
    sec_contract_category C
  WHERE
    C.name = 'Bond'
  AND   
    F.future_type = 'Bond');             
  
  UPDATE sec_future AS F SET F.contract_category_id = (select C.id 
  FROM 
    sec_contract_category C
  WHERE
    C.name = 'STOCK FUTURE'
  AND   
    F.future_type = 'Equity');             
      
  UPDATE sec_future AS F SET F.contract_category_id = (select C.id 
  FROM 
    sec_contract_category C
  WHERE
    C.name = 'Cross Currency'
  AND   
    F.future_type = 'FX');                      
  
  --------------------------------------------------------------                     
  
  ALTER TABLE sec_future DROP CONSTRAINT sec_fk_future2bondfuturetype;
  ALTER TABLE sec_future DROP CONSTRAINT sec_fk_future2commodityfuturetype;
  
  ALTER TABLE sec_future DROP COLUMN bondtype_id;
  ALTER TABLE sec_future DROP COLUMN commoditytype_id;
  
  
  DROP TABLE sec_commodityfuturetype;  
  DROP TABLE sec_bondfuturetype;  
  
COMMIT;
