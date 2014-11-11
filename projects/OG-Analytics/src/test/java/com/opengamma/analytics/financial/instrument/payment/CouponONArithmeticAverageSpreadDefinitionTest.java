package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CouponONArithmeticAverageSpreadDefinitionTest {

  private static final BusinessDayConvention MOD_FOL = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexON FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");

  private static final int US_SETTLEMENT_DAYS = 2;
  /*private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 5, 23);*/
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 9, 7);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(TRADE_DATE, US_SETTLEMENT_DAYS, NYC);
  private static final ZonedDateTime EFFECTIVE_DATE = SPOT_DATE;
  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final Period TENOR_7D = Period.ofDays(7);
  private static final double NOTIONAL = 100000000; // 100m
  private static final double SPREAD = 0.0010; // 10 bps
  private static final int PAYMENT_LAG = 2;
  private static final ZonedDateTime ACCRUAL_END_DATE_3M = ScheduleCalculator.getAdjustedDate(EFFECTIVE_DATE, TENOR_3M, USDLIBOR3M, NYC);
  private static final double ACCURAL_FACTOR_3M = USDLIBOR3M.getDayCount().getDayCountFraction(EFFECTIVE_DATE, ACCRUAL_END_DATE_3M);
  private static final ZonedDateTime PAYMENT_DATE_3M = ScheduleCalculator.getAdjustedDate(ACCRUAL_END_DATE_3M, -1 + FEDFUND.getPublicationLag() + PAYMENT_LAG, NYC);

  private static final ZonedDateTime ACCRUAL_END_DATE_7D = ScheduleCalculator.getAdjustedDate(EFFECTIVE_DATE, TENOR_7D, USDLIBOR3M, NYC);
  private static final double ACCURAL_FACTOR_7D = USDLIBOR3M.getDayCount().getDayCountFraction(EFFECTIVE_DATE, ACCRUAL_END_DATE_7D);
  private static final ZonedDateTime PAYMENT_DATE_7D = ScheduleCalculator.getAdjustedDate(ACCRUAL_END_DATE_7D, -1 + FEDFUND.getPublicationLag() + PAYMENT_LAG, NYC);

  private static final CouponONArithmeticAverageSpreadDefinition FEDFUND_CPN_3M_DEF = new CouponONArithmeticAverageSpreadDefinition(Currency.USD, PAYMENT_DATE_3M, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M,
      ACCURAL_FACTOR_3M, NOTIONAL, FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, SPREAD, NYC);

  private static final CouponONArithmeticAverageSpreadDefinition FEDFUND_CPN_3M_FROM_DEF = CouponONArithmeticAverageSpreadDefinition.from(FEDFUND, EFFECTIVE_DATE, TENOR_3M, NOTIONAL, PAYMENT_LAG,
      MOD_FOL, true, SPREAD, NYC);

  private static final CouponONArithmeticAverageSpreadDefinition FEDFUND_CPN_7D_DEF = new CouponONArithmeticAverageSpreadDefinition(Currency.USD, PAYMENT_DATE_7D, EFFECTIVE_DATE, ACCRUAL_END_DATE_7D,
      ACCURAL_FACTOR_7D, NOTIONAL, FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE_7D, SPREAD, NYC);

  private static final CouponONArithmeticAverageSpreadDefinition FEDFUND_CPN_7D_FROM_DEF = CouponONArithmeticAverageSpreadDefinition.from(FEDFUND, EFFECTIVE_DATE, TENOR_7D, NOTIONAL, PAYMENT_LAG,
      MOD_FOL, true, SPREAD, NYC);

  private static final double EPS = 3e-16;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new CouponONArithmeticAverageSpreadDefinition(null, EFFECTIVE_DATE, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, ACCURAL_FACTOR_3M,
        NOTIONAL, FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, SPREAD, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEffective() {
    new CouponONArithmeticAverageSpreadDefinition(Currency.USD, null, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, ACCURAL_FACTOR_3M,
        NOTIONAL, FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, SPREAD, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStartAccural() {
    new CouponONArithmeticAverageSpreadDefinition(Currency.USD, EFFECTIVE_DATE, null, ACCRUAL_END_DATE_3M, ACCURAL_FACTOR_3M,
        NOTIONAL, FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, SPREAD, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEndAccural() {
    new CouponONArithmeticAverageSpreadDefinition(Currency.USD, EFFECTIVE_DATE, EFFECTIVE_DATE, null, ACCURAL_FACTOR_3M,
        NOTIONAL, FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, SPREAD, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new CouponONArithmeticAverageSpreadDefinition(Currency.USD, EFFECTIVE_DATE, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, ACCURAL_FACTOR_3M,
        NOTIONAL, null, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, SPREAD, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullStartFixing() {
    new CouponONArithmeticAverageSpreadDefinition(Currency.USD, EFFECTIVE_DATE, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, ACCURAL_FACTOR_3M,
        NOTIONAL, FEDFUND, null, ACCRUAL_END_DATE_3M, SPREAD, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullEndFixing() {
    new CouponONArithmeticAverageSpreadDefinition(Currency.USD, EFFECTIVE_DATE, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, ACCURAL_FACTOR_3M,
        NOTIONAL, FEDFUND, EFFECTIVE_DATE, null, SPREAD, NYC);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongCurrency() {
    new CouponONArithmeticAverageSpreadDefinition(Currency.AUD, EFFECTIVE_DATE, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, ACCURAL_FACTOR_3M,
        NOTIONAL, FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, SPREAD, NYC);
  }

  @Test
  public void getter() {
    assertEquals("CouponArithmeticAverageONSpreadDefinition: getter", FEDFUND_CPN_3M_DEF.getPaymentDate(), PAYMENT_DATE_3M);
    assertEquals("CouponArithmeticAverageONSpreadDefinition: getter", FEDFUND_CPN_3M_DEF.getAccrualStartDate(), EFFECTIVE_DATE);
    assertEquals("CouponArithmeticAverageONSpreadDefinition: getter", FEDFUND_CPN_3M_DEF.getAccrualEndDate(), ACCRUAL_END_DATE_3M);
    assertEquals("CouponArithmeticAverageONSpreadDefinition: getter", FEDFUND_CPN_3M_DEF.getFixingPeriodDates()[0], EFFECTIVE_DATE);
    assertEquals("CouponArithmeticAverageONSpreadDefinition: getter", FEDFUND_CPN_3M_DEF.getFixingPeriodDates()[FEDFUND_CPN_3M_DEF.getFixingPeriodDates().length - 1], ACCRUAL_END_DATE_3M);
    assertEquals("CouponArithmeticAverageONSpreadDefinition: getter", FEDFUND_CPN_3M_DEF.getCurrency(), FEDFUND.getCurrency());
    assertEquals("CouponArithmeticAverageONSpreadDefinition: getter", FEDFUND_CPN_3M_DEF.getIndex(), FEDFUND);
    assertEquals("CouponArithmeticAverageONSpreadDefinition: getter", FEDFUND_CPN_3M_DEF.getSpread(), SPREAD);
    assertEquals("CouponArithmeticAverageONSpreadDefinition: getter", FEDFUND_CPN_3M_DEF.getSpreadAmount(), SPREAD * ACCURAL_FACTOR_3M * NOTIONAL);
  }

  @Test
  public void from() {
    assertEquals("CouponArithmeticAverageONSpreadDefinition: from", FEDFUND_CPN_3M_DEF, FEDFUND_CPN_3M_FROM_DEF);
  }

  @Test
  public void equalHash() {
    assertEquals("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF, FEDFUND_CPN_3M_DEF);
    final CouponONArithmeticAverageSpreadDefinition other = CouponONArithmeticAverageSpreadDefinition.from(FEDFUND, EFFECTIVE_DATE, TENOR_3M, NOTIONAL, PAYMENT_LAG,
        MOD_FOL, true, SPREAD, NYC);
    assertEquals("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF, other);
    assertEquals("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.hashCode(), other.hashCode());
    CouponONArithmeticAverageSpreadDefinition modified;
    final IndexON modifiedIndex = IndexONMaster.getInstance().getIndex("EONIA");
    modified = CouponONArithmeticAverageSpreadDefinition.from(modifiedIndex, EFFECTIVE_DATE, TENOR_3M, NOTIONAL, PAYMENT_LAG, MOD_FOL, true, SPREAD, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponONArithmeticAverageSpreadDefinition.from(FEDFUND, EFFECTIVE_DATE.plusDays(1), ACCRUAL_END_DATE_3M, NOTIONAL, PAYMENT_LAG, SPREAD, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponONArithmeticAverageSpreadDefinition.from(FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M.plusDays(1), NOTIONAL, PAYMENT_LAG, SPREAD, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponONArithmeticAverageSpreadDefinition.from(FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, NOTIONAL + 1000, PAYMENT_LAG, SPREAD, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponONArithmeticAverageSpreadDefinition.from(FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, NOTIONAL, PAYMENT_LAG + 1, SPREAD, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
    modified = CouponONArithmeticAverageSpreadDefinition.from(FEDFUND, EFFECTIVE_DATE, ACCRUAL_END_DATE_3M, NOTIONAL, PAYMENT_LAG, SPREAD + 0.0010, NYC);
    assertFalse("CouponArithmeticAverageON: equal-hash", FEDFUND_CPN_3M_DEF.equals(modified));
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeNoFixing() {
    final CouponONArithmeticAverageSpread cpnConverted = FEDFUND_CPN_3M_DEF.toDerivative(TRADE_DATE);
    final double paymentTime = TimeCalculator.getTimeBetween(TRADE_DATE, PAYMENT_DATE_3M);
    final double[] fixingPeriodTimes = TimeCalculator.getTimeBetween(TRADE_DATE, FEDFUND_CPN_3M_DEF.getFixingPeriodDates());
    final CouponONArithmeticAverageSpread cpnExpected = CouponONArithmeticAverageSpread.from(paymentTime, ACCURAL_FACTOR_3M, NOTIONAL, FEDFUND, fixingPeriodTimes,
        FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors(), 0.0, SPREAD);
    assertEquals("CouponArithmeticAverageONSpread definition definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingBeforeStart() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(TRADE_DATE, 1, NYC);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7) }, new double[] {0.01 });
    final Payment cpnConverted = FEDFUND_CPN_3M_DEF.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE_3M);
    final double[] fixingPeriodTimes = TimeCalculator.getTimeBetween(referenceDate, FEDFUND_CPN_3M_DEF.getFixingPeriodDates());
    final CouponONArithmeticAverageSpread cpnExpected = CouponONArithmeticAverageSpread.from(paymentTime, ACCURAL_FACTOR_3M, NOTIONAL, FEDFUND, fixingPeriodTimes,
        FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors(), 0.0, SPREAD);

    assertEquals("CouponArithmeticAverageONSpread definition definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingOnStartNotYetFixed() {
    final ZonedDateTime referenceDate = SPOT_DATE;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8) },
        new double[] {0.01, 0.01 });
    final Payment cpnConverted = FEDFUND_CPN_3M_DEF.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE_3M);
    final double[] fixingPeriodTimes = TimeCalculator.getTimeBetween(referenceDate, FEDFUND_CPN_3M_DEF.getFixingPeriodDates());
    final CouponONArithmeticAverageSpread cpnExpected = CouponONArithmeticAverageSpread.from(paymentTime, ACCURAL_FACTOR_3M, NOTIONAL, FEDFUND, fixingPeriodTimes,
        FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors(), 0.0, SPREAD);
    assertEquals("CouponArithmeticAverageONSpread definition definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingOnStartFixed() {
    final ZonedDateTime referenceDate = FEDFUND_CPN_3M_DEF.getFixingPeriodDates()[1];
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 8), DateUtils.getUTCDate(2011, 9, 9),
      DateUtils.getUTCDate(2011, 9, 12) }, new double[] {fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = FEDFUND_CPN_3M_DEF.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE_3M);
    final double rateAccrued = fixingRate * FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors()[0];
    final double[] fixingPeriodTimes = new double[FEDFUND_CPN_3M_DEF.getFixingPeriodDates().length - 1];

    for (int loopperiod = 1; loopperiod < FEDFUND_CPN_3M_DEF.getFixingPeriodDates().length; loopperiod++) {
      fixingPeriodTimes[loopperiod - 1] = TimeCalculator.getTimeBetween(referenceDate, FEDFUND_CPN_3M_DEF.getFixingPeriodDates()[loopperiod]);
    }

    final double[] fixingAccrualFactorsLeft = new double[FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors().length - 1];
    for (int loopperiod = 1; loopperiod < FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors().length; loopperiod++) {
      fixingAccrualFactorsLeft[loopperiod - 1] = FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors()[loopperiod];
    }
    final CouponONArithmeticAverageSpread cpnExpected = CouponONArithmeticAverageSpread.from(paymentTime, ACCURAL_FACTOR_3M, NOTIONAL, FEDFUND, fixingPeriodTimes,
        fixingAccrualFactorsLeft, rateAccrued, SPREAD);
    assertEquals("CouponArithmeticAverageONSpread definition definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingMiddleNotYetFixed() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 13);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12) }, new double[] {fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = FEDFUND_CPN_3M_DEF.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE_3M);
    final double rateAccrued = fixingRate * FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors()[0] + fixingRate * FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors()[1];
    final double[] fixingPeriodTimes = new double[FEDFUND_CPN_3M_DEF.getFixingPeriodDates().length - 2];

    for (int loopperiod = 2; loopperiod < FEDFUND_CPN_3M_DEF.getFixingPeriodDates().length; loopperiod++) {
      fixingPeriodTimes[loopperiod - 2] = TimeCalculator.getTimeBetween(referenceDate, FEDFUND_CPN_3M_DEF.getFixingPeriodDates()[loopperiod]);
    }

    final double[] fixingAccrualFactorsLeft = new double[FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors().length - 2];
    for (int loopperiod = 2; loopperiod < FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors().length; loopperiod++) {
      fixingAccrualFactorsLeft[loopperiod - 2] = FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors()[loopperiod];
    }
    final CouponONArithmeticAverageSpread cpnExpected = CouponONArithmeticAverageSpread.from(paymentTime, ACCURAL_FACTOR_3M, NOTIONAL, FEDFUND, fixingPeriodTimes,
        fixingAccrualFactorsLeft, rateAccrued, SPREAD);
    assertEquals("CouponArithmeticAverageONSpread definition definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingMiddleFixed() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 14);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13) }, new double[] {fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = FEDFUND_CPN_3M_DEF.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE_3M);
    final double rateAccrued = fixingRate * FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors()[0] + fixingRate * FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors()[1] + fixingRate *
        FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors()[2];
    final double[] fixingPeriodTimes = new double[FEDFUND_CPN_3M_DEF.getFixingPeriodDates().length - 3];

    for (int loopperiod = 3; loopperiod < FEDFUND_CPN_3M_DEF.getFixingPeriodDates().length; loopperiod++) {
      fixingPeriodTimes[loopperiod - 3] = TimeCalculator.getTimeBetween(referenceDate, FEDFUND_CPN_3M_DEF.getFixingPeriodDates()[loopperiod]);
    }

    final double[] fixingAccrualFactorsLeft = new double[FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors().length - 3];
    for (int loopperiod = 3; loopperiod < FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors().length; loopperiod++) {
      fixingAccrualFactorsLeft[loopperiod - 3] = FEDFUND_CPN_3M_DEF.getFixingPeriodAccrualFactors()[loopperiod];
    }
    final CouponONArithmeticAverageSpread cpnExpected = CouponONArithmeticAverageSpread.from(paymentTime, ACCURAL_FACTOR_3M, NOTIONAL, FEDFUND, fixingPeriodTimes,
        fixingAccrualFactorsLeft, rateAccrued, SPREAD);
    assertEquals("CouponArithmeticAverageONSpread definition definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingSecondLastFixed() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 18);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
        new ZonedDateTime[] {
          DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15),
          DateUtils.getUTCDate(2011, 9, 16), DateUtils.getUTCDate(2011, 9, 19) }, new double[] {
          fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = FEDFUND_CPN_7D_DEF.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE_7D);
    final double rateAccrued = fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[0] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[1] + fixingRate *
        FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[2] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[3]
        + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[4];
    final CouponFixed cpnExpected = new CouponFixed(Currency.USD, paymentTime, ACCURAL_FACTOR_7D, NOTIONAL, rateAccrued / FEDFUND_CPN_7D_DEF.getPaymentYearFraction());
    assertEquals("CouponArithmeticAverageONSpread definition definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFixingLast() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 19);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
        new ZonedDateTime[] {
          DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15),
          DateUtils.getUTCDate(2011, 9, 16), DateUtils.getUTCDate(2011, 9, 19) }, new double[] {
          fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = FEDFUND_CPN_7D_DEF.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE_7D);
    final double rateAccrued = fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[0] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[1] + fixingRate *
        FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[2] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[3]
        + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[4];
    final CouponFixed cpnExpected = new CouponFixed(Currency.USD, paymentTime, ACCURAL_FACTOR_7D, NOTIONAL, rateAccrued / FEDFUND_CPN_7D_DEF.getPaymentYearFraction());
    assertEquals("CouponArithmeticAverageONSpread definition definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeAfterLast() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 20);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
        new ZonedDateTime[] {
          DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15),
          DateUtils.getUTCDate(2011, 9, 16), DateUtils.getUTCDate(2011, 9, 19) }, new double[] {
          fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = FEDFUND_CPN_7D_DEF.toDerivative(referenceDate, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(referenceDate, PAYMENT_DATE_7D);
    final double rateAccrued = fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[0] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[1] + fixingRate *
        FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[2] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[3]
        + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[4];
    final CouponFixed cpnExpected = new CouponFixed(Currency.USD, paymentTime, ACCURAL_FACTOR_7D, NOTIONAL, rateAccrued / FEDFUND_CPN_7D_DEF.getPaymentYearFraction());
    assertEquals("CouponArithmeticAverageONSpread definition definition: toDerivative", cpnExpected, cpnConverted);
  }

  @Test
  /**
   * Tests the toDerivative method on the payment date. valuation is at noon, payment set at midnight...
   */
  public void toDerivativeJustAfterPayment() {
    final ZonedDateTime valuationTimeIsNoon = DateUtils.getUTCDate(2011, 9, 20, 12, 0);
    assertTrue("valuationTimeIsNoon used to be after paymentDate, which was midnight. Confirm behaviour", valuationTimeIsNoon.isAfter(FEDFUND_CPN_7D_DEF.getPaymentDate()));
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15) }, new double[] {
      fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    final Payment cpnConverted = FEDFUND_CPN_7D_DEF.toDerivative(valuationTimeIsNoon, fixingTS);
    final double paymentTime = TimeCalculator.getTimeBetween(valuationTimeIsNoon, PAYMENT_DATE_7D);
    final double rateAccrued = fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[0] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[1] + fixingRate *
        FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[2] + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[3]
        + fixingRate * FEDFUND_CPN_7D_DEF.getFixingPeriodAccrualFactors()[4];

    final CouponFixed cpnExpected = new CouponFixed(Currency.USD, paymentTime, ACCURAL_FACTOR_7D, NOTIONAL, rateAccrued / ACCURAL_FACTOR_7D);
    assertEquals("CouponArithmeticAverageONSpread definition definition: toDerivative", cpnExpected, cpnConverted);

    // Test pricing, too. Notice that the value of a coupon on its payment date is non-zero
    final MulticurveProviderDiscount curves = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
    final MultipleCurrencyAmount pvConverted = com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedDiscountingMethod.getInstance().presentValue((CouponFixed) cpnConverted,
        curves);
    final MultipleCurrencyAmount pvExpected = com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedDiscountingMethod.getInstance().presentValue(cpnExpected, curves);
    assertEquals("CouponArithmeticAverageONSpread  definition: toDerivative", pvConverted, pvExpected);
    assertEquals("CouponArithmeticAverageONSpread  definition: toDerivative", pvConverted, MultipleCurrencyAmount.of(Currency.USD, 19444.44444444444));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  /**
   * Tests the toDerivative method: after payment date
   */
  public void toDerivativeAfterPayment() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 9, 21);
    final double fixingRate = 0.01;
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 7), DateUtils.getUTCDate(2011, 9, 8),
      DateUtils.getUTCDate(2011, 9, 9), DateUtils.getUTCDate(2011, 9, 12), DateUtils.getUTCDate(2011, 9, 13), DateUtils.getUTCDate(2011, 9, 14), DateUtils.getUTCDate(2011, 9, 15) }, new double[] {
      fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate, fixingRate });
    FEDFUND_CPN_7D_DEF.toDerivative(referenceDate, fixingTS);
  }

}
