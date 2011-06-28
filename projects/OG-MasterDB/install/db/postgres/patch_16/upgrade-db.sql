BEGIN;
ALTER TABLE sec_fxoption ADD COLUMN is_long BOOLEAN;
UPDATE sec_fxoption SET is_long = 'FALSE';    
ALTER TABLE sec_fxoption ALTER COLUMN is_long SET NOT NULL;
COMMIT;