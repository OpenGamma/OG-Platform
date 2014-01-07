/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NormalVaRParametersTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = 0.998650101968370;
  private static final NormalVaRParameters PARAMETERS = new NormalVaRParameters(HORIZON, PERIODS, QUANTILE);
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeHorizon() {
    new NormalVaRParameters(-HORIZON, PERIODS, QUANTILE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePeriod() {
    new NormalVaRParameters(HORIZON, -PERIODS, QUANTILE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeQuantile() {
    new NormalVaRParameters(HORIZON, PERIODS, -QUANTILE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighQuantile() {
    new NormalVaRParameters(HORIZON, PERIODS, 1 + QUANTILE);
  }

  @Test
  public void testHashCodeAndEquals() {
    assertEquals(PARAMETERS.getHorizon(), HORIZON, 0);
    assertEquals(PARAMETERS.getPeriods(), PERIODS, 0);
    assertEquals(PARAMETERS.getQuantile(), QUANTILE, 0);
    assertEquals(3, PARAMETERS.getZ(), 1e-12);
    assertEquals(0.2, PARAMETERS.getTimeScaling(), 1e-12);
    NormalVaRParameters other = new NormalVaRParameters(HORIZON, PERIODS, QUANTILE);
    assertEquals(PARAMETERS, other);
    other = new NormalVaRParameters(HORIZON + 1, PERIODS, QUANTILE);
    assertFalse(other.equals(PARAMETERS));
    other = new NormalVaRParameters(HORIZON, PERIODS + 1, QUANTILE);
    assertFalse(other.equals(PARAMETERS));
    other = new NormalVaRParameters(HORIZON, PERIODS, QUANTILE * 0.5);
    assertFalse(other.equals(PARAMETERS));    
  }
}
