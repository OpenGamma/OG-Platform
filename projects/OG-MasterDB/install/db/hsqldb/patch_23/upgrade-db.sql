ALTER TABLE sec_swap ADD COLUMN receive_floating_rate_type VARCHAR(32);
ALTER TABLE sec_swap ADD COLUMN pay_floating_rate_type VARCHAR(32);


ALTER TABLE sec_swap ADD COLUMN pay_iseom BOOLEAN;
UPDATE sec_swap SET pay_iseom = 'FALSE';    
ALTER TABLE sec_swap ALTER COLUMN pay_iseom SET NOT NULL;

ALTER TABLE sec_swap ADD COLUMN receive_iseom BOOLEAN;
UPDATE sec_swap SET receive_iseom = 'FALSE';    
ALTER TABLE sec_swap ALTER COLUMN receive_iseom SET NOT NULL;

UPDATE sec_swap SET receive_floating_rate_type='ibor' WHERE receive_legtype = 'Floating interest';
UPDATE sec_swap SET pay_floating_rate_type='ibor' WHERE pay_legtype = 'Floating interest';
    

ALTER TABLE sec_swap DROP COLUMN receive_isibor; 
ALTER TABLE sec_swap DROP COLUMN pay_isibor;