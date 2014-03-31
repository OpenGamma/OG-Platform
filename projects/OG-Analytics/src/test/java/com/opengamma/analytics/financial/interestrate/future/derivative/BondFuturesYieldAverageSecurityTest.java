/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the definition of Yield average bond futures (in particular for AUD-SFE futures).
 */
@Test(groups = TestGroup.UNIT)
public class BondFuturesYieldAverageSecurityTest {
  
  // Bonds: Delivery basket SFE 10Y
  private static final Currency AUD = Currency.AUD;
  // AUD defaults
  private static final LegalEntity ISSUER_LEGAL_ENTITY = IssuerProviderDiscountDataSets.getIssuersAUS();
  private static final Period PAYMENT_TENOR = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final int SETTLEMENT_DAYS = 3;
  private static final int EX_DIVIDEND_DAYS = 7;
  private static final YieldConvention YIELD_CONVENTION = SimpleYieldConvention.AUSTRALIA_EX_DIVIDEND;
  private static final double NOTIONAL_BOND = 100;
  private static final double NOTIONAL_FUTURES = 10000;
  
  private static final ZonedDateTime LAST_TRADING_DATE = DateUtils.getUTCDate(2014, 3, 17);
  private static final ZonedDateTime DELIVERY_DATE = ScheduleCalculator.getAdjustedDate(LAST_TRADING_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 1, 10);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double LAST_TRADING_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, LAST_TRADING_DATE);
