/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class HMUZAdjusterTest {

  private static final HMUZAdjuster ADJUSTER = HMUZAdjuster.getInstance();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    ADJUSTER.adjustInto(null);
  }

  @Test
  public void test_LocalDate() {
    assertEquals(LocalDate.of(2012, 3, 1), ADJUSTER.adjustInto(LocalDate.of(2012, 1, 1)));
    assertEquals(LocalDate.of(2012, 3, 1), ADJUSTER.adjustInto(LocalDate.of(2012, 2, 1)));
    assertEquals(LocalDate.of(2012, 3, 1), ADJUSTER.adjustInto(LocalDate.of(2012, 3, 1)));
    assertEquals(LocalDate.of(2012, 6, 1), ADJUSTER.adjustInto(LocalDate.of(2012, 4, 1)));
    assertEquals(LocalDate.of(2012, 6, 1), ADJUSTER.adjustInto(LocalDate.of(2012, 5, 1)));
    assertEquals(LocalDate.of(2012, 6, 1), ADJUSTER.adjustInto(LocalDate.of(2012, 6, 1)));
    assertEquals(LocalDate.of(2012, 9, 1), ADJUSTER.adjustInto(LocalDate.of(2012, 7, 1)));
    assertEquals(LocalDate.of(2012, 9, 1), ADJUSTER.adjustInto(LocalDate.of(2012, 8, 1)));
    assertEquals(LocalDate.of(2012, 9, 1), ADJUSTER.adjustInto(LocalDate.of(2012, 9, 1)));
    assertEquals(LocalDate.of(2012, 12, 1), ADJUSTER.adjustInto(LocalDate.of(2012, 10, 1)));
    assertEquals(LocalDate.of(2012, 12, 1), ADJUSTER.adjustInto(LocalDate.of(2012, 11, 1)));
    assertEquals(LocalDate.of(2012, 12, 1), ADJUSTER.adjustInto(LocalDate.of(2012, 12, 1)));
  }

  @Test
  public void test_LocalDateTime() {
    assertEquals(LocalDateTime.of(2012, 3, 1, 0, 0), ADJUSTER.adjustInto(LocalDateTime.of(2012, 1, 1, 0, 0)));
  }

}
