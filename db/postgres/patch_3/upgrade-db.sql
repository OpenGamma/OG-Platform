DROP TABLE tss_data_point_delta;

CREATE TABLE tss_data_point_delta (
	meta_data_id BIGINT NOT NULL
	  constraint fk_dp_delta_meta_data  REFERENCES tss_meta_data (id),
	time_stamp TIMESTAMP NOT NULL,
	ts_date date NOT NULL,
	old_value DOUBLE PRECISION NOT NULL,
	operation char(1) NOT NULL
	 CONSTRAINT operation_constraint CHECK ( operation IN ('I', 'U', 'D', 'Q'))
);

CREATE TABLE tss_intraday_data_point (
	meta_data_id BIGINT NOT NULL
	  constraint fk_i_dp_meta_data  REFERENCES tss_meta_data (id),
	ts_date TIMESTAMP NOT NULL,
	value DOUBLE PRECISION NOT NULL,
	PRIMARY KEY (meta_data_id, ts_date)
);

CREATE TABLE tss_intraday_data_point_delta (
	meta_data_id BIGINT NOT NULL
	  constraint fk_i_dp_delta_meta_data  REFERENCES tss_meta_data (id),
	time_stamp TIMESTAMP NOT NULL,
	ts_date TIMESTAMP NOT NULL,
	old_value DOUBLE PRECISION NOT NULL,
	operation char(1) NOT NULL
	 CONSTRAINT operation_constraint_i CHECK ( operation IN ('I', 'U', 'D', 'Q'))
);