
-- create-db-common.sql: Standard Hibernate required by Security Master and Position Master
  
create table hibernate_sequence (
     next_val bigint 
);

insert into hibernate_sequence values ( 1 );
