START TRANSACTION;
  UPDATE eng_schema_version SET version_value='46' WHERE version_key='schema_patch';
  ALTER  TABLE eng_functioncosts RENAME COLUMN function TO function_name;
COMMIT;
