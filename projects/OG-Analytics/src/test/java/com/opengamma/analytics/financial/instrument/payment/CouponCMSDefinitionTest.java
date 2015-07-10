/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to CouponCMSDefinition construction.
 */
@Test(groups = TestGroup.UNIT)
public class CouponCMSDefinitionTest {

  //Swap 2Y
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
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
  private static final ZonedDateTime FAKE_DATE = DateUtils.getUTCDate(0, 1, 1);
  private static final CouponFloatingDefinition COUPON = new CouponIborDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FAKE_DATE, IBOR_INDEX, CALENDAR);
  private static final CouponFloatingDefinition FLOAT_COUPON = CouponIborDefinition.from(COUPON, FIXING_DATE, IBOR_INDEX, CALENDAR);
  private static final CouponCMSDefinition CMS_COUPON_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION,
      CMS_INDEX);
  private static final CouponCMSDefinition CMS_COUPON_2 = CouponCMSDefinition.from(FLOAT_COUPON, SWAP_DEFINITION, CMS_INDEX);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final String FUNDING_CURVE_NAME = " Funding";
  private static final String FORWARD_CURVE_NAME = " Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME };
  private static final double FIXING_RATE = 0.04;
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {FIXING_DATE }, new double[] {FIXING_RATE });

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    CouponCMSDefinition.from(null, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION, CMS_INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualStartDate() {
    CouponCMSDefinition.from(PAYMENT_DATE, null, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION, CMS_INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualEndDate() {
    CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, null, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION, CMS_INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixingDate() {
    CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, null, SWAP_DEFINITION, CMS_INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSwap() {
    CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, null, CMS_INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCMSIndex() {
    CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromNullCoupon() {
    CouponCMSDefinition.from(null, SWAP_DEFINITION, CMS_INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromNullSwap() {
    CouponCMSDefinition.from(FLOAT_COUPON, (SwapFixedIborDefinition) null, CMS_INDEX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromNullCoupons() {
    CouponCMSDefinition.from(null, CMS_INDEX, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromNullIndex() {
    CouponCMSDefinition.from(FLOAT_COUPON, null, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionNullFixingData() {
    CMS_COUPON_DEFINITION.toDerivative(FIXING_DATE, (DoubleTimeSeries<ZonedDateTime>) null);
  }

  @Test
  public void test() {
    assertEquals(CMS_COUPON_DEFINITION.getPaymentDate(), COUPON.getPaymentDate());
    assertEquals(CMS_COUPON_DEFINITION.getAccrualStartDate(), COUPON.getAccrualStartDate());
    assertEquals(CMS_COUPON_DEFINITION.getAccrualEndDate(), COUPON.getAccrualEndDate());
    assertEquals(CMS_COUPON_DEFINITION.getPaymentYearFraction(), COUPON.getPaymentYearFraction(), 1E-10);
    assertEquals(CMS_COUPON_DEFINITION.getNotional(), COUPON.getNotional(), 1E-2);
    assertEquals(CMS_COUPON_DEFINITION.getFixingDate(), FIXING_DATE);
    assertEquals(CMS_COUPON_DEFINITION.getUnderlyingSwap(), SWAP_DEFINITION);
    assertEquals(CMS_COUPON_2.getPaymentDate(), COUPON.getPaymentDate());
    assertEquals(CMS_COUPON_2.getAccrualStartDate(), COUPON.getAccrualStartDate());
    assertEquals(CMS_COUPON_2.getAccrualEndDate(), COUPON.getAccrualEndDate());
    assertEquals(CMS_COUPON_2.getPaymentYearFraction(), COUPON.getPaymentYearFraction(), 1E-10);
    assertEquals(CMS_COUPON_2.getNotional(), COUPON.getNotional(), 1E-2);
    assertEquals(CMS_COUPON_2.getFixingDate(), FIXING_DATE);
    assertEquals(CMS_COUPON_2.getUnderlyingSwap(), SWAP_DEFINITION);
  }

  @Test
  public void testToDerivativeBeforeFixing() {
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final double paymentTime = actAct.getDayCountFraction(REFERENCE_DATE, PAYMENT_DATE);
    final double fixingTime = actAct.getDayCountFraction(REFERENCE_DATE, FIXING_DATE);
    final double settlementTime = actAct.getDayCountFraction(REFERENCE_DATE, SWAP_DEFINITION.getFixedLeg().getNthPayment(0).getAccrualStartDate());
    final SwapFixedCoupon<? extends Payment> convertedSwap = SWAP_DEFINITION.toDerivative(REFERENCE_DATE);
    final CouponCMS couponCMS = new CouponCMS(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, fixingTime, convertedSwap, settlementTime);
    assertEquals(couponCMS, CMS_COUPON_DEFINITION.toDerivative(REFERENCE_DATE));
    assertEquals(couponCMS, CMS_COUPON_DEFINITION.toDerivative(REFERENCE_DATE, FIXING_TS));
  }

  @Test
  public void testToDerivativeAfterFixing() {
    final ZonedDateTime date = FIXING_DATE.plusDays(2);
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    double paymentTime = actAct.getDayCountFraction(date, PAYMENT_DATE);
    CouponFixed couponFixed = new CouponFixed(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, FIXING_RATE);
    assertEquals(couponFixed, CMS_COUPON_DEFINITION.toDerivative(date, FIXING_TS));
    paymentTime = actAct.getDayCountFraction(FIXING_DATE, PAYMENT_DATE);
    couponFixed = new CouponFixed(CUR, paymentTime, ACCRUAL_FACTOR, NOTIONAL, FIXING_RATE);
    assertEquals(couponFixed, CMS_COUPON_DEFINITION.toDerivative(FIXING_DATE, FIXING_TS));
  }
}
