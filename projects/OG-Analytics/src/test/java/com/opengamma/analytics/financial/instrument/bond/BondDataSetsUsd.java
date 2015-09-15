/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.index.IndexPriceMaster;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Examples of bond and bills to be used in tests. Examples in GBP.
 */
public class BondDataSetsUsd {
  
  private static final String US_GOVT_NAME = "US GOVT";
  private static final Set<CreditRating> RATING = new HashSet<>();
  static {
    RATING.add(CreditRating.of("AA", "OG_RATING", true));
  }
  private static final LegalEntity US_GOVT_LEGAL_ENTITY = new LegalEntity("USGOVT", US_GOVT_NAME, RATING, null, null);
  private static final Currency USD = Currency.USD;
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final IndexPriceMaster MASTER_PRICE_INDEX = IndexPriceMaster.getInstance();
  private static final IndexPrice USCPI = MASTER_PRICE_INDEX.getIndex("USCPI");

  /** =====     Bonds     ===== */

  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final int SETTLEMENT_DAYS = 1;
  private static final int EX_DIVIDEND_DAYS = 0;
  private static final YieldConvention YIELD_BOND = 
      YieldConventionFactory.INSTANCE.getYieldConvention("US street");
  
  //UST 0.50 2016-09-30 - ISIN-US912828F478
  private static final ZonedDateTime START_ACCRUAL_DATE_US16 = DateUtils.getUTCDate(2014, 9, 30);
  private static final ZonedDateTime FIRST_COUPON_DATE_US16 = DateUtils.getUTCDate(2015, 3, 31);
  private static final ZonedDateTime MATURITY_DATE_US16 = DateUtils.getUTCDate(2016, 9, 30);
  private static final double RATE_US16 = 0.0500;

  //UST 1.75 2019-09-30 - ISIN-US912828F395
  private static final ZonedDateTime START_ACCRUAL_DATE_US19 = DateUtils.getUTCDate(2014, 9, 30);
  private static final ZonedDateTime FIRST_COUPON_DATE_US19 = DateUtils.getUTCDate(2015, 3, 31);
  private static final ZonedDateTime MATURITY_DATE_US19 = DateUtils.getUTCDate(2019, 9, 30);
  private static final double RATE_US19 = 0.0175;

  //UST 2.375 2024-08-15 - ISIN-US912828D564
  private static final ZonedDateTime START_ACCRUAL_DATE_US24 = DateUtils.getUTCDate(2014, 8, 15);
  private static final ZonedDateTime FIRST_COUPON_DATE_US24 = DateUtils.getUTCDate(2015, 2, 15);
  private static final ZonedDateTime MATURITY_DATE_US24 = DateUtils.getUTCDate(2024, 8, 15);
  private static final double RATE_US24 = 0.02375;
  
  /**
   * Returns the legal entity used for the US GOVT bonds and bills.
   * @return The legal entity.
   */
  public static LegalEntity getLegalEntityUsGovt() {
    return US_GOVT_LEGAL_ENTITY;
  }

  /**
   * Returns the definition of the UST 0.50 2016-09-30 - ISIN-US912828F478 bond security.
   * @param notional The bond notional.
   * @return The bond.
   */
  public static BondFixedSecurityDefinition bondUST_20160930(double notional) {
    return BondFixedSecurityDefinition.from(USD, START_ACCRUAL_DATE_US16, FIRST_COUPON_DATE_US16, MATURITY_DATE_US16,
        PAYMENT_TENOR, RATE_US16, SETTLEMENT_DAYS, notional, EX_DIVIDEND_DAYS, NYC, DAY_COUNT, BUSINESS_DAY, YIELD_BOND,
        IS_EOM, US_GOVT_LEGAL_ENTITY);
  }

