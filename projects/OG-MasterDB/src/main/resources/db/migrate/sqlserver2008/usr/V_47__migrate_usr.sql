SET XACT_ABORT ON;
BEGIN TRANSACTION;
  -- update the version
  UPDATE usr_schema_version SET version_value='47' WHERE version_key='schema_patch';

  ALTER TABLE usr_oguser ALTER COLUMN password varchar(255);

  IF NOT EXISTS (SELECT name FROM sysindexes WHERE name = 'ix_usr_oguser_name')
    CREATE INDEX ix_usr_oguser_name ON usr_oguser(name);

  IF NOT EXISTS (SELECT name FROM sysindexes WHERE name = 'ix_usr_oguser_zone')
    CREATE INDEX ix_usr_oguser_zone ON usr_oguser(time_zone);

  IF NOT EXISTS (SELECT name FROM sysindexes WHERE name = 'ix_usr_oguser_email')
    CREATE INDEX ix_usr_oguser_email ON usr_oguser(email_address);


COMMIT;
