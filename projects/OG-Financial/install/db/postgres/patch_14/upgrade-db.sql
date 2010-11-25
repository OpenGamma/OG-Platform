
drop sequence pos_master_seq;
drop table pos_securitykey;
drop table pos_position;
drop table pos_node;
drop table pos_portfolio;

alter table rsk_computation_target add column id_version varchar(255) null;
alter table rsk_computation_target add column name varchar(255) null;
alter table rsk_computation_target drop constraint rsk_chk_uq_computation_target;
alter table rsk_computation_target add constraint rsk_chk_uq_computation_target unique (type_id, id_scheme, id_value, id_version);


