/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of Federal Fund future.
 */
@Test(groups = TestGroup.UNIT)
public class FederalFundsFutureSecurityDefinitionTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexON INDEX_FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final BusinessDayConvention BUSINESS_DAY_PRECEDING = BusinessDayConventions.PRECEDING;
  private static final BusinessDayConvention BUSINESS_DAY_FOLLOWING = BusinessDayConventions.FOLLOWING;
  private static final ZonedDateTime MARCH_1 = DateUtils.getUTCDate(2012, 3, 1);
  private static final ZonedDateTime APRIL_1 = DateUtils.getUTCDate(2012, 4, 1);
  private static final ZonedDateTime LAST_TRADING_DATE = BUSINESS_DAY_PRECEDING.adjustDate(NYC, APRIL_1);
  private static final ZonedDateTime PERIOD_FIRST_DATE = BUSINESS_DAY_FOLLOWING.adjustDate(NYC, MARCH_1);
  private static final ZonedDateTime PERIOD_LAST_DATE = BUSINESS_DAY_FOLLOWING.adjustDate(NYC, APRIL_1.minusDays(1));
  private static final List<ZonedDateTime> FIXING_LIST = new ArrayList<>();
  private static final ZonedDateTime[] FIXING_DATE;
  static {
    ZonedDateTime date = PERIOD_FIRST_DATE;
    while (!date.isAfter(PERIOD_LAST_DATE)) {
      FIXING_LIST.add(date);
      date = BUSINESS_DAY_FOLLOWING.adjustDate(NYC, date.plusDays(1));
    }
    FIXING_DATE = FIXING_LIST.toArray(new ZonedDateTime[FIXING_LIST.size()]);
  }
  private static final double[] FIXING_ACCURAL_FACTOR = new double[FIXING_DATE.length - 1];
  static {
    for (int loopfix = 0; loopfix < FIXING_DATE.length - 1; loopfix++) {
      FIXING_ACCURAL_FACTOR[loopfix] = INDEX_FEDFUND.getDayCount().getDayCountFraction(FIXING_DATE[loopfix], FIXING_DATE[loopfix + 1]);
    }
  }
  private static final double NOTIONAL = 5000000;
  private static final double PAYMENT_ACCURAL_FACTOR = 1.0 / 12.0;
  private static final String NAME = "FFH2";

  private static final FederalFundsFutureSecurityDefinition FUTURE_FEDFUND_DEFINITION = new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, FIXING_DATE, FIXING_ACCURAL_FACTOR,
      NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 30);
  private static final String CURVE_NAME = "OIS";

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullLastTrading() {
    new FederalFundsFutureSecurityDefinition(null, INDEX_FEDFUND, FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, null, FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFixingDate() {
    new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, null, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFixingAccrual() {
    new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, FIXING_DATE, null, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullName() {
    new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void fixingLength() {
    new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, new ZonedDateTime[] {LAST_TRADING_DATE }, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
  }

  @Test
  /**
   * Tests the getter methods.
   */
  public void getter() {
    assertEquals("Fed fund future security definition", LAST_TRADING_DATE, FUTURE_FEDFUND_DEFINITION.getLastTradingDate());
    assertEquals("Fed fund future security definition", INDEX_FEDFUND, FUTURE_FEDFUND_DEFINITION.getIndex());
    assertEquals("Fed fund future security definition", FIXING_DATE, FUTURE_FEDFUND_DEFINITION.getFixingPeriodDate());
    assertEquals("Fed fund future security definition", FIXING_ACCURAL_FACTOR, FUTURE_FEDFUND_DEFINITION.getFixingPeriodAccrualFactor());
    assertEquals("Fed fund future security definition", NOTIONAL, FUTURE_FEDFUND_DEFINITION.getNotional());
    assertEquals("Fed fund future security definition", PAYMENT_ACCURAL_FACTOR, FUTURE_FEDFUND_DEFINITION.getMarginAccrualFactor());
    assertEquals("Fed fund future security definition", NAME, FUTURE_FEDFUND_DEFINITION.getName());
    assertEquals("Fed fund future security definition", INDEX_FEDFUND.getCurrency(), FUTURE_FEDFUND_DEFINITION.getCurrency());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertTrue(FUTURE_FEDFUND_DEFINITION.equals(FUTURE_FEDFUND_DEFINITION));
    final FederalFundsFutureSecurityDefinition other = new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR,
        NAME);
    assertTrue(FUTURE_FEDFUND_DEFINITION.equals(other));
    assertTrue(FUTURE_FEDFUND_DEFINITION.hashCode() == other.hashCode());
    FederalFundsFutureSecurityDefinition modifiedFuture;
    modifiedFuture = new FederalFundsFutureSecurityDefinition(PERIOD_LAST_DATE, INDEX_FEDFUND, FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
    assertFalse(FUTURE_FEDFUND_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, IndexONMaster.getInstance().getIndex("EONIA"), FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL,
        PAYMENT_ACCURAL_FACTOR, NAME);
    assertFalse(FUTURE_FEDFUND_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL + 1.0, PAYMENT_ACCURAL_FACTOR, NAME);
    assertFalse(FUTURE_FEDFUND_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL, 0.25, NAME);
    assertFalse(FUTURE_FEDFUND_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureSecurityDefinition(LAST_TRADING_DATE, INDEX_FEDFUND, FIXING_DATE, FIXING_ACCURAL_FACTOR, NOTIONAL, PAYMENT_ACCURAL_FACTOR, "Wrong");
    assertFalse(FUTURE_FEDFUND_DEFINITION.equals(modifiedFuture));
    assertFalse(FUTURE_FEDFUND_DEFINITION.equals(LAST_TRADING_DATE));
    assertFalse(FUTURE_FEDFUND_DEFINITION.equals(null));
  }

  @Test
  /**
   * Tests the from method
   */
  public void from() {
    final FederalFundsFutureSecurityDefinition from = FederalFundsFutureSecurityDefinition.from(MARCH_1, INDEX_FEDFUND, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME, NYC);
    assertEquals("Fed fund future security definition: builder", FUTURE_FEDFUND_DEFINITION, from);
  }

  @Test
  /**
   * Tests the from method
   */
  public void from2() {
    final FederalFundsFutureSecurityDefinition from = FederalFundsFutureSecurityDefinition.from(MARCH_1, INDEX_FEDFUND, NOTIONAL, PAYMENT_ACCURAL_FACTOR, "FFMar12", NYC);
    final FederalFundsFutureSecurityDefinition fromFF = FederalFundsFutureSecurityDefinition.fromFedFund(MARCH_1, INDEX_FEDFUND, NYC);
    assertEquals("Fed fund future security definition: builder", from, fromFF);
  }

  @Test
  /**
   * Tests the toDerivative method before the first fixing date.
   */
  @SuppressWarnings("deprecation")
  public void toDerivativeNoFixingDeprecated() {
    final double lastTtradingTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, LAST_TRADING_DATE);
    final double[] fixingPeriodTime = new double[FIXING_DATE.length];
    for (int loopfix = 0; loopfix < FIXING_DATE.length; loopfix++) {
      fixingPeriodTime[loopfix] = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATE[loopfix]);
    }
    final FederalFundsFutureSecurity futureFedFundExpected = new FederalFundsFutureSecurity(INDEX_FEDFUND, 0.0, fixingPeriodTime, lastTtradingTime, FIXING_ACCURAL_FACTOR,
        FUTURE_FEDFUND_DEFINITION.getFixingTotalAccrualFactor(), NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME, CURVE_NAME);
    final FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAME);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  @SuppressWarnings("deprecation")
  public void toDerivativeAfterStartFixingDeprecated() {
    FUTURE_FEDFUND_DEFINITION.toDerivative(ScheduleCalculator.getAdjustedDate(FIXING_DATE[0], 2, NYC), CURVE_NAME);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  @SuppressWarnings("deprecation")
  public void toDerivativeFirstDaymonthDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 3, 1);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(DateUtils.getUTCDate(2012, 3, 1), 0.0010);
    final FederalFundsFutureSecurity futureFedFundExpected = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    final FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate, fixingTS, CURVE_NAME);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  @SuppressWarnings("deprecation")
  public void toDerivativeSecondDayMonthNoFixingDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 3, 2);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(DateUtils.getUTCDate(2012, 3, 1), 0.0010);
    final FederalFundsFutureSecurity futureFedFundExpected = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate, CURVE_NAME);
    final FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate, fixingTS, CURVE_NAME);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  @SuppressWarnings("deprecation")
  public void toDerivativeSecondDayMonthFixingDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 3, 2);
    final double lastTtradingTime = TimeCalculator.getTimeBetween(referenceDate, LAST_TRADING_DATE);
    final double[] rateFixing = new double[] {0.0010, 0.0011 };
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(
        new ZonedDateTime[] {DateUtils.getUTCDate(2012, 3, 1), DateUtils.getUTCDate(2012, 3, 2) }, rateFixing, ZoneOffset.UTC);
    final double accruedInterest = FIXING_ACCURAL_FACTOR[0] * rateFixing[1];
    final double[] fixingPeriodAccrualFactor = new double[FIXING_ACCURAL_FACTOR.length - 1];
    System.arraycopy(FIXING_ACCURAL_FACTOR, 1, fixingPeriodAccrualFactor, 0, fixingPeriodAccrualFactor.length);
    final double[] fixingPeriodTime = new double[FIXING_DATE.length - 1];
    for (int loopfix = 0; loopfix < FIXING_DATE.length - 1; loopfix++) {
      fixingPeriodTime[loopfix] = TimeCalculator.getTimeBetween(referenceDate, FIXING_DATE[loopfix + 1]);
    }
    final FederalFundsFutureSecurity futureFedFundExpected = new FederalFundsFutureSecurity(INDEX_FEDFUND, accruedInterest, fixingPeriodTime, lastTtradingTime, fixingPeriodAccrualFactor,
        FUTURE_FEDFUND_DEFINITION.getFixingTotalAccrualFactor(), NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME, CURVE_NAME);
    final FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate, fixingTS, CURVE_NAME);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  @SuppressWarnings("deprecation")
  public void toDerivativeMiddleMonthNoFixingDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 3, 7);
    final double lastTtradingTime = TimeCalculator.getTimeBetween(referenceDate, LAST_TRADING_DATE);
    final ZonedDateTime[] dateFixing = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 3, 1), DateUtils.getUTCDate(2012, 3, 2), DateUtils.getUTCDate(2012, 3, 5), DateUtils.getUTCDate(2012, 3, 6) };
    final double[] rateFixing = new double[] {0.0010, 0.0011, 0.0012, 0.0013 };
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(dateFixing, rateFixing, ZoneOffset.UTC);
    final double accruedInterest = FIXING_ACCURAL_FACTOR[0] * rateFixing[1] + FIXING_ACCURAL_FACTOR[1] * rateFixing[2] + FIXING_ACCURAL_FACTOR[2] * rateFixing[3];
    final double[] fixingPeriodAccrualFactor = new double[FIXING_ACCURAL_FACTOR.length - 3];
    System.arraycopy(FIXING_ACCURAL_FACTOR, 3, fixingPeriodAccrualFactor, 0, fixingPeriodAccrualFactor.length);
    final double[] fixingPeriodTime = new double[FIXING_DATE.length - 3];
    for (int loopfix = 0; loopfix < FIXING_DATE.length - 3; loopfix++) {
      fixingPeriodTime[loopfix] = TimeCalculator.getTimeBetween(referenceDate, FIXING_DATE[loopfix + 3]);
    }
    final FederalFundsFutureSecurity futureFedFundExpected = new FederalFundsFutureSecurity(INDEX_FEDFUND, accruedInterest, fixingPeriodTime, lastTtradingTime, fixingPeriodAccrualFactor,
        FUTURE_FEDFUND_DEFINITION.getFixingTotalAccrualFactor(), NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME, CURVE_NAME);
    final FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate, fixingTS, CURVE_NAME);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  @SuppressWarnings("deprecation")
  public void toDerivativeMiddleMonthFixingDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 3, 7);
    final double lastTtradingTime = TimeCalculator.getTimeBetween(referenceDate, LAST_TRADING_DATE);
    final ZonedDateTime[] dateFixing = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 3, 1), DateUtils.getUTCDate(2012, 3, 2), DateUtils.getUTCDate(2012, 3, 5), DateUtils.getUTCDate(2012, 3, 6),
      DateUtils.getUTCDate(2012, 3, 7) };
    final double[] rateFixing = new double[] {0.0010, 0.0011, 0.0012, 0.0013, 0.0014 };
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(dateFixing, rateFixing, ZoneOffset.UTC);
    final double accruedInterest = FIXING_ACCURAL_FACTOR[0] * rateFixing[1] + FIXING_ACCURAL_FACTOR[1] * rateFixing[2] + FIXING_ACCURAL_FACTOR[2] * rateFixing[3] + FIXING_ACCURAL_FACTOR[3] *
        rateFixing[4];
    final double[] fixingPeriodAccrualFactor = new double[FIXING_ACCURAL_FACTOR.length - 4];
    System.arraycopy(FIXING_ACCURAL_FACTOR, 4, fixingPeriodAccrualFactor, 0, fixingPeriodAccrualFactor.length);
    final double[] fixingPeriodTime = new double[FIXING_DATE.length - 4];
    for (int loopfix = 0; loopfix < FIXING_DATE.length - 4; loopfix++) {
      fixingPeriodTime[loopfix] = TimeCalculator.getTimeBetween(referenceDate, FIXING_DATE[loopfix + 4]);
    }
    final FederalFundsFutureSecurity futureFedFundExpected = new FederalFundsFutureSecurity(INDEX_FEDFUND, accruedInterest, fixingPeriodTime, lastTtradingTime, fixingPeriodAccrualFactor,
        FUTURE_FEDFUND_DEFINITION.getFixingTotalAccrualFactor(), NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME, CURVE_NAME);
    final FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate, fixingTS, CURVE_NAME);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }

  /**
   * Tests the toDerivative method.
   */
  @Test
  @SuppressWarnings("deprecation")
  public void toDerivativeEndPeriodNoFixingDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 4, 2);
    final double lastTtradingTime = TimeCalculator.getTimeBetween(referenceDate, LAST_TRADING_DATE);
    final ZonedDateTime[] dateFixing = new ZonedDateTime[FIXING_DATE.length - 1];
    System.arraycopy(FIXING_DATE, 0, dateFixing, 0, dateFixing.length);
    final double[] rateFixing = new double[dateFixing.length];
    for (int loopfix = 0; loopfix < dateFixing.length; loopfix++) {
      rateFixing[loopfix] = 0.0010 + loopfix * 0.0001;
    }
    double accruedInterest = 0.0;
    for (int loopfix = 0; loopfix < dateFixing.length - 1; loopfix++) {
      accruedInterest += FIXING_ACCURAL_FACTOR[loopfix] * rateFixing[loopfix + 1];
    }
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(dateFixing, rateFixing, ZoneOffset.UTC);
    final double[] fixingPeriodAccrualFactor = new double[] {FIXING_ACCURAL_FACTOR[FIXING_ACCURAL_FACTOR.length - 1] };
    final double[] fixingPeriodTime = new double[] {TimeCalculator.getTimeBetween(referenceDate, FIXING_DATE[FIXING_DATE.length - 2]),
      TimeCalculator.getTimeBetween(referenceDate, FIXING_DATE[FIXING_DATE.length - 1]) };
    final FederalFundsFutureSecurity futureFedFundExpected = new FederalFundsFutureSecurity(INDEX_FEDFUND, accruedInterest, fixingPeriodTime, lastTtradingTime, fixingPeriodAccrualFactor,
        FUTURE_FEDFUND_DEFINITION.getFixingTotalAccrualFactor(), NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME, CURVE_NAME);
    final FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate, fixingTS, CURVE_NAME);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeEndPeriodFixingDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 4, 2);
    final double lastTtradingTime = TimeCalculator.getTimeBetween(referenceDate, LAST_TRADING_DATE);
    final ZonedDateTime[] dateFixing = new ZonedDateTime[FIXING_DATE.length - 1];
    System.arraycopy(FIXING_DATE, 0, dateFixing, 0, dateFixing.length);
    final double[] rateFixing = new double[dateFixing.length];
    for (int loopfix = 0; loopfix < dateFixing.length; loopfix++) {
      rateFixing[loopfix] = 0.0010 + loopfix * 0.0001;
    }
    double accruedInterest = 0.0;
    for (int loopfix = 0; loopfix < dateFixing.length; loopfix++) {
      accruedInterest += FIXING_ACCURAL_FACTOR[loopfix] * rateFixing[loopfix];
    }
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(dateFixing, rateFixing, ZoneOffset.UTC);
    final double[] fixingPeriodAccrualFactor = new double[0];
    final double[] fixingPeriodTime = new double[] {TimeCalculator.getTimeBetween(referenceDate, FIXING_DATE[FIXING_DATE.length - 1]) };
    final FederalFundsFutureSecurity futureFedFundExpected = new FederalFundsFutureSecurity(INDEX_FEDFUND, accruedInterest, fixingPeriodTime, lastTtradingTime, fixingPeriodAccrualFactor,
        FUTURE_FEDFUND_DEFINITION.getFixingTotalAccrualFactor(), NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
    final FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate, fixingTS);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }

  @Test
  /**
   * Tests the toDerivative method before the first fixing date.
   */
  public void toDerivativeNoFixing() {
    final double lastTtradingTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, LAST_TRADING_DATE);
    final double[] fixingPeriodTime = new double[FIXING_DATE.length];
    for (int loopfix = 0; loopfix < FIXING_DATE.length; loopfix++) {
      fixingPeriodTime[loopfix] = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATE[loopfix]);
    }
    final FederalFundsFutureSecurity futureFedFundExpected = new FederalFundsFutureSecurity(INDEX_FEDFUND, 0.0, fixingPeriodTime, lastTtradingTime, FIXING_ACCURAL_FACTOR,
        FUTURE_FEDFUND_DEFINITION.getFixingTotalAccrualFactor(), NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
    final FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(REFERENCE_DATE);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void toDerivativeAfterStartFixing() {
    FUTURE_FEDFUND_DEFINITION.toDerivative(ScheduleCalculator.getAdjustedDate(FIXING_DATE[0], 2, NYC));
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeFirstDaymonth() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 3, 1);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(DateUtils.getUTCDate(2012, 3, 1), 0.0010);
    final FederalFundsFutureSecurity futureFedFundExpected = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate);
    final FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate, fixingTS);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeSecondDayMonthNoFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 3, 2);
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(DateUtils.getUTCDate(2012, 2, 29), 0.0010);
    final FederalFundsFutureSecurity futureFedFundExpected = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate);
    final FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate, fixingTS);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeSecondDayMonthFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 3, 2);
    final double lastTtradingTime = TimeCalculator.getTimeBetween(referenceDate, LAST_TRADING_DATE);
    final double[] rateFixing = new double[] {0.0010, 0.0011 };
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(
        new ZonedDateTime[] {DateUtils.getUTCDate(2012, 3, 1), DateUtils.getUTCDate(2012, 3, 2) }, rateFixing, ZoneOffset.UTC);
    // Even if 1 and 2 are in time series, only 1 is suppose to be known (reference is 2 and publication lag is 1).
    final double accruedInterest = FIXING_ACCURAL_FACTOR[0] * rateFixing[0];
    final double[] fixingPeriodAccrualFactor = new double[FIXING_ACCURAL_FACTOR.length - 1];
    System.arraycopy(FIXING_ACCURAL_FACTOR, 1, fixingPeriodAccrualFactor, 0, fixingPeriodAccrualFactor.length);
    final double[] fixingPeriodTime = new double[FIXING_DATE.length - 1];
    for (int loopfix = 0; loopfix < FIXING_DATE.length - 1; loopfix++) {
      fixingPeriodTime[loopfix] = TimeCalculator.getTimeBetween(referenceDate, FIXING_DATE[loopfix + 1]);
    }
    final FederalFundsFutureSecurity futureFedFundExpected = new FederalFundsFutureSecurity(INDEX_FEDFUND, accruedInterest, fixingPeriodTime, lastTtradingTime, fixingPeriodAccrualFactor,
        FUTURE_FEDFUND_DEFINITION.getFixingTotalAccrualFactor(), NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
    final FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate, fixingTS);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeMiddleMonthNoFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 3, 7);
    final double lastTtradingTime = TimeCalculator.getTimeBetween(referenceDate, LAST_TRADING_DATE);
    final ZonedDateTime[] dateFixing = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 3, 1), DateUtils.getUTCDate(2012, 3, 2), DateUtils.getUTCDate(2012, 3, 5) };
    final double[] rateFixing = new double[] {0.0010, 0.0011, 0.0012 };
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(dateFixing, rateFixing, ZoneOffset.UTC);
    final double accruedInterest = FIXING_ACCURAL_FACTOR[0] * rateFixing[0] + FIXING_ACCURAL_FACTOR[1] * rateFixing[1] + FIXING_ACCURAL_FACTOR[2] * rateFixing[2];
    final int index = 3;
    final double[] fixingPeriodAccrualFactor = new double[FIXING_ACCURAL_FACTOR.length - index];
    System.arraycopy(FIXING_ACCURAL_FACTOR, index, fixingPeriodAccrualFactor, 0, fixingPeriodAccrualFactor.length);
    final double[] fixingPeriodTime = new double[FIXING_DATE.length - index];
    for (int loopfix = 0; loopfix < FIXING_DATE.length - index; loopfix++) {
      fixingPeriodTime[loopfix] = TimeCalculator.getTimeBetween(referenceDate, FIXING_DATE[loopfix + index]);
    }
    final FederalFundsFutureSecurity futureFedFundExpected = new FederalFundsFutureSecurity(INDEX_FEDFUND, accruedInterest, fixingPeriodTime, lastTtradingTime, fixingPeriodAccrualFactor,
        FUTURE_FEDFUND_DEFINITION.getFixingTotalAccrualFactor(), NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
    final FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate, fixingTS);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeMiddleMonthFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 3, 7);
    final double lastTtradingTime = TimeCalculator.getTimeBetween(referenceDate, LAST_TRADING_DATE);
    final ZonedDateTime[] dateFixing = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 3, 1), DateUtils.getUTCDate(2012, 3, 2), DateUtils.getUTCDate(2012, 3, 5), DateUtils.getUTCDate(2012, 3, 6) };
    final double[] rateFixing = new double[] {0.0010, 0.0011, 0.0012, 0.0013 };
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(dateFixing, rateFixing, ZoneOffset.UTC);
    final double accruedInterest = FIXING_ACCURAL_FACTOR[0] * rateFixing[0] + FIXING_ACCURAL_FACTOR[1] * rateFixing[1] + FIXING_ACCURAL_FACTOR[2] * rateFixing[2] + FIXING_ACCURAL_FACTOR[3] *
        rateFixing[3];
    final int index = 4;
    final double[] fixingPeriodAccrualFactor = new double[FIXING_ACCURAL_FACTOR.length - index];
    System.arraycopy(FIXING_ACCURAL_FACTOR, index, fixingPeriodAccrualFactor, 0, fixingPeriodAccrualFactor.length);
    final double[] fixingPeriodTime = new double[FIXING_DATE.length - index];
    for (int loopfix = 0; loopfix < FIXING_DATE.length - index; loopfix++) {
      fixingPeriodTime[loopfix] = TimeCalculator.getTimeBetween(referenceDate, FIXING_DATE[loopfix + index]);
    }
    final FederalFundsFutureSecurity futureFedFundExpected = new FederalFundsFutureSecurity(INDEX_FEDFUND, accruedInterest, fixingPeriodTime, lastTtradingTime, fixingPeriodAccrualFactor,
        FUTURE_FEDFUND_DEFINITION.getFixingTotalAccrualFactor(), NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
    final FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate, fixingTS);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeEndPeriodNoFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 4, 2);
    final double lastTradingTime = TimeCalculator.getTimeBetween(referenceDate, LAST_TRADING_DATE);
    final ZonedDateTime[] dateFixing = new ZonedDateTime[FIXING_DATE.length - 2];
    System.arraycopy(FIXING_DATE, 0, dateFixing, 0, dateFixing.length);
    final double[] rateFixing = new double[dateFixing.length];
    for (int loopfix = 0; loopfix < dateFixing.length; loopfix++) {
      rateFixing[loopfix] = 0.0010 + loopfix * 0.0001;
    }
    double accruedInterest = 0.0;
    for (int loopfix = 0; loopfix < dateFixing.length; loopfix++) {
      accruedInterest += FIXING_ACCURAL_FACTOR[loopfix] * rateFixing[loopfix];
    }
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(dateFixing, rateFixing, ZoneOffset.UTC);
    final double[] fixingPeriodAccrualFactor = new double[] {FIXING_ACCURAL_FACTOR[FIXING_ACCURAL_FACTOR.length - 1] };
    final double[] fixingPeriodTime = new double[] {TimeCalculator.getTimeBetween(referenceDate, FIXING_DATE[FIXING_DATE.length - 2]),
      TimeCalculator.getTimeBetween(referenceDate, FIXING_DATE[FIXING_DATE.length - 1]) };
    final FederalFundsFutureSecurity futureFedFundExpected = new FederalFundsFutureSecurity(INDEX_FEDFUND, accruedInterest, fixingPeriodTime, lastTradingTime, fixingPeriodAccrualFactor,
        FUTURE_FEDFUND_DEFINITION.getFixingTotalAccrualFactor(), NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
    final FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate, fixingTS);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeEndPeriodFixing() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 4, 2);
    final double lastTradingTime = TimeCalculator.getTimeBetween(referenceDate, LAST_TRADING_DATE);
    final ZonedDateTime[] dateFixing = new ZonedDateTime[FIXING_DATE.length - 1];
    System.arraycopy(FIXING_DATE, 0, dateFixing, 0, dateFixing.length);
    final double[] rateFixing = new double[dateFixing.length];
    for (int loopfix = 0; loopfix < dateFixing.length; loopfix++) {
      rateFixing[loopfix] = 0.0010 + loopfix * 0.0001;
    }
    double accruedInterest = 0.0;
    for (int loopfix = 0; loopfix < dateFixing.length; loopfix++) {
      accruedInterest += FIXING_ACCURAL_FACTOR[loopfix] * rateFixing[loopfix];
    }
    final DoubleTimeSeries<ZonedDateTime> fixingTS = ImmutableZonedDateTimeDoubleTimeSeries.of(dateFixing, rateFixing, ZoneOffset.UTC);
    final double[] fixingPeriodAccrualFactor = new double[0];
    final double[] fixingPeriodTime = new double[] {TimeCalculator.getTimeBetween(referenceDate, FIXING_DATE[FIXING_DATE.length - 1]) };
    final FederalFundsFutureSecurity futureFedFundExpected = new FederalFundsFutureSecurity(INDEX_FEDFUND, accruedInterest, fixingPeriodTime, lastTradingTime, fixingPeriodAccrualFactor,
        FUTURE_FEDFUND_DEFINITION.getFixingTotalAccrualFactor(), NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
    final FederalFundsFutureSecurity futureFedFundConverted = FUTURE_FEDFUND_DEFINITION.toDerivative(referenceDate, fixingTS);
    assertEquals("Fed fund future security definition: toDerivative", futureFedFundExpected, futureFedFundConverted);
  }
}
