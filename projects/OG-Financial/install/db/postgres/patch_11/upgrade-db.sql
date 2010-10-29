ALTER TABLE cfg_config DROP COLUMN last_read_instant;

ALTER TABLE rsk_run ALTER COLUMN view_version DROP NOT NULL;
ALTER TABLE rsk_run ALTER COLUMN view_version TYPE varchar(255);
