START TRANSACTION;
  -- update the version
  UPDATE usr_schema_version SET version_value='46' WHERE version_key='schema_patch';

  DROP INDEX ix_usr_oguser_name;
  
  ALTER TABLE usr_oguser ALTER COLUMN name RENAME TO userid;
  ALTER TABLE usr_oguser ADD COLUMN name varchar(255);
  ALTER TABLE usr_oguser ADD COLUMN time_zone varchar(255);
  ALTER TABLE usr_oguser ADD COLUMN email_address varchar(255);

  UPDATE usr_oguser SET name = userid;
  UPDATE usr_oguser SET time_zone = 'Europe/London';
  ALTER TABLE usr_oguser ALTER COLUMN time_zone SET NOT NULL;

  CREATE INDEX ix_usr_oguser_userid ON usr_oguser(userid);
  
COMMIT;
