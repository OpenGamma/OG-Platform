BEGIN TRAN;

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
    
COMMIT;
