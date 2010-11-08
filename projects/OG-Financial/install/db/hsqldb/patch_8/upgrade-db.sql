ALTER TABLE cfg_config DROP COLUMN last_read_instant;

ALTER TABLE rsk_run ALTER COLUMN view_version SET NULL;
ALTER TABLE rsk_run ALTER COLUMN view_version varchar(255);
