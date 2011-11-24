CREATE TABLE sec_nondeliverablefxoption (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    option_exercise_type varchar(32) NOT NULL,
    put_amount double precision NOT NULL,
    call_amount double precision NOT NULL,
    expiry_date timestamp without time zone NOT NULL,
    expiry_zone varchar(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    put_currency_id bigint NOT NULL,
    call_currency_id bigint NOT NULL,
    settlement_date timestamp without time zone NOT NULL,
    settlement_zone varchar(50) NOT NULL,
    is_long boolean NOT NULL,
	is_delivery_in_call_currency boolean NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_fk_nondeliverablefxoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_fk_nondeliverablefxoption2putcurrency FOREIGN KEY (put_currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_fk_nondeliverablefxoption2callcurrency FOREIGN KEY (call_currency_id) REFERENCES sec_currency (id)
);
CREATE INDEX ix_sec_nondeliverablefxoption_security_id ON sec_nondeliverablefxoption(security_id);