/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

import static org.testng.AssertJUnit.assertEquals;
import static org.threeten.bp.temporal.ChronoUnit.DAYS;
import static org.threeten.bp.temporal.ChronoUnit.MONTHS;
import static org.threeten.bp.temporal.ChronoUnit.YEARS;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

/**
 * 
 */
public class FrequencyTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName1() {
    new SimpleFrequency(null, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeFrequency() {
    new SimpleFrequency("a", -32);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName2() {
    new PeriodFrequency(null, Period.of(2, DAYS));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPeriod() {
    new PeriodFrequency("X", null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoEquivalent() {
    new SimpleFrequency("a", 23).toPeriodFrequency();
  }

  @Test
  public void testPredefinedSimpleFrequencies() {
    assertEquals(SimpleFrequency.ANNUAL.getPeriodsPerYear(), 1, 0);
    assertEquals(SimpleFrequency.BIMONTHLY.getPeriodsPerYear(), 6, 0);
    assertEquals(SimpleFrequency.BIWEEKLY.getPeriodsPerYear(), 26, 0);
    assertEquals(SimpleFrequency.CONTINUOUS.getPeriodsPerYear(), Double.MAX_VALUE, 0);
    assertEquals(SimpleFrequency.DAILY.getPeriodsPerYear(), 365, 0);
    assertEquals(SimpleFrequency.MONTHLY.getPeriodsPerYear(), 12, 0);
    assertEquals(SimpleFrequency.QUARTERLY.getPeriodsPerYear(), 4, 0);
    assertEquals(SimpleFrequency.SEMI_ANNUAL.getPeriodsPerYear(), 2, 0);
    assertEquals(SimpleFrequency.WEEKLY.getPeriodsPerYear(), 52, 0);
  }

  @Test
  public void testPredefinedPeriodFrequencies() {
    assertEquals(PeriodFrequency.ANNUAL.getPeriod(), Period.of(1, YEARS));
    assertEquals(PeriodFrequency.BIMONTHLY.getPeriod(), Period.of(2, MONTHS));
    assertEquals(PeriodFrequency.BIWEEKLY.getPeriod(), Period.of(14, DAYS));
    assertEquals(PeriodFrequency.CONTINUOUS.getPeriod(), Period.ZERO);
    assertEquals(PeriodFrequency.DAILY.getPeriod(), Period.of(1, DAYS));
    assertEquals(PeriodFrequency.MONTHLY.getPeriod(), Period.of(1, MONTHS));
    assertEquals(PeriodFrequency.QUARTERLY.getPeriod(), Period.of(3, MONTHS));
    assertEquals(PeriodFrequency.SEMI_ANNUAL.getPeriod(), Period.of(6, MONTHS));
    assertEquals(PeriodFrequency.WEEKLY.getPeriod(), Period.of(7, DAYS));
  }

  @Test
  public void testConvert() {
    assertEquals(SimpleFrequency.ANNUAL.toPeriodFrequency(), PeriodFrequency.ANNUAL);
    assertEquals(SimpleFrequency.BIMONTHLY.toPeriodFrequency(), PeriodFrequency.BIMONTHLY);
    assertEquals(SimpleFrequency.BIWEEKLY.toPeriodFrequency(), PeriodFrequency.BIWEEKLY);
    assertEquals(SimpleFrequency.CONTINUOUS.toPeriodFrequency(), PeriodFrequency.CONTINUOUS);
    assertEquals(SimpleFrequency.DAILY.toPeriodFrequency(), PeriodFrequency.DAILY);
    assertEquals(SimpleFrequency.MONTHLY.toPeriodFrequency(), PeriodFrequency.MONTHLY);
    assertEquals(SimpleFrequency.SEMI_ANNUAL.toPeriodFrequency(), PeriodFrequency.SEMI_ANNUAL);
    assertEquals(SimpleFrequency.WEEKLY.toPeriodFrequency(), PeriodFrequency.WEEKLY);
  }
}
