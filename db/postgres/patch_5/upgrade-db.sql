alter table sec_cash
  add column maturity_date timestamp not null default '2008-08-08 20:08:08',
  add column maturity_zone varchar(50) not null default 'UTC';

alter table sec_cash
  alter column maturity_date drop default,
  alter column maturity_zone drop default;
  
alter table sec_fra
  add column currency_id bigint not null default 1,
  add column region_scheme varchar(255) not null default 'ISO 3166-1 2 Letter Code',
  add column region_identifier varchar(255) not null default 'US',
  add constraint sec_fk_fra2currency foreign key (currency_id) references sec_currency (id);
  
alter table sec_fra
  alter column currency_id drop default,
  alter column region_scheme drop default,
  alter column region_identifier drop default;