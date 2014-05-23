/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorCMSTest {
  //Swap 5Y
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(5);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 3, 17);
  //Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, 1.0, RATE, FIXED_IS_PAYER);
  //Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, 1.0, IBOR_INDEX, !FIXED_IS_PAYER, CALENDAR);
  // CMS coupon construction
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = new SwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2010, 12, 30);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 5);
  private static final DayCount PAYMENT_DAY_COUNT = DayCounts.ACT_360;
  private static final double ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final CouponCMSDefinition CMS_COUPON_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION,
      CMS_INDEX);
  // Cap/Floor construction
  private static final double STRIKE = 0.04;
  private static final boolean IS_CAP = true;
  private static final CapFloorCMSDefinition CMS_CAP_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, STRIKE, IS_CAP);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);

  private static final CapFloorCMS CMS_CAP = (CapFloorCMS) CMS_CAP_DEFINITION.toDerivative(REFERENCE_DATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCMSCoupon() {
    CapFloorCMSDefinition.from(null, STRIKE, IS_CAP);
  }

  @Test
  public void testFrom() {
    final CapFloorCMSDefinition capConstructor = new CapFloorCMSDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION, CMS_INDEX,
        STRIKE, IS_CAP);
    final CapFloorCMSDefinition capFrom = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, STRIKE, IS_CAP);
    assertEquals(capConstructor, capFrom);
  }

  @Test
  public void testGetter() {
    assertEquals(STRIKE, CMS_CAP.getStrike(), 1E-10);
    assertEquals(IS_CAP, CMS_CAP.isCap());
  }

  @Test
  public void testWithNotional() {
    final double notional = NOTIONAL + 1000;
    final CapFloorCMS capFloorCMS = new CapFloorCMS(CUR, ACCRUAL_FACTOR, RATE, NOTIONAL, ACCRUAL_FACTOR, CMS_CAP.getUnderlyingSwap(), SETTLEMENT_DAYS, STRIKE, IS_CAP);
    final CapFloorCMS expected = new CapFloorCMS(CUR, ACCRUAL_FACTOR, RATE, notional, ACCRUAL_FACTOR, CMS_CAP.getUnderlyingSwap(), SETTLEMENT_DAYS, STRIKE, IS_CAP);
    assertEquals(expected, capFloorCMS.withNotional(notional));
  }

  @Test
  public void testHashCodeEquals() {
    final CapFloorCMS capFloorCMS = new CapFloorCMS(CUR, ACCRUAL_FACTOR, RATE, NOTIONAL, ACCRUAL_FACTOR, CMS_CAP.getUnderlyingSwap(), SETTLEMENT_DAYS, STRIKE, IS_CAP);
    CapFloorCMS other = new CapFloorCMS(CUR, ACCRUAL_FACTOR, RATE, NOTIONAL, ACCRUAL_FACTOR, CMS_CAP.getUnderlyingSwap(), SETTLEMENT_DAYS, STRIKE, IS_CAP);
    assertEquals(capFloorCMS, other);
    assertEquals(capFloorCMS.hashCode(), other.hashCode());
    other = new CapFloorCMS(Currency.AUD, ACCRUAL_FACTOR, RATE, NOTIONAL, ACCRUAL_FACTOR, CMS_CAP.getUnderlyingSwap(), SETTLEMENT_DAYS, STRIKE, IS_CAP);
    assertFalse(other.equals(capFloorCMS));
    other = new CapFloorCMS(CUR, ACCRUAL_FACTOR + 1, RATE, NOTIONAL, ACCRUAL_FACTOR, CMS_CAP.getUnderlyingSwap(), SETTLEMENT_DAYS, STRIKE, IS_CAP);
    assertFalse(other.equals(capFloorCMS));
    other = new CapFloorCMS(CUR, ACCRUAL_FACTOR, RATE + 0.01, NOTIONAL, ACCRUAL_FACTOR, CMS_CAP.getUnderlyingSwap(), SETTLEMENT_DAYS, STRIKE, IS_CAP);
    assertFalse(other.equals(capFloorCMS));
    other = new CapFloorCMS(CUR, ACCRUAL_FACTOR, RATE, NOTIONAL + 1, ACCRUAL_FACTOR, CMS_CAP.getUnderlyingSwap(), SETTLEMENT_DAYS, STRIKE, IS_CAP);
    assertFalse(other.equals(capFloorCMS));
    other = new CapFloorCMS(CUR, ACCRUAL_FACTOR, RATE, NOTIONAL, ACCRUAL_FACTOR + 1, CMS_CAP.getUnderlyingSwap(), SETTLEMENT_DAYS, STRIKE, IS_CAP);
    assertFalse(other.equals(capFloorCMS));
    other = new CapFloorCMS(CUR, ACCRUAL_FACTOR, RATE, NOTIONAL, ACCRUAL_FACTOR, CMS_CAP.getUnderlyingSwap().withNotional(NOTIONAL + 1), SETTLEMENT_DAYS, STRIKE, IS_CAP);
    assertFalse(other.equals(capFloorCMS));
    other = new CapFloorCMS(CUR, ACCRUAL_FACTOR, RATE, NOTIONAL, ACCRUAL_FACTOR, CMS_CAP.getUnderlyingSwap(), SETTLEMENT_DAYS + 1, STRIKE, IS_CAP);
    assertFalse(other.equals(capFloorCMS));
    other = new CapFloorCMS(CUR, ACCRUAL_FACTOR, RATE, NOTIONAL, ACCRUAL_FACTOR, CMS_CAP.getUnderlyingSwap(), SETTLEMENT_DAYS, STRIKE + 0.01, IS_CAP);
    assertFalse(other.equals(capFloorCMS));
    other = new CapFloorCMS(CUR, ACCRUAL_FACTOR, RATE, NOTIONAL, ACCRUAL_FACTOR, CMS_CAP.getUnderlyingSwap(), SETTLEMENT_DAYS, STRIKE, !IS_CAP);
    assertFalse(other.equals(capFloorCMS));
  }
}
