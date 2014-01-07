/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
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
    new PeriodFrequency(null, Period.ofDays(2));
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
    assertEquals(PeriodFrequency.ANNUAL.getPeriod(), Period.ofYears(1));
    assertEquals(PeriodFrequency.BIMONTHLY.getPeriod(), Period.ofMonths(2));
    assertEquals(PeriodFrequency.BIWEEKLY.getPeriod(), Period.ofDays(14));
    assertEquals(PeriodFrequency.CONTINUOUS.getPeriod(), Period.ZERO);
    assertEquals(PeriodFrequency.DAILY.getPeriod(), Period.ofDays(1));
    assertEquals(PeriodFrequency.MONTHLY.getPeriod(), Period.ofMonths(1));
    assertEquals(PeriodFrequency.QUARTERLY.getPeriod(), Period.ofMonths(3));
    assertEquals(PeriodFrequency.SEMI_ANNUAL.getPeriod(), Period.ofMonths(6));
    assertEquals(PeriodFrequency.WEEKLY.getPeriod(), Period.ofDays(7));
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