//  private static final double SPOT_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, SPOT_DATE);
  // ASX 10 Year Bond Contract - March 14
  private static final double[] UNDERLYING_COUPON = {0.0575, 0.0550, 0.0275, 0.0325};
  private static final ZonedDateTime[] UNDERLYING_MATURITY_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2022, 7, 15), DateUtils.getUTCDate(2023, 4, 15),
    DateUtils.getUTCDate(2024, 4, 15), DateUtils.getUTCDate(2025, 4, 15)};
  private static final int NB_BOND = UNDERLYING_COUPON.length;
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[NB_BOND];
  private static final BondFixedSecurityDefinition[] BASKET_SECURITY_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  private static final BondFixedSecurity[] BASKET_SECURITY_SETTLEDELIVERY = new BondFixedSecurity[NB_BOND];
  private static final BondFixedSecurity[] BASKET_SECURITY_STANDARD = new BondFixedSecurity[NB_BOND];
  static {
    for(int loopbond=0; loopbond<NB_BOND; loopbond++) {
      START_ACCRUAL_DATE[loopbond] = UNDERLYING_MATURITY_DATE[loopbond].minusYears(12);
      BASKET_SECURITY_DEFINITION[loopbond] = BondFixedSecurityDefinition.from(AUD, START_ACCRUAL_DATE[loopbond], UNDERLYING_MATURITY_DATE[loopbond], PAYMENT_TENOR,
          UNDERLYING_COUPON[loopbond], SETTLEMENT_DAYS, NOTIONAL_BOND, EX_DIVIDEND_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM, ISSUER_LEGAL_ENTITY, "Repo");
      BASKET_SECURITY_SETTLEDELIVERY[loopbond] = BASKET_SECURITY_DEFINITION[loopbond].toDerivative(REFERENCE_DATE, DELIVERY_DATE);
      BASKET_SECURITY_STANDARD[loopbond] = BASKET_SECURITY_DEFINITION[loopbond].toDerivative(REFERENCE_DATE, SPOT_DATE);
    }
  }
  private static final double SYNTHETIC_COUPON = 0.06;
  private static final int TENOR = 10;
  
  private static final BondFuturesYieldAverageSecurity FUT_SEC = new BondFuturesYieldAverageSecurity(LAST_TRADING_TIME, BASKET_SECURITY_SETTLEDELIVERY, 
      BASKET_SECURITY_STANDARD, SYNTHETIC_COUPON, TENOR, NOTIONAL_FUTURES);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBasketDelivery() {
    new BondFuturesYieldAverageSecurity(LAST_TRADING_TIME, null, BASKET_SECURITY_STANDARD, SYNTHETIC_COUPON, TENOR, NOTIONAL_FUTURES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullBasketStandard() {
    new BondFuturesYieldAverageSecurity(LAST_TRADING_TIME, BASKET_SECURITY_SETTLEDELIVERY, null, SYNTHETIC_COUPON, TENOR, NOTIONAL_FUTURES);
  }

  @Test
  public void getter() {
    assertEquals("YieldAverageBondFuturesSecurity: getter", LAST_TRADING_TIME, FUT_SEC.getTradingLastTime());
    assertEquals("YieldAverageBondFuturesSecurity: getter", SYNTHETIC_COUPON, FUT_SEC.getCouponRate());
    assertEquals("YieldAverageBondFuturesSecurity: getter", TENOR, FUT_SEC.getTenor());
    assertEquals("YieldAverageBondFuturesSecurity: getter", NOTIONAL_FUTURES, FUT_SEC.getNotional());
  }
  
  @Test
  public void equalHash() {
    final BondFuturesYieldAverageSecurity other = new BondFuturesYieldAverageSecurity(LAST_TRADING_TIME, BASKET_SECURITY_SETTLEDELIVERY, 
      BASKET_SECURITY_STANDARD, SYNTHETIC_COUPON, TENOR, NOTIONAL_FUTURES);
    assertEquals("YieldAverageBondFuturesSecurity: equal - hash", FUT_SEC, other);
    assertEquals("YieldAverageBondFuturesSecurity: equal - hash", FUT_SEC.hashCode(), other.hashCode());
    BondFuturesYieldAverageSecurity modified;
    modified = new BondFuturesYieldAverageSecurity(LAST_TRADING_TIME+0.1, BASKET_SECURITY_SETTLEDELIVERY, BASKET_SECURITY_STANDARD, SYNTHETIC_COUPON, TENOR, NOTIONAL_FUTURES);
    assertFalse("YieldAverageBondFuturesSecurity: equal - hash", FUT_SEC.equals(modified));
    modified = new BondFuturesYieldAverageSecurity(LAST_TRADING_TIME, BASKET_SECURITY_STANDARD, BASKET_SECURITY_STANDARD, SYNTHETIC_COUPON, TENOR, NOTIONAL_FUTURES);
    assertFalse("YieldAverageBondFuturesSecurity: equal - hash", FUT_SEC.equals(modified));
    modified = new BondFuturesYieldAverageSecurity(LAST_TRADING_TIME, BASKET_SECURITY_SETTLEDELIVERY, BASKET_SECURITY_SETTLEDELIVERY, SYNTHETIC_COUPON, TENOR, NOTIONAL_FUTURES);
    assertFalse("YieldAverageBondFuturesSecurity: equal - hash", FUT_SEC.equals(modified));
    modified = new BondFuturesYieldAverageSecurity(LAST_TRADING_TIME, BASKET_SECURITY_SETTLEDELIVERY, BASKET_SECURITY_STANDARD, SYNTHETIC_COUPON+0.01, TENOR, NOTIONAL_FUTURES);
    assertFalse("YieldAverageBondFuturesSecurity: equal - hash", FUT_SEC.equals(modified));
    modified = new BondFuturesYieldAverageSecurity(LAST_TRADING_TIME, BASKET_SECURITY_SETTLEDELIVERY, BASKET_SECURITY_STANDARD, SYNTHETIC_COUPON, TENOR+1, NOTIONAL_FUTURES);
    assertFalse("YieldAverageBondFuturesSecurity: equal - hash", FUT_SEC.equals(modified));
    modified = new BondFuturesYieldAverageSecurity(LAST_TRADING_TIME, BASKET_SECURITY_SETTLEDELIVERY, BASKET_SECURITY_STANDARD, SYNTHETIC_COUPON, TENOR, NOTIONAL_FUTURES*10);
    assertFalse("YieldAverageBondFuturesSecurity: equal - hash", FUT_SEC.equals(modified));
  }
  
}
