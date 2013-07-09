START TRANSACTION;
  UPDATE sec_schema_version SET version_value='45' WHERE version_key='schema_patch';
COMMIT;