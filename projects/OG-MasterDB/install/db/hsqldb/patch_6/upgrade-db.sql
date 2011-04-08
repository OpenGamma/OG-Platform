-- In order to add the unique constraint we need to get rid of any existing duplicates
alter table cfg_config add constraint name_type_unique unique (name, config_type, ver_to_instant); -- TODO this is not right IGN-101
