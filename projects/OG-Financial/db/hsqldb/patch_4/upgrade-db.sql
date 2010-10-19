alter table sec_bond alter column announcement_date set null;
alter table sec_bond alter column announcement_zone set null;
alter table sec_bond alter column businessdayconvention_id set null;
alter table sec_bond alter column guaranteetype_id set null;
alter table rsk_live_data_snapshot drop column complete;