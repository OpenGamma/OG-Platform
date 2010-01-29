drop table DOMAIN_SPEC_IDENTIFIER;
drop table DOMAIN;
drop table TIME_SERIES_DATA;
drop table TIME_SERIES_KEY;
drop table QUOTED_OBJECT;
drop table DATA_SOURCE;
drop table DATA_PROVIDER;
drop table DATA_FIELD;
drop table OBSERVATION_TIME;

create table QUOTED_OBJECT (
	id integer not null generated always as identity
    	constraint quoted_obj_pk primary key,
	name varchar(255) not null,
	description varchar(255)
);

create index quoted_object_name_idx on QUOTED_OBJECT(name);

create table DATA_SOURCE (
	id integer not null generated always as identity
    	constraint data_source_id_pk primary key,
	name varchar(255) not null,
	description varchar(255)
);

create index data_source_idx on DATA_SOURCE(name);

create table DATA_PROVIDER (
	id integer not null generated always as identity
    	constraint data_provider_id_pk primary key,
	name varchar(255) not null,
	description varchar(255)
);

create index data_provider_name_idx on DATA_PROVIDER(name);

create table DATA_FIELD (
	id integer not null generated always as identity
    	constraint data_field_id_pk primary key,
	name varchar(255) not null,
	description varchar(255)
);

create index data_field_name_idx on DATA_FIELD(name);

create table OBSERVATION_TIME (
	id integer not null generated always as identity
    	constraint observation_time_id_pk primary key,
	name varchar(255) not null,
	description varchar(255)
);

create index observation_time_name_idx on OBSERVATION_TIME(name);

create table TIME_SERIES_KEY (
	id integer not null generated always as identity 
		constraint time_series_key_pk primary key,
	qouted_obj_id integer constraint ts_qouted_obj_id_fk  
		references QUOTED_OBJECT on delete cascade on update restrict,
	data_soure_id integer constraint data_soure_id_fk  
		references DATA_SOURCE on delete cascade on update restrict,
	data_provider_id integer constraint data_provider_id_fk 
		references DATA_PROVIDER on delete cascade on update restrict,
	data_field_id integer constraint data_field_id_fk 
	 	references DATA_FIELD on delete cascade on update restrict,
	observation_time_id integer constraint observation_time_id_fk
		references OBSERVATION_TIME on delete cascade on update restrict
);

create table TIME_SERIES_DATA (
	ts_id integer constraint ts_id_fk references TIME_SERIES_KEY on delete cascade on update restrict,
	value double not null
);

create table DOMAIN (
	id integer not null generated always as identity
    	constraint domain_id_pk primary key,
	name varchar(255) not null,
	description varchar(255)
);

create index domain_name_idx on DOMAIN(name);
		
create table DOMAIN_SPEC_IDENTIFIER (
	id integer not null generated always as identity
    	constraint domain_spec_id_pk primary key,
	quoted_obj_id integer constraint ds_qouted_obj_id_fk 
		references QUOTED_OBJECT on delete cascade on update restrict,
	domain_id integer constraint domain_id_fk 
		references DOMAIN on delete cascade on update restrict,
	identifier varchar(255) not null
);

create index domain_spec_identifier_identifier_idx on DOMAIN_SPEC_IDENTIFIER(identifier);