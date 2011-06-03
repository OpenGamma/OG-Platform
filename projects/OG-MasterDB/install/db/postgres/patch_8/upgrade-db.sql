
alter table pos_trade add column premium_value double precision;
alter table pos_trade add column premium_currency varchar(255);
alter table pos_trade add column premium_date date;
alter table pos_trade add column premium_time time;
alter table pos_trade add column premium_zone_offset int;

CREATE INDEX ix_sec_security_sec_typeu ON sec_security(upper(sec_type));