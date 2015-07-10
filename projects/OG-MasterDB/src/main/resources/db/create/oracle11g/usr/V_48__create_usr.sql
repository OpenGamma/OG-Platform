-- create-db-user.sql : User Master

-- User has one primary table: usr_user.
-- There is no versioning, just a store of event history

CREATE TABLE usr_schema_version (
    version_key NVARCHAR2(32) NOT NULL,
    version_value NVARCHAR2(255) NOT NULL
);
INSERT INTO usr_schema_version (version_key, version_value) VALUES ('schema_patch', '48');

-- Main user tables
CREATE SEQUENCE usr_user_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE usr_user_idkey_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE usr_user_perm_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE usr_user_extn_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE usr_user_event_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;

CREATE TABLE usr_user (
    id NUMBER(19) NOT NULL,
    version INT NOT NULL,
    user_name NVARCHAR2(255) NOT NULL,
    user_name_ci NVARCHAR2(255) NOT NULL,
    password_hash NVARCHAR2(255),
    status CHAR NOT NULL,
    email_address NVARCHAR2(255),
    email_address_ci NVARCHAR2(255),
    display_name NVARCHAR2(255) NOT NULL,
    display_name_ci NVARCHAR2(255) NOT NULL,
    locale_tag NVARCHAR2(127) NOT NULL,
    time_zone NVARCHAR2(127) NOT NULL,
    date_fmt_style NVARCHAR2(31) NOT NULL,
    time_fmt_style NVARCHAR2(31) NOT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX ix_usr_user_uname ON usr_user(user_name_ci);
CREATE INDEX ix_usr_user_email ON usr_user(email_address_ci);
CREATE INDEX ix_usr_user_dname ON usr_user(display_name_ci);

CREATE TABLE usr_user_idkey (
    id NUMBER(19) NOT NULL,
    key_scheme NVARCHAR2(255) NOT NULL,
    key_value NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT usr_chk_uidkey UNIQUE (key_scheme, key_value)
);

CREATE TABLE usr_user_user2idkey (
    user_id NUMBER(19) NOT NULL,
    idkey_id NUMBER(19) NOT NULL,
    PRIMARY KEY (user_id, idkey_id),
    CONSTRAINT usr_fk_uuseridkey2exg FOREIGN KEY (user_id) REFERENCES usr_user (id) ON DELETE CASCADE,
    CONSTRAINT usr_fk_uuseridkey2idkey FOREIGN KEY (idkey_id) REFERENCES usr_user_idkey (id) ON DELETE CASCADE
);
CREATE INDEX ix_usr_user2idkey_idkey ON usr_user_user2idkey(idkey_id);

CREATE TABLE usr_user_assocperm (
    id NUMBER(19) NOT NULL,
    user_id NUMBER(19) NOT NULL,
    permission_str NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT usr_fk_uperm2user FOREIGN KEY (user_id) REFERENCES usr_user (id) ON DELETE CASCADE
);
CREATE INDEX ix_usr_perm_userid ON usr_user_assocperm(user_id);

CREATE TABLE usr_user_extension (
    id NUMBER(19) NOT NULL,
    user_id NUMBER(19) NOT NULL,
    extn_key NVARCHAR2(255) NOT NULL,
    extn_value NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT usr_fk_uextn2user FOREIGN KEY (user_id) REFERENCES usr_user (id) ON DELETE CASCADE
);
CREATE INDEX ix_usr_uextn_userid ON usr_user_extension(user_id);

CREATE TABLE usr_user_name_lookup (
    user_name_ci NVARCHAR2(255) NOT NULL,
    user_id NUMBER(19) NOT NULL,
    deleted CHAR NOT NULL,
    PRIMARY KEY (user_name_ci)
);
-- user_id is not a foreign key as user can be deleted while lookup is left remaining

CREATE TABLE usr_user_event_history (
    id NUMBER(19) NOT NULL,
    user_id NUMBER(19) NOT NULL,
    version INT NOT NULL,
    event_type CHAR NOT NULL,
    active_user NVARCHAR2(255) NOT NULL,
    event_instant TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX ix_usr_uevent_userid ON usr_user_event_history(user_id);
-- user_id is not a foreign key as user can be deleted while event is left remaining

CREATE TABLE usr_user_event_item (
    id NUMBER(19) NOT NULL,
    event_id NUMBER(19) NOT NULL,
    description NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT usr_fk_ueventitem2event FOREIGN KEY (event_id) REFERENCES usr_user_event_history (id) ON DELETE CASCADE
);
CREATE INDEX ix_usr_ueventitem_eventid ON usr_user_event_item(event_id);


-- Main role tables
CREATE SEQUENCE usr_role_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE usr_role_assocuser_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE usr_role_assocperm_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE usr_role_assocrole_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE usr_role_event_seq
    START WITH 1000 INCREMENT BY 1 NOCYCLE;

CREATE TABLE usr_role (
    id NUMBER(19) NOT NULL,
    version INT NOT NULL,
    role_name NVARCHAR2(255) NOT NULL,
    role_name_ci NVARCHAR2(255) NOT NULL,
    description NVARCHAR2(255),
    PRIMARY KEY (id)
);
CREATE INDEX ix_usr_role_rname ON usr_role(role_name_ci);

CREATE TABLE usr_role_assocuser (
    id NUMBER(19) NOT NULL,
    role_id NUMBER(19) NOT NULL,
    assoc_user NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT usr_fk_ruser2role FOREIGN KEY (role_id) REFERENCES usr_role (id) ON DELETE CASCADE
);
CREATE INDEX ix_usr_ruser_roleid ON usr_role_assocuser(role_id);

CREATE TABLE usr_role_assocperm (
    id NUMBER(19) NOT NULL,
    role_id NUMBER(19) NOT NULL,
    assoc_perm NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT usr_fk_rperm2role FOREIGN KEY (role_id) REFERENCES usr_role (id) ON DELETE CASCADE
);
CREATE INDEX ix_usr_rperm_roleid ON usr_role_assocperm(role_id);

CREATE TABLE usr_role_assocrole (
    id NUMBER(19) NOT NULL,
    role_id NUMBER(19) NOT NULL,
    assoc_role NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT usr_fk_rrole2role FOREIGN KEY (role_id) REFERENCES usr_role (id) ON DELETE CASCADE
);
CREATE INDEX ix_usr_rrole_roleid ON usr_role_assocrole(role_id);

CREATE TABLE usr_role_name_lookup (
    role_name_ci NVARCHAR2(255) NOT NULL,
    role_id NUMBER(19) NOT NULL,
    deleted CHAR NOT NULL,
    PRIMARY KEY (role_name_ci)
);
-- role_id is not a foreign key as role can be deleted while lookup is left remaining

CREATE TABLE usr_role_event_history (
    id NUMBER(19) NOT NULL,
    role_id NUMBER(19) NOT NULL,
    version INT NOT NULL,
    event_type CHAR NOT NULL,
    active_user NVARCHAR2(255) NOT NULL,
    event_instant TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);
CREATE INDEX ix_usr_revent_roleid ON usr_role_event_history(role_id);
-- role_id is not a foreign key as role can be deleted while event is left remaining

CREATE TABLE usr_role_event_item (
    id NUMBER(19) NOT NULL,
    event_id NUMBER(19) NOT NULL,
    description NVARCHAR2(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT usr_fk_reventitem2event FOREIGN KEY (event_id) REFERENCES usr_role_event_history (id) ON DELETE CASCADE
);
CREATE INDEX ix_usr_reventitem_eventid ON usr_role_event_item(event_id);

