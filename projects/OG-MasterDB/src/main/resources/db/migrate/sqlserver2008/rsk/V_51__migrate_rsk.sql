BEGIN TRAN;

  UPDATE rsk_schema_version SET version_value='51' WHERE version_key='schema_patch';

  create table rsk_target_property (
    id bigint not null,
    target_id bigint not null,
    property_key varchar(255),
    property_value varchar(255),
    primary key (id),

    constraint rsk_fk_trg_prop2target
        foreign key (target_id) references rsk_computation_target (id)
  );

COMMIT;
