
create table rsk_function_unique_id (
	id int not null,
	unique_id varchar(255) not null,
	
	primary key (id),
	
	constraint rsk_chk_uq_function_unique_id unique (unique_id)
);

insert into rsk_function_unique_id values (0, 'unknown');

alter table rsk_value add column function_unique_id int not null default 0;
alter table rsk_value alter column function_unique_id drop default;
alter table rsk_value add constraint rsk_fk_value2function_id
        foreign key (function_unique_id) references rsk_function_unique_id (id);
alter table rsk_failure add column function_unique_id int not null;
alter table rsk_failure add constraint rsk_fk_failure2function_id
        foreign key (function_unique_id) references rsk_function_unique_id (id);

alter table rsk_opengamma_version drop constraint rsk_chk_uq_opengamma_version;
alter table rsk_opengamma_version drop column hash;
alter table rsk_opengamma_version add constraint rsk_chk_uq_opengamma_version unique (version);

alter table rsk_run drop column run_reason;
alter table rsk_run drop column view_oid;
alter table rsk_run drop column view_version;

alter table rsk_run add constraint rsk_chk_uq_run unique (run_time_id);