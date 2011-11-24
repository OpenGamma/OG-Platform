CREATE TABLE sec_nondeliverablefxforward (
  id bigint NOT NULL,
  security_id bigint NOT NULL,
  region_scheme varchar(255) NOT NULL,
  region_identifier varchar(255) NOT NULL,
  underlying_scheme varchar(255) NOT NULL,
  underlying_identifier varchar(255) NOT NULL,
  forward_date timestamp without time zone NOT NULL,
  forward_zone varchar(50) NOT NULL,
  is_delivery_in_receive_currency boolean NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT sec_fk_nondeliverablefxforward2sec FOREIGN KEY (security_id) REFERENCES sec_security (id)
);
CREATE INDEX ix_sec_nondeliverablefxforward_security_id ON sec_nondeliverablefxforward(security_id);