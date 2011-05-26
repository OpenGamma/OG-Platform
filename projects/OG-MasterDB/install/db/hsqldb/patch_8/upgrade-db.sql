
alter table pos_trade add column premium_value double precision;
alter table pos_trade add column premium_currency varchar(255);
alter table pos_trade add column premium_date date;
alter table pos_trade add column premium_time time(6);
alter table pos_trade add column premium_zone_offset int;