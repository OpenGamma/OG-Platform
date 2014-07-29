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
 *  Test for the end of month temporal adjuster
 */
@Test(groups = TestGroup.UNIT)
public class EndOfMonthTemporalAdjusterTest {
  private static final TemporalAdjuster ADJUSTER = EndOfMonthTemporalAdjuster.getAdjuster();

  @Test
  public void test() {
    for (int i = 1; i < 30; i++) {
      final LocalDate date = LocalDate.of(2013, 1, i);
      assertEquals(LocalDate.of(2013, 1, 31), ADJUSTER.adjustInto(date));
    }
  }

}
