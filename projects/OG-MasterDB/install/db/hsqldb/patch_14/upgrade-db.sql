ALTER TABLE sec_future drop COLUMN cashratetype_id;
ALTER TABLE sec_future drop constraint sec_fk_future2cashrate;

BEGIN;
ALTER TABLE sec_fra ADD COLUMN underlying_scheme varchar(255);
UPDATE sec_fra SET underlying_scheme = 'BLOOMBERG_TICKER';    
ALTER TABLE sec_fra ALTER COLUMN underlying_scheme SET NOT NULL;
COMMIT;

BEGIN;
ALTER TABLE sec_fra ADD COLUMN underlying_identifier varchar(255);
UPDATE sec_fra SET underlying_identifier = 'US0003M Index';    
ALTER TABLE sec_fra ALTER COLUMN underlying_identifier SET NOT NULL;
COMMIT;