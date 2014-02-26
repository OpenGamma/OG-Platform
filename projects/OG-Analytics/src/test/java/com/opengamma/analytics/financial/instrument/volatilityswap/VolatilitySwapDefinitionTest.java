/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.volatilityswap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the volatility swap definition object.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilitySwapDefinitionTest {
  /** The current date */
  private static final ZonedDateTime NOW = ZonedDateTime.now();
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
  /** A Volatility swap definition */
  private static final VolatilitySwapDefinition DEFINITION = new VolatilitySwapDefinition(CCY, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2, PLUS_5Y,
      T_PLUS_2, PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDCAL);

  /**
   * Tests the getters.
   */
  @Test
  public void testGetters() {
    assertEquals(OBS_PER_YEAR, DEFINITION.getAnnualizationFactor());
    assertEquals(WEEKENDCAL, DEFINITION.getCalendar());
    assertEquals(CCY, DEFINITION.getCurrency());
    assertEquals(PLUS_5Y, DEFINITION.getObservationEndDate());
    assertEquals(1303, DEFINITION.getNumberOfObservationsExpected());
    assertEquals(OBSERVATION_FREQUENCY, DEFINITION.getObservationFrequency());
    assertEquals(T_PLUS_2, DEFINITION.getObservationStartDate());
    assertEquals(T_PLUS_2, DEFINITION.getEffectiveDate());
    assertEquals(PLUS_5Y, DEFINITION.getSettlementDate());
    assertEquals(VOL_NOTIONAL, DEFINITION.getVolatilityNotional());
    assertEquals(VOL_STRIKE, DEFINITION.getVolatilityStrike());
  }

  /**
   * Tests the hashcode and equals methods.
   */
  @Test
  public void testHashCodeEquals() {
    VolatilitySwapDefinition other = new VolatilitySwapDefinition(CCY, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2, PLUS_5Y,
        T_PLUS_2, PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDCAL);
    assertEquals(DEFINITION, DEFINITION);
    assertEquals(DEFINITION, other);
    assertEquals(DEFINITION.hashCode(), other.hashCode());
    assertFalse(DEFINITION.equals(null));
    assertFalse(DEFINITION.equals(new CashDefinition(CCY, NOW, PLUS_5Y, VOL_NOTIONAL, VOL_STRIKE, 5)));
    other = new VolatilitySwapDefinition(Currency.USD, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2, PLUS_5Y,
        T_PLUS_2, PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDCAL);
    assertFalse(other.equals(DEFINITION));
    other = new VolatilitySwapDefinition(CCY, VOL_STRIKE + 0.01, VOL_NOTIONAL, T_PLUS_2, PLUS_5Y,
        T_PLUS_2, PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDCAL);
    assertFalse(other.equals(DEFINITION));
    other = new VolatilitySwapDefinition(CCY, VOL_STRIKE, VOL_NOTIONAL * 10, T_PLUS_2, PLUS_5Y,
        T_PLUS_2, PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDCAL);
    assertFalse(other.equals(DEFINITION));
    other = new VolatilitySwapDefinition(CCY, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2.plusDays(1), PLUS_5Y,
        T_PLUS_2, PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDCAL);
    assertFalse(other.equals(DEFINITION));
    other = new VolatilitySwapDefinition(CCY, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2, PLUS_5Y.plusDays(1),
        T_PLUS_2, PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDCAL);
    assertFalse(other.equals(DEFINITION));
    other = new VolatilitySwapDefinition(CCY, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2, PLUS_5Y,
        T_PLUS_2.plusDays(1), PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDCAL);
    assertFalse(other.equals(DEFINITION));
    other = new VolatilitySwapDefinition(CCY, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2, PLUS_5Y,
        T_PLUS_2, PLUS_5Y.plusDays(1), OBSERVATION_FREQUENCY, OBS_PER_YEAR, WEEKENDCAL);
    assertFalse(other.equals(DEFINITION));
    other = new VolatilitySwapDefinition(CCY, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2, PLUS_5Y,
        T_PLUS_2, PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR + 1, WEEKENDCAL);
    assertFalse(other.equals(DEFINITION));
    other = new VolatilitySwapDefinition(CCY, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2, PLUS_5Y,
        T_PLUS_2, PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, new NoHolidayCalendar());
    assertFalse(other.equals(DEFINITION));
  }

  //  /**
  //   * Tests creation of a forward-starting volatility swap derivative
  //   */
  //  @Test
  //  public void testForwardStarting() {
  //    final VolatilitySwap volatilitySwap = DEFINITION.toDerivative(NOW, ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
  //    assertEquals(OBS_PER_YEAR, volatilitySwap.getAnnualizationFactor());
  //    assertEquals(CCY, volatilitySwap.getCurrency());
  //    assertEquals(0, volatilitySwap.getObsDisrupted());
  //    ArrayAsserts.assertArrayEquals(new double[0], volatilitySwap.getObservations(), 0);
  //    ArrayAsserts.assertArrayEquals(new double[0], volatilitySwap.getObservationWeights(), 0);
  //    assertEquals(1303, volatilitySwap.getObsExpected());
  //    assertEquals(5, volatilitySwap.getTimeToObsEnd(), 0);
  //    assertEquals(2. / 365, volatilitySwap.getTimeToObsStart(), 0);
  //    assertEquals(5, volatilitySwap.getTimeToSettlement(), 0);
  //    assertEquals(VOL_NOTIONAL / VOL_STRIKE / 2, volatilitySwap.getVarNotional(), 0);
  //    assertEquals(VOL_STRIKE * VOL_STRIKE, volatilitySwap.getVarStrike());
  //    assertEquals(VOL_NOTIONAL, volatilitySwap.getVolNotional());
  //    assertEquals(VOL_STRIKE, volatilitySwap.getVolStrike(), 0);
  //    assertEquals(volatilitySwap, DEFINITION.toDerivative(NOW));
  //    assertEquals(volatilitySwap, DEFINITION.toDerivative(NOW, "A", "B"));
  //  }
  //
  //  /**
  //   * Tests creation of a seasoned volatility swap derivative
  //   */
  //  @Test
  //  public void testSeasoned() {
  //    final VolatilitySwapDefinition definition = new VolatilitySwapDefinition(CCY, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2, PLUS_5Y,
  //        T_PLUS_2, PLUS_5Y, OBSERVATION_FREQUENCY, OBS_PER_YEAR, new NoHolidayCalendar());
  //    final LocalDate[] dates = new LocalDate[365];
  //    final double[] vars = new double[365];
  //    LocalDate date = NOW.toLocalDate();
  //    for (int i = 0; i < 365; i++) {
  //      dates[i] = date;
  //      vars[i] = 0.01;
  //      date = date.plusDays(1);
  //    }
  //    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(dates, vars);
  //    final VolatilitySwap VolatilitySwap = definition.toDerivative(NOW.plusYears(1), ts);
  //    assertEquals(OBS_PER_YEAR, VolatilitySwap.getAnnualizationFactor());
  //    assertEquals(CCY, VolatilitySwap.getCurrency());
  //    assertEquals(1, VolatilitySwap.getObsDisrupted());
  //    ArrayAsserts.assertArrayEquals(vars, VolatilitySwap.getObservations(), 0);
  //    ArrayAsserts.assertArrayEquals(new double[0], VolatilitySwap.getObservationWeights(), 0);
  //    assertEquals(1827, VolatilitySwap.getObsExpected());
  //    assertEquals(4, VolatilitySwap.getTimeToObsEnd(), 0);
  //    assertEquals(-1, VolatilitySwap.getTimeToObsStart(), 0);
  //    assertEquals(4, VolatilitySwap.getTimeToSettlement(), 0);
  //    assertEquals(VOL_NOTIONAL / VOL_STRIKE / 2, VolatilitySwap.getVarNotional(), 0);
  //    assertEquals(VOL_STRIKE * VOL_STRIKE, VolatilitySwap.getVarStrike());
  //    assertEquals(VOL_NOTIONAL, VolatilitySwap.getVolNotional());
  //    assertEquals(VOL_STRIKE, VolatilitySwap.getVolStrike(), 0);
  //    assertEquals(VolatilitySwap, definition.toDerivative(NOW.plusYears(1), ts, "A", "B"));
  //  }

  /**
   * Tests expected failure for observation frequencies other than daily 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWeeklyObservations() {
    final PeriodFrequency freqWeek = PeriodFrequency.WEEKLY;
    new VolatilitySwapDefinition(CCY, VOL_STRIKE, VOL_NOTIONAL, T_PLUS_2, PLUS_5Y, T_PLUS_2, PLUS_5Y, freqWeek, OBS_PER_YEAR, WEEKENDCAL);
  }
}
