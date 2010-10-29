
create table audit_log (
    id bigint not null,
    user_id varchar(255) not null,
    originatingSystem varchar(255) not null,
    object_id varchar(255) not null,
    operation varchar(255) not null,
    description varchar(255),
    success boolean not null,
    timestamp timestamp not null,
    primary key (id)
);

create table authority (
    id bigint not null,
    regex varchar(255) not null unique,
    primary key (id)
);

create table user_group (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table user_group_to_authority (
    group_id bigint not null,
    authority_id bigint not null,
    primary key (group_id, authority_id),
    constraint fk_user_group_to_authority2user_group foreign key (group_id) references user_group(id) ON DELETE CASCADE,
    constraint fk_user_group_to_authority2authority foreign key (authority_id) references authority(id) ON DELETE CASCADE
);

create table og_user (
    id bigint not null,
    username varchar(255) not null unique,
    password varchar(255) not null,
    lastLogin date not null,
    primary key (id)
);

create table user_to_user_group (
    user_id bigint not null,
    user_group_id bigint not null,
    primary key (user_id, user_group_id),
    constraint fk_user_to_user_group2og_user foreign key (user_id) references og_user(id) ON DELETE CASCADE,
    constraint fk_user_to_user_group2user_group foreign key (user_group_id) references user_group(id) ON DELETE CASCADE
);

create sequence hibernate_sequence start 1 increment 1;
