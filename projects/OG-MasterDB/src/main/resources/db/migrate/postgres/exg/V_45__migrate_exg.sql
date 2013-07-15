START TRANSACTION;
  UPDATE hol_schema_version SET version_value='45' WHERE version_key='schema_patch';
  UPDATE exg_schema_version SET version_value='45' WHERE version_key='schema_patch';  
COMMIT;