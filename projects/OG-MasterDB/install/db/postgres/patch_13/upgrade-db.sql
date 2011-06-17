ALTER TABLE sec_swap ADD COLUMN pay_isibor boolean;
ALTER TABLE sec_swap ADD COLUMN receive_isibor boolean;

begin;

UPDATE sec_swap SET receive_isibor='TRUE' WHERE receive_legtype = 'Floating interest';
UPDATE sec_swap SET pay_isibor='TRUE' WHERE pay_legtype = 'Floating interest';
    
commit;

