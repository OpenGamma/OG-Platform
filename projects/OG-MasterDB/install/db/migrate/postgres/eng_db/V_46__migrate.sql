START TRANSACTION;
  UPDATE eng_schema_version SET version_value='46' WHERE version_key='schema_patch';
  ALTER  TABLE eng_functioncosts RENAME COLUMN function TO function_name;
  ALTER  TABLE eng_functioncosts ADD PRIMARY KEY (configuration, function_name, version_instant);
COMMIT;
