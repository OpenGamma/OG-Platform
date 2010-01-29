
    create table audit_log (
        id int8 not null,
        user_id varchar(255) not null,
        originatingSystem varchar(255) not null,
        object_id varchar(255) not null,
        operation varchar(255) not null,
        description varchar(255),
        success bool not null,
        timestamp timestamp not null,
        primary key (id)
    );

    create table authorities (
        authority_id int8 not null,
        authority varchar(255) not null unique,
        primary key (authority_id)
    );

    create table user_groups (
        group_id int8 not null,
        name varchar(255) not null unique,
        primary key (group_id)
    );

    create table user_groups_to_authorities (
        group_id int8 not null,
        authority_id int8 not null,
        primary key (group_id, authority_id)
    );

    create table users (
        user_id int8 not null,
        username varchar(255) not null unique,
        password varchar(255) not null,
        lastLogin date not null,
        primary key (user_id)
    );

    create table users_to_user_groups (
        user_group_id int8 not null,
        user_id int8 not null
    );

    create table users_to_users_groups (
        user_id int8 not null,
        user_group_id int8 not null,
        primary key (user_id, user_group_id)
    );

    alter table user_groups_to_authorities 
        add constraint FK981143B42FC78C47 
        foreign key (authority_id) 
        references authorities;

    alter table user_groups_to_authorities 
        add constraint FK981143B4D2113DBC 
        foreign key (group_id) 
        references user_groups;

    alter table users_to_user_groups 
        add constraint FKE759BC1BF5BC644D 
        foreign key (user_id) 
        references users;

    alter table users_to_user_groups 
        add constraint FKE759BC1BD51497B0 
        foreign key (user_group_id) 
        references user_groups;

    alter table users_to_users_groups 
        add constraint FK67A5BEF8F5BC644D 
        foreign key (user_id) 
        references users;

    alter table users_to_users_groups 
        add constraint FK67A5BEF8D51497B0 
        foreign key (user_group_id) 
        references user_groups;

    create sequence hibernate_sequence start 1 increment 1;
