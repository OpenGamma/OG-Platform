/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RiskFactorResultTest {

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor() {
    new RiskFactorResult(null);
  }

  @Test
  public void test() {
    final double value = 1.2;
    final RiskFactorResult result = new RiskFactorResult(value);
    assertEquals(result.getResult(), value, 0);

  }
}
