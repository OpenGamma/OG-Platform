/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
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
 * Test related to the construction and the conversion of Ibor coupon with spread.
 */
@Test(groups = TestGroup.UNIT)
public class CouponIborSpreadDefinitionTest {

  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Deprecated");

  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final double NOTIONAL = 1000000; //1m
  private static final double SPREAD = -0.001; // -10 bps
  // Coupon with standard payment and accrual dates.
  private static final CouponIborDefinition IBOR_COUPON_DEFINITION = CouponIborDefinition.from(NOTIONAL, FIXING_DATE, INDEX, CALENDAR);
  private static final CouponIborSpreadDefinition IBOR_COUPON_SPREAD_DEFINITION = CouponIborSpreadDefinition.from(IBOR_COUPON_DEFINITION, SPREAD);
  private static final CouponIborSpreadDefinition IBOR_COUPON_SPREAD_CONSTR_DEFINITION = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_DEFINITION.getPaymentDate().plusDays(1),
      IBOR_COUPON_DEFINITION.getAccrualStartDate().minusDays(1), IBOR_COUPON_DEFINITION.getAccrualEndDate().minusDays(1), IBOR_COUPON_DEFINITION.getPaymentYearFraction(),
      NOTIONAL, FIXING_DATE, IBOR_COUPON_DEFINITION.getFixingPeriodStartDate().plusDays(2), IBOR_COUPON_DEFINITION.getAccrualEndDate().minusDays(2),
      IBOR_COUPON_DEFINITION.getFixingPeriodAccrualFactor(), INDEX, SPREAD, CALENDAR);
  private static final double FIXING_RATE = 0.04;
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {FIXING_DATE }, new double[] {FIXING_RATE });
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 5);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27); //For conversion to derivative

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupon() {
    CouponIborSpreadDefinition.from(null, SPREAD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionAfterFixingNoData() {
    IBOR_COUPON_SPREAD_DEFINITION.toDerivative(FIXING_DATE.plusDays(3));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConversionNullTS() {
    IBOR_COUPON_SPREAD_DEFINITION.toDerivative(FIXING_DATE, (DoubleTimeSeries<ZonedDateTime>) null);
  }

  @Test
  public void testGetter() {
    assertEquals(IBOR_COUPON_SPREAD_DEFINITION.getNotional(), NOTIONAL, 1E-2);
    assertEquals(IBOR_COUPON_SPREAD_DEFINITION.getFixingDate(), FIXING_DATE);
    assertEquals(IBOR_COUPON_SPREAD_DEFINITION.getSpread(), SPREAD, 1E-10);
    assertEquals(IBOR_COUPON_SPREAD_DEFINITION.getSpreadAmount(), SPREAD * NOTIONAL * IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), 1E-2);
    assertEquals("CouponIborSpreadDefinition: getter", IBOR_COUPON_SPREAD_CONSTR_DEFINITION.getPaymentDate(), IBOR_COUPON_DEFINITION.getPaymentDate().plusDays(1));
    assertEquals("CouponIborSpreadDefinition: getter", IBOR_COUPON_SPREAD_CONSTR_DEFINITION.getAccrualStartDate(), IBOR_COUPON_DEFINITION.getAccrualStartDate().minusDays(1));
    assertEquals("CouponIborSpreadDefinition: getter", IBOR_COUPON_SPREAD_CONSTR_DEFINITION.getAccrualEndDate(), IBOR_COUPON_DEFINITION.getAccrualEndDate().minusDays(1));
    assertEquals("CouponIborSpreadDefinition: getter", IBOR_COUPON_SPREAD_CONSTR_DEFINITION.getFixingPeriodStartDate(), IBOR_COUPON_DEFINITION.getFixingPeriodStartDate().plusDays(2));
    assertEquals("CouponIborSpreadDefinition: getter", IBOR_COUPON_SPREAD_CONSTR_DEFINITION.getFixingPeriodEndDate(), IBOR_COUPON_DEFINITION.getAccrualEndDate().minusDays(2));
    assertEquals("CouponIborSpreadDefinition: getter", IBOR_COUPON_SPREAD_CONSTR_DEFINITION.getFixingPeriodAccrualFactor(), IBOR_COUPON_DEFINITION.getFixingPeriodAccrualFactor());
  }

  @Test
  public void from() {
    final ZonedDateTime accrualStartDate = DateUtils.getUTCDate(2011, 1, 3);
    final ZonedDateTime accrualEndDate = DateUtils.getUTCDate(2011, 4, 1);
    final double accrualFactor = 0.25;
    final CouponIborSpreadDefinition cpn = CouponIborSpreadDefinition.from(accrualStartDate, accrualEndDate, accrualFactor, NOTIONAL, INDEX, SPREAD, CALENDAR);
    assertEquals("CouponIborSpreadDefinition: from", NOTIONAL, cpn.getNotional());
    assertEquals("CouponIborSpreadDefinition: from", SPREAD, cpn.getSpread());
    assertEquals("CouponIborSpreadDefinition: from", accrualStartDate, cpn.getAccrualStartDate());
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, -INDEX.getSpotLag(), CALENDAR);
    assertEquals("CouponIborSpreadDefinition: from", fixingDate, cpn.getFixingDate());
    assertEquals("CouponIborSpreadDefinition: from", accrualStartDate, cpn.getFixingPeriodStartDate());
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(accrualStartDate, INDEX, CALENDAR);
    assertEquals("CouponIborSpreadDefinition: from", fixingPeriodEndDate, cpn.getFixingPeriodEndDate());
  }

  @Test
  public void testObject() {
    CouponIborSpreadDefinition other = CouponIborSpreadDefinition.from(IBOR_COUPON_DEFINITION, SPREAD);
    assertEquals(IBOR_COUPON_SPREAD_DEFINITION, other);
    assertEquals(IBOR_COUPON_SPREAD_DEFINITION.hashCode(), other.hashCode());
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(),
        IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, INDEX, SPREAD, CALENDAR);
    assertEquals(IBOR_COUPON_SPREAD_DEFINITION, other);
    assertEquals(IBOR_COUPON_SPREAD_DEFINITION.hashCode(), other.hashCode());
    other = new CouponIborSpreadDefinition(Currency.AUD, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(),
        IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(), IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, new IborIndex(Currency.AUD, TENOR, SETTLEMENT_DAYS,
            DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor"), SPREAD, CALENDAR);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate().plusDays(1), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(),
        IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(), IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, INDEX, SPREAD, CALENDAR);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate().plusDays(1),
        IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(), IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, INDEX, SPREAD, CALENDAR);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate()
        .plusDays(1), IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, INDEX, SPREAD, CALENDAR);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(),
        IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction() + 0.01, NOTIONAL, FIXING_DATE, INDEX, SPREAD, CALENDAR);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(),
        IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL + 100, FIXING_DATE, INDEX, SPREAD, CALENDAR);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(),
        IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_DATE.plusDays(1), INDEX, SPREAD, CALENDAR);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(),
        IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, new IborIndex(Currency.EUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, !IS_EOM, "Ibor"), SPREAD,
        CALENDAR);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
    other = new CouponIborSpreadDefinition(CUR, IBOR_COUPON_SPREAD_DEFINITION.getPaymentDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualStartDate(), IBOR_COUPON_SPREAD_DEFINITION.getAccrualEndDate(),
        IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_DATE, INDEX, SPREAD + 0.01, CALENDAR);
    assertFalse(IBOR_COUPON_SPREAD_DEFINITION.equals(other));
  }

  @Test
  public void testToDerivativeBeforeFixing() {
    final double paymentTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, PAYMENT_DATE);
    final double fixingTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATE);
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, IBOR_COUPON_DEFINITION.getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, IBOR_COUPON_DEFINITION.getFixingPeriodEndDate());
    final CouponIborSpread couponIbor = new CouponIborSpread(CUR, paymentTime, IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, fixingTime, INDEX,
        fixingPeriodStartTime, fixingPeriodEndTime, IBOR_COUPON_SPREAD_DEFINITION.getFixingPeriodAccrualFactor(), SPREAD);
    CouponIborSpread convertedDefinition = (CouponIborSpread) IBOR_COUPON_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE);
    assertEquals(couponIbor, convertedDefinition);
    convertedDefinition = (CouponIborSpread) IBOR_COUPON_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, FIXING_TS);
    assertEquals(couponIbor, convertedDefinition);
  }

  @Test
  public void testToDerivativeAfterFixing() {
    final ZonedDateTime date = FIXING_DATE.plusDays(2);
    double paymentTime = TimeCalculator.getTimeBetween(date, PAYMENT_DATE);
    CouponFixed couponFixed = new CouponFixed(CUR, paymentTime, IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_RATE + SPREAD);
    CouponFixed convertedDefinition = (CouponFixed) IBOR_COUPON_SPREAD_DEFINITION.toDerivative(date, FIXING_TS);
    assertEquals(couponFixed, convertedDefinition);
    paymentTime = TimeCalculator.getTimeBetween(FIXING_DATE, PAYMENT_DATE);
    couponFixed = new CouponFixed(CUR, paymentTime, IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_RATE + SPREAD);
    convertedDefinition = (CouponFixed) IBOR_COUPON_SPREAD_DEFINITION.toDerivative(FIXING_DATE, FIXING_TS);
    assertEquals(couponFixed, convertedDefinition);
  }

  @Test
  /**
   * Tests the toDerivative method where the fixing date is equal to the current date.
   */
  public void testToDerivativeOnFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 1, 3, 12, 5);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE);
    final double fixingTime = TimeCalculator.getTimeBetween(referenceDate, FIXING_DATE);
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(referenceDate, IBOR_COUPON_DEFINITION.getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(referenceDate, IBOR_COUPON_DEFINITION.getFixingPeriodEndDate());
    // The fixing is known
    final CouponFixed coupon = new CouponFixed(CUR, paymentTime, IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, FIXING_RATE + SPREAD);
    final Payment couponConverted = IBOR_COUPON_SPREAD_DEFINITION.toDerivative(referenceDate, FIXING_TS);
    assertEquals(coupon, couponConverted);
    // The fixing is not known
    final DoubleTimeSeries<ZonedDateTime> fixingTS2 = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {ScheduleCalculator.getAdjustedDate(FIXING_DATE, -1, CALENDAR) },
        new double[] {FIXING_RATE });
    final CouponIborSpread coupon2 = new CouponIborSpread(CUR, paymentTime, IBOR_COUPON_SPREAD_DEFINITION.getPaymentYearFraction(), NOTIONAL, fixingTime, INDEX, fixingPeriodStartTime,
        fixingPeriodEndTime, IBOR_COUPON_SPREAD_DEFINITION.getFixingPeriodAccrualFactor(), SPREAD);
    final Payment couponConverted2 = IBOR_COUPON_SPREAD_DEFINITION.toDerivative(referenceDate, fixingTS2);
    assertEquals("CouponIborGearingDefinition: toDerivative", coupon2, couponConverted2);
    final Payment couponConverted3 = IBOR_COUPON_SPREAD_DEFINITION.toDerivative(referenceDate);
    assertEquals("CouponIborGearingDefinition: toDerivative", coupon2, couponConverted3);
  }

}
