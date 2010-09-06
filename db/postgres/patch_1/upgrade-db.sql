alter table sec_cash
  add column currency_id bigint not null default 1,
  add column region_scheme varchar(255) not null default 'ISO 3166-1 2 Letter Code',
  add column region_identifier varchar(255) not null default 'GB',
  add constraint sec_fk_cash2currency foreign key (currency_id) references sec_currency (id);

alter table sec_cash
  alter column currency_id drop default,
  alter column region_scheme drop default,
  alter column region_identifier drop default;
  
alter table sec_bond
  add column maturity_zone varchar(50) not null default 'UTC';
  
alter table sec_bond
  alter column maturity_zone drop default;
 
alter table sec_future
  add column expiry_zone varchar(50) not null default 'UTC';
  
alter table sec_future
  alter column expiry_zone drop default;
  
alter table sec_option
  add column expiry_zone varchar(50) not null default 'UTC',
  add column underlyingexpiry_zone varchar(50);
 
alter table sec_option
  alter column expiry_zone drop default;
