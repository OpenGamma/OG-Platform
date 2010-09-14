/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskreward;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class JensenAlphaCalculatorTest {

  @Test
  public void test() {
    final double assetReturn = 0.12;
    final double riskFreeReturn = 0.03;
    final double beta = 0.7;
    final double marketReturn = 0.11;
    assertEquals(new JensenAlphaCalculator().calculate(assetReturn, riskFreeReturn, beta, marketReturn), 0.034, 1e-3);
  }
}