  /**
   * Returns the definition of the UST 1.75 2019-09-30 - ISIN-US912828F395 bond security.
   * @param notional The bond notional.
   * @return The bond.
   */
  public static BondFixedSecurityDefinition bondUST_20190930(double notional) {
    return BondFixedSecurityDefinition.from(USD, START_ACCRUAL_DATE_US19, FIRST_COUPON_DATE_US19, MATURITY_DATE_US19,
        PAYMENT_TENOR, RATE_US19, SETTLEMENT_DAYS, notional, EX_DIVIDEND_DAYS, NYC, DAY_COUNT, BUSINESS_DAY, YIELD_BOND,
        IS_EOM, US_GOVT_LEGAL_ENTITY);
  }

  /**
   * Returns the definition of the UST 2.375 2024-08-15 - ISIN-US912828D564 bond security.
   * @param notional The bond notional.
   * @return The bond.
   */
  public static BondFixedSecurityDefinition bondUST_20240815(double notional) {
    return BondFixedSecurityDefinition.from(USD, START_ACCRUAL_DATE_US24, FIRST_COUPON_DATE_US24, MATURITY_DATE_US24,
        PAYMENT_TENOR, RATE_US24, SETTLEMENT_DAYS, notional, EX_DIVIDEND_DAYS, NYC, DAY_COUNT, BUSINESS_DAY, YIELD_BOND,
        IS_EOM, US_GOVT_LEGAL_ENTITY);
  }

  /** =====     Bills     ===== */
  
  private static final YieldConvention YIELD_BILL = YieldConventionFactory.INSTANCE.getYieldConvention("DISCOUNT");
  private static final DayCount DAY_COUNT_BILL = DayCounts.ACT_360;
  private static final int SPOT_LAG_BILL = 1;

  /**
   * Returns the definition of a US Treasury bill. 
   * @param notional The bill notional.
   * @param maturityDate The bill maturity date.
   * @return
   */
  public static BillSecurityDefinition billUS(double notional, ZonedDateTime maturityDate) {
    return new BillSecurityDefinition(USD, maturityDate, notional, SPOT_LAG_BILL, NYC, 
        YIELD_BILL, DAY_COUNT_BILL, US_GOVT_LEGAL_ENTITY);
  }

  /** =====     TIPS     ===== */
  
  private static final boolean IS_EOM_TIPS = false;
  private static final int MONTH_LAG_TIPS = 3;
  private static final Period COUPON_PERIOD_TIPS = Period.ofMonths(6);
  private static final YieldConvention YIELD_CONVENTION_TIPS = SimpleYieldConvention.US_IL_REAL;
  private static final int SETTLEMENT_DAYS_TIPS = 2;
  private static final double NOTIONAL_TIPS = 1.00;
  
  // 2% 10-YEAR TREASURY INFLATION-PROTECTED SECURITIES (TIPS) Due January 15, 2016 - US912828ET33
  private static final ZonedDateTime START_DATE_TIPS_16 = DateUtils.getUTCDate(2006, 1, 15);
  private static final ZonedDateTime MATURITY_DATE_TIPS_16 = DateUtils.getUTCDate(2016, 1, 15);
  private static final double INDEX_START_TIPS_16 = 198.47742; // Date:
  private static final double REAL_RATE_TIPS_16 = 0.02;

  /**
   * Returns the definition of the TIPS 2.00 2016-01-15 - ISIN-US912828ET33 security.
   * @param notional The bond notional.
   * @return The bond.
   */
  public static BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> 
    bondTIPS_20160115(double notional) {
    return BondCapitalIndexedSecurityDefinition.fromInterpolation(USCPI, MONTH_LAG_TIPS, 
        START_DATE_TIPS_16, INDEX_START_TIPS_16, MATURITY_DATE_TIPS_16, COUPON_PERIOD_TIPS, NOTIONAL_TIPS, 
        REAL_RATE_TIPS_16, BUSINESS_DAY, SETTLEMENT_DAYS_TIPS, NYC, DAY_COUNT, YIELD_CONVENTION_TIPS, IS_EOM_TIPS, 
        US_GOVT_LEGAL_ENTITY);
  }
  
  
  

}
