ALTER TABLE tss_identifier ADD COLUMN valid_from DATE;
ALTER TABLE tss_identifier ADD COLUMN valid_to DATE;
DROP INDEX idx_identifier_scheme_value;
CREATE INDEX idx_identifier_scheme_value on tss_identifier (identification_scheme_id, identifier_value);


alter table sec_bond alter column announcement_date set null;
alter table sec_bond alter column announcement_zone set null;
alter table sec_bond alter column businessdayconvention_id set null;
alter table sec_bond alter column guaranteetype_id set null;
alter table rsk_live_data_snapshot drop column complete;
