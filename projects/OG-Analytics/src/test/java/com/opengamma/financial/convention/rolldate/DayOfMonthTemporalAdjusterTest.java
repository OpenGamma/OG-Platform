/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rolldate;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.opengamma.util.test.TestGroup;

/**
 * Test for the day of month temporal adjuster
 */
@Test(groups = TestGroup.UNIT)
public class DayOfMonthTemporalAdjusterTest {

  @Test
  public void testFirstMonth() {
    final LocalDate date = LocalDate.of(2013, 1, 1);
    for (int i = 1; i < 30; i++) {
      final TemporalAdjuster ADJUSTER = new DayOfMonthTemporalAdjuster(i);
      assertEquals(LocalDate.of(2013, 1, i), ADJUSTER.adjustInto(date));
    }
  }
}
