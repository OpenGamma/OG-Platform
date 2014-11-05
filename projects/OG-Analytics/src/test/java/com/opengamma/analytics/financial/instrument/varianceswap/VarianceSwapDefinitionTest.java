/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.varianceswap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.AnalyticsTestBase;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the variance swap definition object.
 */
@Test(groups = TestGroup.UNIT)
public class VarianceSwapDefinitionTest extends AnalyticsTestBase {
  /** The current date */
  private static final ZonedDateTime NOW = ZonedDateTime.of(2014, 02, 27, 12, 0, 0, 0, ZoneId.of("UTC"));
  /** The settlement date */
  private static final ZonedDateTime T_PLUS_2 = NOW.plusDays(2);
  /** The maturity date */
  private static final ZonedDateTime PLUS_5Y = NOW.plusYears(5);
  /** The observation frequency */
  private static final PeriodFrequency OBSERVATION_FREQUENCY = PeriodFrequency.DAILY;
  /** The currency */
  private static final Currency CCY = Currency.EUR;
  /** The calendar */
  private static final Calendar WEEKENDCAL = new MondayToFridayCalendar("WEEKEND");
  /** The number of observations per year */
  private static final double OBS_PER_YEAR = 250;
  /** The volatility strike */
  private static final double VOL_STRIKE = 0.25;
  /** The volatility notional */
  private static final double VOL_NOTIONAL = 1.0E6;
  /** A variance swap definition */
  private static final VarianceSwapDefinition DEFINITION = new VarianceSwapDefinition(T_PLUS_2, PLUS_5Y, PLUS_5Y,  CCY,
      WEEKENDCAL, OBS_PER_YEAR, VOL_STRIKE, VOL_NOTIONAL);

  /**
   * @throws Exception If a variance swap definition cannot be created from the inputs
   */
  private VarianceSwapDefinitionTest() throws Exception {
    super(VarianceSwapDefinition.class,
        new Object[] {T_PLUS_2, PLUS_5Y, PLUS_5Y, CCY, WEEKENDCAL, OBS_PER_YEAR, VOL_STRIKE, VOL_NOTIONAL },
        new Class[] {ZonedDateTime.class, ZonedDateTime.class, ZonedDateTime.class,
      Currency.class, Calendar.class, double.class, double.class, double.class },
      new boolean[] {true, true, true, true, true, false, false, false });
  }

  /**
   * Tests the getters.
   */
  @Test
  public void testGetters() {
    assertEquals(OBS_PER_YEAR, DEFINITION.getAnnualizationFactor());
    assertEquals(WEEKENDCAL, DEFINITION.getCalendar());
    assertEquals(CCY, DEFINITION.getCurrency());
    assertEquals(PLUS_5Y, DEFINITION.getObsEndDate());
    assertEquals(1303, DEFINITION.getObsExpected());
    assertEquals(T_PLUS_2, DEFINITION.getObsStartDate());
    assertEquals(PLUS_5Y, DEFINITION.getSettlementDate());
    assertEquals(VOL_NOTIONAL / VOL_STRIKE / 2, DEFINITION.getVarNotional());
    assertEquals(VOL_STRIKE * VOL_STRIKE, DEFINITION.getVarStrike());
    assertEquals(VOL_NOTIONAL, DEFINITION.getVolNotional());
    assertEquals(VOL_STRIKE, DEFINITION.getVolStrike());
  }

  /**
   * Tests the hashcode and equals methods.
   */
  @Test
  public void testHashCodeEquals() {
    VarianceSwapDefinition other = new VarianceSwapDefinition(T_PLUS_2, PLUS_5Y, PLUS_5Y,  CCY,
        WEEKENDCAL, OBS_PER_YEAR, VOL_STRIKE, VOL_NOTIONAL);
    assertEquals(DEFINITION, DEFINITION);
    assertEquals(DEFINITION, other);
    assertEquals(DEFINITION.hashCode(), other.hashCode());
    assertFalse(DEFINITION.equals(null));
    assertFalse(DEFINITION.equals(new CashDefinition(CCY, NOW, PLUS_5Y, VOL_NOTIONAL, VOL_STRIKE, 5)));
    other = new VarianceSwapDefinition(T_PLUS_2.plusDays(1), PLUS_5Y, PLUS_5Y, CCY,
        WEEKENDCAL, OBS_PER_YEAR, VOL_STRIKE, VOL_NOTIONAL);
    assertFalse(other.equals(DEFINITION));
    other = new VarianceSwapDefinition(T_PLUS_2, PLUS_5Y.plusDays(1), PLUS_5Y,  CCY,
        WEEKENDCAL, OBS_PER_YEAR, VOL_STRIKE, VOL_NOTIONAL);
    assertFalse(other.equals(DEFINITION));
    other = new VarianceSwapDefinition(T_PLUS_2, PLUS_5Y, PLUS_5Y.plusDays(1), CCY,
        WEEKENDCAL, OBS_PER_YEAR, VOL_STRIKE, VOL_NOTIONAL);
    assertFalse(other.equals(DEFINITION));
    other = new VarianceSwapDefinition(T_PLUS_2, PLUS_5Y, PLUS_5Y,  Currency.USD,
        WEEKENDCAL, OBS_PER_YEAR, VOL_STRIKE, VOL_NOTIONAL);
    assertFalse(other.equals(DEFINITION));
    other = new VarianceSwapDefinition(T_PLUS_2, PLUS_5Y, PLUS_5Y,  CCY,
        new NoHolidayCalendar(), OBS_PER_YEAR, VOL_STRIKE, VOL_NOTIONAL);
    assertFalse(other.equals(DEFINITION));
    other = new VarianceSwapDefinition(T_PLUS_2, PLUS_5Y, PLUS_5Y,  CCY,
        WEEKENDCAL, OBS_PER_YEAR + 1, VOL_STRIKE, VOL_NOTIONAL);
    assertFalse(other.equals(DEFINITION));
    other = new VarianceSwapDefinition(T_PLUS_2, PLUS_5Y, PLUS_5Y, CCY,
        WEEKENDCAL, OBS_PER_YEAR, VOL_STRIKE + 0.01, VOL_NOTIONAL);
    assertFalse(other.equals(DEFINITION));
    other = new VarianceSwapDefinition(T_PLUS_2, PLUS_5Y, PLUS_5Y,  CCY,
        WEEKENDCAL, OBS_PER_YEAR, VOL_STRIKE, VOL_NOTIONAL * 10);
    assertFalse(other.equals(DEFINITION));
  }

