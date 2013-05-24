START TRANSACTION;
  -- update the version
  UPDATE usr_schema_version SET version_value='47' WHERE version_key='schema_patch';

  ALTER TABLE usr_oguser ALTER COLUMN password SET NULL;

  CREATE INDEX ix_usr_oguser_name ON usr_oguser(name);
  CREATE INDEX ix_usr_oguser_zone ON usr_oguser(time_zone);
  CREATE INDEX ix_usr_oguser_email ON usr_oguser(email_address);

COMMIT;
