
ALTER TABLE sec_fxoption ADD COLUMN option_exercise_type varchar(32);
UPDATE sec_fxoption SET option_exercise_type = 'EUROPEAN';    
ALTER TABLE sec_fxoption ALTER COLUMN option_exercise_type SET NOT NULL;    
