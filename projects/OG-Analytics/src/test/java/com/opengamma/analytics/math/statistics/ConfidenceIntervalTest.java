/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ConfidenceIntervalTest {
  private static final double VALUE = 100;
  private static final double LOWER = 50;
  private static final double UPPER = 120;
  private static final double CONFIDENCE_LEVEL = 0.95;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowLevel() {
    new ConfidenceInterval(VALUE, LOWER, UPPER, -CONFIDENCE_LEVEL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighLevel() {
    new ConfidenceInterval(VALUE, LOWER, UPPER, 1 + CONFIDENCE_LEVEL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBounds() {
    new ConfidenceInterval(VALUE, UPPER, LOWER, CONFIDENCE_LEVEL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowerBound() {
    new ConfidenceInterval(UPPER + 1, LOWER, UPPER, CONFIDENCE_LEVEL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUpperBound() {
    new ConfidenceInterval(LOWER - 1, LOWER, UPPER, CONFIDENCE_LEVEL);
  }

  @Test
  public void test() {
    final ConfidenceInterval interval1 = new ConfidenceInterval(VALUE, LOWER, UPPER, CONFIDENCE_LEVEL);
    assertEquals(interval1.getValue(), VALUE, 0);
    assertEquals(interval1.getConfidenceLevel(), CONFIDENCE_LEVEL, 0);
    assertEquals(interval1.getLowerInterval(), LOWER, 0);
    assertEquals(interval1.getUpperInterval(), UPPER, 0);
    assertFalse(interval1.isWithinInterval(4.5));
    assertFalse(interval1.isWithinInterval(-1));
    assertTrue(interval1.isWithinInterval(112.5));
    final ConfidenceInterval interval2 = new ConfidenceInterval(VALUE + 1, LOWER, UPPER, CONFIDENCE_LEVEL);
    final ConfidenceInterval interval3 = new ConfidenceInterval(VALUE, LOWER + 1, UPPER, CONFIDENCE_LEVEL);
    final ConfidenceInterval interval4 = new ConfidenceInterval(VALUE, LOWER, UPPER + 1, CONFIDENCE_LEVEL);
    final ConfidenceInterval interval5 = new ConfidenceInterval(VALUE, LOWER, UPPER, CONFIDENCE_LEVEL + 0.01);
    final ConfidenceInterval interval6 = new ConfidenceInterval(VALUE, LOWER, UPPER, CONFIDENCE_LEVEL);
    assertEquals(interval1, interval1);
    assertEquals(interval1, interval6);
    assertEquals(interval1.hashCode(), interval6.hashCode());
    assertFalse(interval1.equals(interval2));
    assertFalse(interval1.equals(null));
    assertFalse(interval1.equals(2));
    assertFalse(interval1.equals(interval3));
    assertFalse(interval1.equals(interval4));
    assertFalse(interval1.equals(interval5));
  }
}
