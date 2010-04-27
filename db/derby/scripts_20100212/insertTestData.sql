INSERT INTO domain (name, description) VALUES ('bbgTicker', 'Bloomberg Ticker');
INSERT INTO domain (name, description) VALUES ('cusip', 'Cusip');
INSERT INTO domain (name, description) VALUES ('bbgUnique', 'Bloomberg Unique');

INSERT INTO quoted_object (name, description) VALUES ('OpenGamma', 'OpenGamma Inc');
INSERT INTO quoted_object (name, description) VALUES ('Apple', 'Apple Inc');
INSERT INTO quoted_object (name, description) VALUES ('Dell', 'Dell Inc');

INSERT INTO domain_spec_identifier (quoted_obj_id, domain_id, identifier) VALUES (1, 1, 'OGM US Equity');
INSERT INTO domain_spec_identifier (quoted_obj_id, domain_id, identifier) VALUES (1, 2, 'OGM2345678');

INSERT INTO domain_spec_identifier (quoted_obj_id, domain_id, identifier) VALUES (2, 1, 'AAPL US Equity');
INSERT INTO domain_spec_identifier (quoted_obj_id, domain_id, identifier) VALUES (2, 2, '123456789');
INSERT INTO domain_spec_identifier (quoted_obj_id, domain_id, identifier) VALUES (2, 3, 'XI45678-89');

INSERT INTO data_source (name, description) VALUES ('BBG', 'Bloomberg');
INSERT INTO data_source (name, description) VALUES ('RDMS', 'Reuters');

INSERT INTO data_provider (name, description) VALUES ('CMPL', 'CMPL');
INSERT INTO data_provider (name, description) VALUES ('CMPN', 'CMPN');

INSERT INTO data_field (name, description) VALUES ('CLOSE', 'Daily Close Price');
INSERT INTO data_field (name, description) VALUES ('OPEN', 'Daily Open Price');

INSERT INTO observation_time (name, description) VALUES ('LCLOSE', 'London Close');
INSERT INTO observation_time (name, description) VALUES ('NCLOSE', 'NewYork Close');
INSERT INTO observation_time (name, description) VALUES ('LOPEN', 'London Open');
INSERT INTO observation_time (name, description) VALUES ('NOPEN', 'NewYork Open');

INSERT INTO time_series_key (qouted_obj_id, data_soure_id, data_provider_id, data_field_id, observation_time_id) VALUES (1, 1, 1, 1, 1);
INSERT INTO time_series_key (qouted_obj_id, data_soure_id, data_provider_id, data_field_id, observation_time_id) VALUES (1, 1, 1, 2, 1);

INSERT INTO time_series_data (id, ts_date, value) VALUES (2, '2010-01-03', 5.10);
INSERT INTO time_series_data (id, ts_date, value) VALUES (2, '2010-01-04', 5.11);
INSERT INTO time_series_data (id, ts_date, value) VALUES (2, '2010-01-05', 5.12);
INSERT INTO time_series_data (id, ts_date, value) VALUES (2, '2010-01-06', 5.13);
INSERT INTO time_series_data (id, ts_date, value) VALUES (2, '2010-01-07', 5.14);
INSERT INTO time_series_data (id, ts_date, value) VALUES (2, '2010-01-08', 5.15);
INSERT INTO time_series_data (id, ts_date, value) VALUES (2, '2010-01-09', 5.16);
INSERT INTO time_series_data (id, ts_date, value) VALUES (2, '2010-01-10', 5.17);
INSERT INTO time_series_data (id, ts_date, value) VALUES (2, '2010-02-01', 5.20);
INSERT INTO time_series_data (id, ts_date, value) VALUES (2, '2010-02-02', 5.21);
INSERT INTO time_series_data (id, ts_date, value) VALUES (2, '2010-02-03', 5.22);
INSERT INTO time_series_data (id, ts_date, value) VALUES (2, '2010-02-04', 5.23);


INSERT INTO time_series_data (id, ts_date, value) VALUES (1, '2010-01-03', 10.10);
INSERT INTO time_series_data (id, ts_date, value) VALUES (1, '2010-01-04', 10.11);
INSERT INTO time_series_data (id, ts_date, value) VALUES (1, '2010-01-05', 10.12);
INSERT INTO time_series_data (id, ts_date, value) VALUES (1, '2010-01-06', 10.13);
INSERT INTO time_series_data (id, ts_date, value) VALUES (1, '2010-01-07', 10.14);
INSERT INTO time_series_data (id, ts_date, value) VALUES (1, '2010-01-08', 10.15);
INSERT INTO time_series_data (id, ts_date, value) VALUES (1, '2010-01-09', 10.16);
INSERT INTO time_series_data (id, ts_date, value) VALUES (1, '2010-01-10', 10.17);
INSERT INTO time_series_data (id, ts_date, value) VALUES (1, '2010-02-01', 10.20);
INSERT INTO time_series_data (id, ts_date, value) VALUES (1, '2010-02-02', 10.21);
INSERT INTO time_series_data (id, ts_date, value) VALUES (1, '2010-02-03', 10.22);
INSERT INTO time_series_data (id, ts_date, value) VALUES (1, '2010-02-04', 10.23);



