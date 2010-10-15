alter table sec_bond alter column announcement_date drop not null;
alter table sec_bond alter column announcement_zone drop not null;
alter table sec_bond alter column businessdayconvention_id drop not null;
alter table sec_bond alter column guaranteetype_id drop not null;
alter table rsk_live_data_snapshot drop column complete;