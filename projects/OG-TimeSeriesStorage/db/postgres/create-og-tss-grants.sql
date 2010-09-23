REVOKE ALL ON tss_data_source FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_data_source TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE tss_data_source_id_seq TO tssupdater;
GRANT SELECT ON TABLE tss_data_source TO PUBLIC;

REVOKE ALL ON tss_data_provider FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_data_provider TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE tss_data_provider_id_seq TO tssupdater;
GRANT SELECT ON TABLE tss_data_provider TO PUBLIC;

REVOKE ALL ON tss_data_field FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_data_field TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE tss_data_field_id_seq TO tssupdater;
GRANT SELECT ON TABLE tss_data_field TO PUBLIC;

REVOKE ALL ON tss_observation_time FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_observation_time TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE tss_observation_time_id_seq TO tssupdater;
GRANT SELECT ON TABLE tss_observation_time TO PUBLIC;

REVOKE ALL ON tss_domain FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_domain TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE tss_domain_id_seq TO tssupdater;
GRANT SELECT ON TABLE tss_domain TO PUBLIC;

REVOKE ALL ON tss_quoted_object FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_quoted_object TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE tss_quoted_object_id_seq TO tssupdater;
GRANT SELECT ON TABLE tss_quoted_object TO PUBLIC;

REVOKE ALL ON tss_time_series_key FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_time_series_key TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE tss_time_series_key_id_seq TO tssupdater;
GRANT SELECT ON TABLE tss_time_series_key TO PUBLIC;

REVOKE ALL ON tss_time_series_data FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_time_series_data TO tssupdater;
GRANT SELECT ON TABLE tss_time_series_data TO PUBLIC;

REVOKE ALL ON tss_time_series_data_delta FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_time_series_data_delta TO tssupdater;
GRANT SELECT ON TABLE tss_time_series_data_delta TO PUBLIC;

REVOKE ALL ON tss_domain_spec_identifier FROM PUBLIC;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE tss_domain_spec_identifier TO tssupdater;
GRANT SELECT, UPDATE ON SEQUENCE tss_domain_spec_identifier_id_seq TO tssupdater;
GRANT SELECT ON TABLE tss_domain_spec_identifier TO PUBLIC;