  /**
   * Tests the static constructors.
   */
  @Test
  public void testStaticConstruction() {
    VarianceSwapDefinition definition = VarianceSwapDefinition.fromVegaParams(T_PLUS_2, PLUS_5Y, PLUS_5Y,  CCY, WEEKENDCAL, OBS_PER_YEAR, VOL_STRIKE, VOL_NOTIONAL);
    assertEquals(DEFINITION, definition);
    definition = VarianceSwapDefinition.fromVarianceParams(T_PLUS_2, PLUS_5Y, PLUS_5Y,  CCY, WEEKENDCAL, OBS_PER_YEAR, VOL_STRIKE * VOL_STRIKE, VOL_NOTIONAL / VOL_STRIKE / 2);
    assertEquals(DEFINITION, definition);
  }

  /**
   * Tests creation of a forward-starting variance swap derivative
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testForwardStarting() {
    final VarianceSwap varianceSwap = DEFINITION.toDerivative(NOW, ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(OBS_PER_YEAR, varianceSwap.getAnnualizationFactor());
    assertEquals(CCY, varianceSwap.getCurrency());
    assertEquals(0, varianceSwap.getObsDisrupted());
    ArrayAsserts.assertArrayEquals(new double[0], varianceSwap.getObservations(), 0);
    ArrayAsserts.assertArrayEquals(new double[0], varianceSwap.getObservationWeights(), 0);
    assertEquals(1303, varianceSwap.getObsExpected());
    assertEquals(5, varianceSwap.getTimeToObsEnd(), 0);
    assertEquals(2. / 365, varianceSwap.getTimeToObsStart(), 0);
    assertEquals(5, varianceSwap.getTimeToSettlement(), 0);
    assertEquals(VOL_NOTIONAL / VOL_STRIKE / 2, varianceSwap.getVarNotional(), 0);
    assertEquals(VOL_STRIKE * VOL_STRIKE, varianceSwap.getVarStrike());
    assertEquals(VOL_NOTIONAL, varianceSwap.getVolNotional());
    assertEquals(VOL_STRIKE, varianceSwap.getVolStrike(), 0);
    assertEquals(varianceSwap, DEFINITION.toDerivative(NOW));
  }

  /**
   * Tests creation of a seasoned variance swap derivative
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testSeasoned() {
    final VarianceSwapDefinition definition = new VarianceSwapDefinition(NOW, PLUS_5Y, PLUS_5Y,  CCY,
        new NoHolidayCalendar(), OBS_PER_YEAR, VOL_STRIKE, VOL_NOTIONAL);
    final LocalDate[] dates = new LocalDate[365];
    final double[] vars = new double[365];
    LocalDate date = NOW.toLocalDate();
    for (int i = 0; i < 365; i++) {
      dates[i] = date;
      vars[i] = 0.01;
      date = date.plusDays(1);
    }
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(dates, vars);
    final VarianceSwap varianceSwap = definition.toDerivative(NOW.plusYears(1), ts);
    assertEquals(OBS_PER_YEAR, varianceSwap.getAnnualizationFactor());
    assertEquals(CCY, varianceSwap.getCurrency());
    assertEquals(0, varianceSwap.getObsDisrupted());
    ArrayAsserts.assertArrayEquals(vars, varianceSwap.getObservations(), 0);
    ArrayAsserts.assertArrayEquals(new double[0], varianceSwap.getObservationWeights(), 0);
    assertEquals(1827, varianceSwap.getObsExpected());
    assertEquals(4, varianceSwap.getTimeToObsEnd(), 0);
    assertEquals(-1, varianceSwap.getTimeToObsStart(), 0);
    assertEquals(4, varianceSwap.getTimeToSettlement(), 0);
    assertEquals(VOL_NOTIONAL / VOL_STRIKE / 2, varianceSwap.getVarNotional(), 0);
    assertEquals(VOL_STRIKE * VOL_STRIKE, varianceSwap.getVarStrike());
    assertEquals(VOL_NOTIONAL, varianceSwap.getVolNotional());
    assertEquals(VOL_STRIKE, varianceSwap.getVolStrike(), 0);
    assertEquals(varianceSwap, definition.toDerivative(NOW.plusYears(1), ts, "A", "B"));
  }

}
