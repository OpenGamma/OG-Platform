BEGIN TRAN;

  UPDATE eng_schema_version SET version_value='47' WHERE version_key='schema_patch';
  
  DROP TABLE eng_functioncosts;

  CREATE TABLE eng_functioncosts (
    id BIGINT NOT NULL IDENTITY(1,1),
    configuration VARCHAR(255) NOT NULL,
    function_name VARCHAR(255) NOT NULL,
    version_instant DATETIME2(6) NOT NULL,
    invocation_cost DECIMAL(31,8) NOT NULL,
    data_input_cost DECIMAL(31,8) NOT NULL,
    data_output_cost DECIMAL(31,8) NOT NULL,
    PRIMARY KEY (id)
  );

  CREATE INDEX ix_eng_funcost_config ON eng_functioncosts(configuration);
  CREATE INDEX ix_eng_funcost_fnname ON eng_functioncosts(function_name);
  CREATE INDEX ix_eng_funcost_version ON eng_functioncosts(version_instant);

COMMIT;
