BEGIN TRAN;

  -- update the version
  UPDATE sec_schema_version SET version_value='55' WHERE version_key='schema_patch';
  
  CREATE TABLE sec_debtseniority (
    id bigint NOT NULL,
    name varchar(255) NOT NULL UNIQUE,
    PRIMARY KEY (id)
  );
    
COMMIT;
