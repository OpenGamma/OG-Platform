/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.math.statistics.distribution.NormalDistribution;

/**
 * @author emcleod
 * 
 */
public class VaRCalculatorTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = new NormalDistribution(0, 1).getCDF(3.);
  private static final VaRCalculator<NormalStatistics<?>> V = new MyVaRCalculator(HORIZON, PERIODS, QUANTILE);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeHorizon() {
    new MyVaRCalculator(-HORIZON, PERIODS, QUANTILE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePeriods() {
    new MyVaRCalculator(HORIZON, -PERIODS, QUANTILE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLowQuantile() {
    new MyVaRCalculator(HORIZON, PERIODS, -0.1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighQuantile() {
    new MyVaRCalculator(HORIZON, PERIODS, 1.1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetHorizon() {
    V.setHorizon(-HORIZON);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetPeriods() {
    V.setPeriods(-PERIODS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetLowQuantile() {
    V.setQuantile(-0.1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetHighQuantile() {
    V.setQuantile(1.1);
  }

  @Test
  public void testGetters() {
    assertTrue(V.getHorizon() == HORIZON);
    assertTrue(V.getPeriods() == PERIODS);
    assertTrue(V.getQuantile() == QUANTILE);
  }

  private static class MyVaRCalculator extends VaRCalculator<NormalStatistics<?>> {

    public MyVaRCalculator(final double horizon, final double periods, final double quantile) {
      super(horizon, periods, quantile);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
     */
    @Override
    public Double evaluate(final NormalStatistics<?> x) {
      return null;
    }

  }
}
