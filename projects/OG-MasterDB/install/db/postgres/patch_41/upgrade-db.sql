
CREATE TABLE sec_equity_index_dividend_futureoption (
    id bigint NOT NULL,
    security_id bigint NOT NULL,
    option_exercise_type varchar(32) NOT NULL,
    option_type varchar(32) NOT NULL,
    strike double precision NOT NULL,
    expiry_date timestamp without time zone NOT NULL,
    expiry_zone varchar(50) NOT NULL,
    expiry_accuracy smallint NOT NULL,
    underlying_scheme varchar(255) NOT NULL,
    underlying_identifier varchar(255) NOT NULL,
    currency_id bigint NOT NULL,
    exchange_id bigint NOT NULL,
    margined boolean NOT NULL,
    pointValue double precision NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT sec_equity_index_dividend_futureoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
    CONSTRAINT sec_equity_index_dividend_futureoption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
    CONSTRAINT sec_equity_index_dividend_futureoption2exchange FOREIGN KEY (exchange_id) REFERENCES sec_exchange (id)
);
CREATE INDEX ix_sec_equity_index_dividend_futureoption_security_id ON sec_equity_index_dividend_futureoption(security_id);