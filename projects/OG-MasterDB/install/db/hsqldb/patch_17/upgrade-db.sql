ALTER TABLE sec_fxbarrieroption ADD COLUMN barrier_level DOUBLE PRECISION NOT NULL;

ALTER TABLE sec_fxbarrieroption ADD COLUMN is_long BOOLEAN;
UPDATE sec_fxbarrieroption SET is_long = 'FALSE';    
ALTER TABLE sec_fxbarrieroption ALTER COLUMN is_long SET NOT NULL;