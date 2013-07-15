START TRANSACTION;
  UPDATE pos_schema_version SET version_value='44' WHERE version_key='schema_patch';
COMMIT;