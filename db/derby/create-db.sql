
    create table audit_log (
        id bigint not null,
        user_id varchar(255) not null,
        originatingSystem varchar(255) not null,
        object_id varchar(255) not null,
        operation varchar(255) not null,
        description varchar(255),
        success smallint not null,
        timestamp timestamp not null,
        primary key (id)
    );

    create table authorities (
        authority_id bigint not null,
        authority varchar(255) not null unique,
        primary key (authority_id)
    );

    create table user_groups (
        group_id bigint not null,
        name varchar(255) not null unique,
        primary key (group_id)
    );

    create table user_groups_to_authorities (
        group_id bigint not null,
        authority_id bigint not null,
        primary key (group_id, authority_id)
    );

    create table users (
        user_id bigint not null,
        username varchar(255) not null unique,
        password varchar(255) not null,
        lastLogin date not null,
        primary key (user_id)
    );

    create table users_to_user_groups (
        user_group_id bigint not null,
        user_id bigint not null
    );

    create table users_to_users_groups (
        user_id bigint not null,
        user_group_id bigint not null,
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

    create table hibernate_sequence (
         next_val bigint 
    );

    insert into hibernate_sequence values ( 1 );
