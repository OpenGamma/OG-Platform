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
