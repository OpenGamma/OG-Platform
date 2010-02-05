
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
        id int8 not null,
        authority varchar(255) not null unique,
        primary key (id)
    );

    create table user_groups (
        id int8 not null,
        name varchar(255) not null unique,
        primary key (id)
    );

    create table user_groups_to_authorities (
        group_id int8 not null,
        authority_id int8 not null,
        primary key (group_id, authority_id),
        constraint fk_user_groups_to_authorities2user_groups foreign key (group_id) references user_groups(id) ON DELETE CASCADE,
        constraint fk_user_groups_to_authorities2authorities foreign key (authority_id) references authorities(id) ON DELETE CASCADE
    );

    create table users (
        id int8 not null,
        username varchar(255) not null unique,
        password varchar(255) not null,
        lastLogin date not null,
        primary key (id)
    );

    create table users_to_user_groups (
        user_id int8 not null,
        user_group_id int8 not null,
        primary key (user_id, user_group_id),
        constraint fk_users_to_user_groups2users foreign key (user_id) references users(id) ON DELETE CASCADE,
        constraint fk_users_to_user_groups2user_groups foreign key (user_group_id) references user_groups(id) ON DELETE CASCADE
    );

    create sequence hibernate_sequence start 1 increment 1;
