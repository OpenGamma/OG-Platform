alter table sec_cash add column maturity_date timestamp default '2008-08-08 20:08:08' not null;
alter table sec_cash add column maturity_zone varchar(50) default 'UTC' not null;

alter table sec_cash alter column maturity_date drop default;
alter table sec_cash alter column maturity_zone drop default;
  
alter table sec_fra add column currency_id bigint default 1 not null;
alter table sec_fra add column region_scheme varchar(255) default 'ISO 3166-1 2 Letter Code' not null;
alter table sec_fra add column region_identifier varchar(255) default 'US' not null;
alter table sec_fra add constraint sec_fk_fra2currency foreign key (currency_id) references sec_currency (id);
  
alter table sec_fra alter column currency_id drop default;
alter table sec_fra alter column region_scheme drop default,
alter table sec_fra alter column region_identifier drop default;