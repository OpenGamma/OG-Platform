/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.fra.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class ForwardRateAgreementTest {
  public static final String CURVE_NAME = "test";

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeStart() {
    new ForwardRateAgreement(-2, 2, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeEnd() {
    new ForwardRateAgreement(2, -2, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStartAfterEnd() {
    new ForwardRateAgreement(3, 2, CURVE_NAME);
  }

  @Test
  public void test() {
    final double start = 3;
    final double end = 6;
    final ForwardRateAgreement fra = new ForwardRateAgreement(start, end, CURVE_NAME);
    assertEquals(fra.getStartTime(), start, 0);
    assertEquals(fra.getEndTime(), end, 0);
    ForwardRateAgreement other = new ForwardRateAgreement(start, end, CURVE_NAME);
    assertEquals(fra, other);
    assertEquals(fra.hashCode(), other.hashCode());
    other = new ForwardRateAgreement(start, end + 1, CURVE_NAME);
    assertFalse(other.equals(fra));
    other = new ForwardRateAgreement(start + 1, end, CURVE_NAME);
    assertFalse(other.equals(fra));
  }
}
