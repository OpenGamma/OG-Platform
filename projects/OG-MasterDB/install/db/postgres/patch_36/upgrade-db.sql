ALTER TABLE sec_fra ADD COLUMN fixing_date TIMESTAMP without time zone;
ALTER TABLE sec_fra ADD COLUMN fixing_zone varchar(50);

BEGIN;

update sec_fra as A
set fixing_zone = (select B.start_zone from sec_fra as B where B.id = A.id);

update sec_fra as A
set fixing_date = (select B.start_date - INTERVAL '2 days' from sec_fra as B where B.id = A.id);

ALTER TABLE sec_fra ALTER COLUMN fixing_date SET NOT NULL;
ALTER TABLE sec_fra ALTER COLUMN fixing_zone SET NOT NULL;
COMMIT;