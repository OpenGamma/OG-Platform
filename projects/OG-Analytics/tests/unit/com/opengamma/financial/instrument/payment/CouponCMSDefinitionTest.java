/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.ZZZSwapFixedIborDefinition;
import com.opengamma.financial.interestrate.payments.CouponCMS;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Tenor;

/**
 * Test related to CouponCMSDefinition construction.
 */
public class CouponCMSDefinitionTest {

  //Swap 2Y
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Tenor ANNUITY_TENOR = new Tenor(Period.ofYears(2));
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2011, 3, 17);
  //  private static final ZonedDateTime MATURITY_DATE = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, BUSINESS_DAY, CALENDAR, IS_EOM, ANNUITY_TENOR);
  //Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, 1.0, RATE, FIXED_IS_PAYER);
  //Ibor leg: quarterly money
  private static final Tenor INDEX_TENOR = new Tenor(Period.ofMonths(3));
  //  private static final PeriodFrequency INDEX_FREQUENCY = PeriodFrequency.QUARTERLY;
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, 1.0, INDEX, !FIXED_IS_PAYER);

  private static final ZZZSwapFixedIborDefinition SWAP = new ZZZSwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY);

  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_DATE = DateUtil.getUTCDate(2010, 12, 30);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtil.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtil.getUTCDate(2011, 4, 5);
  private static final DayCount PAYMENT_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final double ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m

  private static final ZonedDateTime FAKE_DATE = DateUtil.getUTCDate(0, 1, 1);
  private static final CouponFloatingDefinition COUPON = new CouponFloatingDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FAKE_DATE);
  private static final CouponFloatingDefinition FLOAT_COUPON = CouponFloatingDefinition.from(COUPON, FIXING_DATE);

  private static final CouponCMSDefinition CMS_COUPON = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP);
  private static final CouponCMSDefinition CMS_COUPON_2 = CouponCMSDefinition.from(FLOAT_COUPON, SWAP);

  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 12, 27); //For conversion to derivative

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    CouponCMSDefinition.from(null, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualStartDate() {
    CouponCMSDefinition.from(PAYMENT_DATE, null, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualEndDate() {
    CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, null, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixingDate() {
    CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, null, SWAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSwap() {
    CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromNullCoupon() {
    CouponCMSDefinition.from(null, SWAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromNullSwap() {
    ZZZSwapFixedIborDefinition NullSwap = null; // To remove .from ambiguity
    CouponCMSDefinition.from(FLOAT_COUPON, NullSwap);
  }

  @Test
  public void test() {
    assertEquals(CMS_COUPON.getPaymentDate(), COUPON.getPaymentDate());
    assertEquals(CMS_COUPON.getAccrualStartDate(), COUPON.getAccrualStartDate());
    assertEquals(CMS_COUPON.getAccrualEndDate(), COUPON.getAccrualEndDate());
    assertEquals(CMS_COUPON.getPaymentYearFraction(), COUPON.getPaymentYearFraction(), 1E-10);
    assertEquals(CMS_COUPON.getNotional(), COUPON.getNotional(), 1E-2);
    assertEquals(CMS_COUPON.getFixingDate(), FIXING_DATE);
    assertEquals(CMS_COUPON.isFixed(), false);
    assertEquals(CMS_COUPON.getUnderlyingSwap(), SWAP);
    assertEquals(CMS_COUPON_2.getPaymentDate(), COUPON.getPaymentDate());
    assertEquals(CMS_COUPON_2.getAccrualStartDate(), COUPON.getAccrualStartDate());
    assertEquals(CMS_COUPON_2.getAccrualEndDate(), COUPON.getAccrualEndDate());
    assertEquals(CMS_COUPON_2.getPaymentYearFraction(), COUPON.getPaymentYearFraction(), 1E-10);
    assertEquals(CMS_COUPON_2.getNotional(), COUPON.getNotional(), 1E-2);
    assertEquals(CMS_COUPON_2.getFixingDate(), FIXING_DATE);
    assertEquals(CMS_COUPON_2.isFixed(), false);
    assertEquals(CMS_COUPON_2.getUnderlyingSwap(), SWAP);
  }

  @Test
  public void testFixingProcess() {
    CouponFloatingDefinition CouponWithReset = CouponCMSDefinition.from(FLOAT_COUPON, SWAP);
    double RESET_RATE = 0.04;
    assertEquals(CouponWithReset.isFixed(), false);
    CouponWithReset.fixingProcess(RESET_RATE);
    assertEquals(CouponWithReset.isFixed(), true);
    assertEquals(CouponWithReset.getFixedRate(), RESET_RATE, 1E-10);
  }

  @Test
  public void testToDerivative() {
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
    double paymentTime = actAct.getDayCountFraction(zonedDate, PAYMENT_DATE);
    double fixingTime = actAct.getDayCountFraction(zonedDate, FIXING_DATE);
    String fundingCurve = "Funding";
    String forwardCurve = "Forward";
    String[] curves = {fundingCurve, forwardCurve};
    FixedCouponSwap<? extends Payment> convertedSwap = SWAP.toDerivative(REFERENCE_DATE, curves);
    CouponCMS couponCMS = new CouponCMS(paymentTime, ACCRUAL_FACTOR, NOTIONAL, fixingTime, convertedSwap);
    CouponCMS convertedDefinition = (CouponCMS) CMS_COUPON.toDerivative(REFERENCE_DATE, curves);
    assertEquals(couponCMS, convertedDefinition);
  }

}
