/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.equity.variance.definition.VarianceSwapDefinition;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.money.Currency;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;

/**
 * 
 */
public class VarianceSwapDefinitionTest {

  private final ZonedDateTime now = ZonedDateTime.now();
  private final ZonedDateTime tPlus2 = now.plusDays(2);
  @SuppressWarnings("unused")
  private final ZonedDateTime plus1y = now.plusYears(1);
  private final ZonedDateTime plus5y = now.plusYears(5);
  private final PeriodFrequency obsFreq = PeriodFrequency.DAILY;
  private final Currency ccy = Currency.EUR;
  private static final Calendar WEEKENDCAL = new MondayToFridayCalendar("WEEKEND");
  private final double obsPerYear = 250;
  private final double volStrike = 0.25;
  private final double volNotional = 1.0E6;

  private final DoubleTimeSeries<LocalDate> emptyTimeSeries = new ArrayLocalDateDoubleTimeSeries(new LocalDate[0], new double[0]);

  @Test
  public void forwardStarting() {
    // Construct a forward starting swap, Definition
    final VarianceSwapDefinition varSwapDefn = new VarianceSwapDefinition(tPlus2, plus5y, plus5y, obsFreq, ccy, WEEKENDCAL, obsPerYear, volStrike, volNotional);
    // Construct a forward starting swap, Derivative 
    varSwapDefn.toDerivative(now, emptyTimeSeries);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  // FIXME Failing on purpose so that we don't forget to extend
  public void weeklyObservations() {
    final PeriodFrequency freqWeek = PeriodFrequency.WEEKLY;
    new VarianceSwapDefinition(tPlus2, plus5y, plus5y, freqWeek, ccy, WEEKENDCAL, obsPerYear, volStrike, volNotional);
  }
}
