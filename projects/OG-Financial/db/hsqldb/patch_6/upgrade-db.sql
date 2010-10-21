ALTER TABLE tss_identifier ADD COLUMN valid_from DATE;
ALTER TABLE tss_identifier ADD COLUMN valid_to DATE;
DROP INDEX idx_identifier_scheme_value;
CREATE INDEX idx_identifier_scheme_value on tss_identifier (identification_scheme_id, identifier_value);
