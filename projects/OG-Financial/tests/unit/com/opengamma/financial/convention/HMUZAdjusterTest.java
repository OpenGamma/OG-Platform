/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

/**
 *
 */
public class HMUZAdjusterTest {
  private static final HMUZAdjuster ADJUSTER = HMUZAdjuster.getInstance();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    ADJUSTER.adjustDate(null);
  }

  @Test
  public void test() {
    assertEquals(LocalDate.of(2012, 3, 1), ADJUSTER.adjustDate(LocalDate.of(2012, 1, 1)));
    assertEquals(LocalDate.of(2012, 3, 1), ADJUSTER.adjustDate(LocalDate.of(2012, 2, 1)));
    assertEquals(LocalDate.of(2012, 3, 1), ADJUSTER.adjustDate(LocalDate.of(2012, 3, 1)));
    assertEquals(LocalDate.of(2012, 6, 1), ADJUSTER.adjustDate(LocalDate.of(2012, 4, 1)));
    assertEquals(LocalDate.of(2012, 6, 1), ADJUSTER.adjustDate(LocalDate.of(2012, 5, 1)));
    assertEquals(LocalDate.of(2012, 6, 1), ADJUSTER.adjustDate(LocalDate.of(2012, 6, 1)));
    assertEquals(LocalDate.of(2012, 9, 1), ADJUSTER.adjustDate(LocalDate.of(2012, 7, 1)));
    assertEquals(LocalDate.of(2012, 9, 1), ADJUSTER.adjustDate(LocalDate.of(2012, 8, 1)));
    assertEquals(LocalDate.of(2012, 9, 1), ADJUSTER.adjustDate(LocalDate.of(2012, 9, 1)));
    assertEquals(LocalDate.of(2012, 12, 1), ADJUSTER.adjustDate(LocalDate.of(2012, 10, 1)));
    assertEquals(LocalDate.of(2012, 12, 1), ADJUSTER.adjustDate(LocalDate.of(2012, 11, 1)));
    assertEquals(LocalDate.of(2012, 12, 1), ADJUSTER.adjustDate(LocalDate.of(2012, 12, 1)));
  }
}
