ALTER TABLE sec_swap ADD COLUMN receive_floating_rate_type VARCHAR(32);
ALTER TABLE sec_swap ADD COLUMN pay_floating_rate_type VARCHAR(32);
ALTER TABLE sec_swap ADD COLUMN pay_settlement_days INTEGER;
ALTER TABLE sec_swap ADD COLUMN pay_gearing DOUBLE precision;
ALTER TABLE sec_swap ADD COLUMN pay_offset_fixing_id bigint;
ALTER TABLE sec_swap ADD COLUMN receive_settlement_days INTEGER;
ALTER TABLE sec_swap ADD COLUMN receive_gearing DOUBLE precision;
ALTER TABLE sec_swap ADD COLUMN receive_offset_fixing_id bigint;

ALTER TABLE sec_swap ADD CONSTRAINT sec_fk_payfreq2frequency FOREIGN KEY (pay_frequency_id) REFERENCES sec_frequency (id);
ALTER TABLE sec_swap ADD CONSTRAINT sec_fk_receivefreq2frequency FOREIGN KEY (receive_frequency_id) REFERENCES sec_frequency (id);
ALTER TABLE sec_swap ADD CONSTRAINT sec_fk_payoffset2frequency FOREIGN KEY (pay_offset_fixing_id) REFERENCES sec_frequency (id);
ALTER TABLE sec_swap ADD CONSTRAINT sec_fk_recvoffset2frequency FOREIGN KEY (receive_offset_fixing_id) REFERENCES sec_frequency (id);


ALTER TABLE sec_swap ADD COLUMN pay_iseom BOOLEAN;
UPDATE sec_swap SET pay_iseom = 'FALSE';    
ALTER TABLE sec_swap ALTER COLUMN pay_iseom SET NOT NULL;

ALTER TABLE sec_swap ADD COLUMN receive_iseom BOOLEAN;
UPDATE sec_swap SET receive_iseom = 'FALSE';    
ALTER TABLE sec_swap ALTER COLUMN receive_iseom SET NOT NULL;

UPDATE sec_swap SET receive_floating_rate_type='ibor' WHERE receive_isibor = 'TRUE';
UPDATE sec_swap SET pay_floating_rate_type='ibor' WHERE pay_isibor = 'TRUE';
UPDATE sec_swap SET receive_floating_rate_type='cms' WHERE receive_isibor = 'FALSE';
UPDATE sec_swap SET pay_floating_rate_type='cms' WHERE pay_isibor = 'FALSE';
    

ALTER TABLE sec_swap DROP COLUMN receive_isibor; 
ALTER TABLE sec_swap DROP COLUMN pay_isibor;