/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import org.junit.Test;

/**
 * @author emcleod
 * 
 */
public class PNLMeanCalculatorTest {

  @Test(expected = IllegalArgumentException.class)
  public void test() {
    new PNLMeanCalculator().evaluate((HistoricalVaRDataBundle) null);
  }
}
