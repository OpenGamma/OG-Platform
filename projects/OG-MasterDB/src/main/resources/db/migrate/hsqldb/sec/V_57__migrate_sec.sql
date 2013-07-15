START TRANSACTION;

  -- update the version
  UPDATE sec_schema_version SET version_value='57' WHERE version_key='schema_patch';

  ALTER TABLE sec_equity ADD COLUMN preferred boolean;

  UPDATE sec_equity SET preferred = 'false';

  UPDATE sec_equity SET preferred = 'true' WHERE security_id IN (  
	  SELECT security_id
	  FROM sec_security_attribute
	  WHERE attr_key = 'preferred'
	    AND attr_value = 'true'
  );

  DELETE FROM sec_security_attribute WHERE attr_key = 'preferred';

  ALTER TABLE sec_equity ALTER COLUMN preferred SET NOT NULL;

COMMIT;
