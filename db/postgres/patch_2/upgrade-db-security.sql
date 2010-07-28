
-- Security master upgrade from patch_1

ALTER TABLE sec_identifier_association
  ALTER validStartDate TYPE TIMESTAMP,
  ALTER validEndDate  TYPE TIMESTAMP;

ALTER TABLE sec_equity
  ALTER effectiveDateTime TYPE TIMESTAMP,
  ALTER lastModifiedDateTime TYPE TIMESTAMP,
  ADD shortName VARCHAR(255);

ALTER TABLE sec_option
  ALTER effectiveDateTime TYPE TIMESTAMP,
  ALTER lastModifiedDateTime TYPE TIMESTAMP,
  ALTER expiry TYPE TIMESTAMP,
  ADD expiry_accuracy SMALLINT NOT NULL DEFAULT 3,
  ADD payment DOUBLE PRECISION,
  ADD lowerbound DOUBLE PRECISION,
  ADD upperbound DOUBLE PRECISION,
  ADD choose_date TIMESTAMP,
  ADD choose_zone VARCHAR(50),
  ADD underlyingstrike DOUBLE PRECISION,
  ADD underlyingexpiry_date TIMESTAMP,
  ADD underlyingexpiry_accuracy SMALLINT,
  ADD reverse BOOL; 
ALTER TABLE sec_option
  ALTER expiry_accuracy DROP DEFAULT;
ALTER TABLE sec_option RENAME expiry TO expiry_date;

ALTER TABLE sec_bond
  ALTER effectiveDateTime TYPE TIMESTAMP,
  ALTER lastModifiedDateTime TYPE TIMESTAMP,
  ALTER maturity TYPE TIMESTAMP,
  ALTER announcementdate TYPE TIMESTAMP,
  ALTER interestaccrualdate TYPE TIMESTAMP,
  ALTER settlementdate TYPE TIMESTAMP,
  ALTER firstcoupondate TYPE TIMESTAMP,
  ADD maturity_accuracy SMALLINT NOT NULL DEFAULT 3,
  ADD announcement_zone VARCHAR(50) NOT NULL DEFAULT 'UTC',
  ADD interestaccrual_zone VARCHAR(50) NOT NULL DEFAULT 'UTC',
  ADD settlement_zone VARCHAR(50) NOT NULL DEFAULT 'UTC',
  ADD firstcoupon_zone VARCHAR(50) NOT NULL DEFAULT 'UTC';
ALTER TABLE sec_bond RENAME maturity TO maturity_date;
ALTER TABLE sec_bond RENAME announcementdate TO announcement_date;
ALTER TABLE sec_bond RENAME interestaccrualdate TO interestaccrual_date;
ALTER TABLE sec_bond RENAME settlementdate TO settlement_date;
ALTER TABLE sec_bond RENAME firstcoupondate TO firstcoupon_date;
ALTER TABLE sec_bond
  ALTER maturity_accuracy DROP DEFAULT,
  ALTER announcement_zone DROP DEFAULT,
  ALTER interestaccrual_zone DROP DEFAULT,
  ALTER settlement_zone DROP DEFAULT,
  ALTER firstcoupon_zone DROP DEFAULT;

ALTER TABLE sec_future
  ALTER effectiveDateTime TYPE TIMESTAMP,
  ALTER lastModifiedDateTime TYPE TIMESTAMP,
  ALTER expiry TYPE TIMESTAMP,
  ADD expiry_accuracy SMALLINT NOT NULL DEFAULT 3;
ALTER TABLE sec_future RENAME expiry TO expiry_date;
ALTER TABLE sec_future
  ALTER expiry_accuracy DROP DEFAULT;

ALTER TABLE sec_futurebundle
  ALTER startDate TYPE TIMESTAMP,
  ALTER endDate TYPE TIMESTAMP;

ALTER TABLE sec_cash
  ALTER effectiveDateTime TYPE TIMESTAMP,
  ALTER lastModifiedDateTime TYPE TIMESTAMP;

ALTER TABLE sec_fra
  ALTER effectiveDateTime TYPE TIMESTAMP,
  ALTER lastModifiedDateTime TYPE TIMESTAMP,
  ALTER startDate TYPE TIMESTAMP,
  ALTER endDate TYPE TIMESTAMP,
  ADD start_zone VARCHAR(50) NOT NULL DEFAULT 'UTC',
  ADD end_zone VARCHAR(50) NOT NULL DEFAULT 'UTC';
ALTER TABLE sec_fra RENAME startDate TO start_date;
ALTER TABLE sec_fra RENAME endDate TO end_date;
ALTER TABLE sec_fra
  ALTER start_zone DROP DEFAULT,
  ALTER end_zone DROP DEFAULT;

ALTER TABLE sec_swap
  ALTER effectiveDateTime TYPE TIMESTAMP,
  ALTER lastModifiedDateTime TYPE TIMESTAMP,
  ALTER tradedate TYPE TIMESTAMP,
  ALTER effectivedate TYPE TIMESTAMP,
  ALTER maturitydate TYPE TIMESTAMP,
  ALTER forwardstartdate TYPE TIMESTAMP,
  ADD trade_zone VARCHAR(50) NOT NULL DEFAULT 'UTC',
  ADD effective_zone VARCHAR(50) NOT NULL DEFAULT 'UTC',
  ADD maturity_zone VARCHAR(50) NOT NULL DEFAULT 'UTC',
  ADD forwardstart_zone VARCHAR(50);
ALTER TABLE sec_swap RENAME tradedate TO trade_date;
ALTER TABLE sec_swap RENAME effectivedate TO effective_date;
ALTER TABLE sec_swap RENAME maturitydate TO maturity_date;
ALTER TABLE sec_swap RENAME forwardstartdate TO forwardstart_date;
ALTER TABLE sec_swap
  ALTER trade_zone DROP DEFAULT,
  ALTER effective_zone DROP DEFAULT,
  ALTER maturity_zone DROP DEFAULT,
  ALTER forwardstart_zone DROP DEFAULT;
UPDATE sec_swap SET forwardstart_zone='UTC' WHERE forwardstart_date IS NOT NULL;