/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class InterpolatedSmileFunctionTest {
  private static final double FORWARD = 0.23;
  private static final double EXPIRY = 3.0;
  private static final int NUM_DATA = 11;
  private static final double[] STRIKES = new double[NUM_DATA];
  private static final double[] VOLS = new double[NUM_DATA];
  static {
    for (int i = 0; i < NUM_DATA; ++i) {
      STRIKES[i] = FORWARD * (0.8 + 0.05 * i);
      VOLS[i] = 0.25 * (Math.cos(Math.sqrt(i) / 3.0) + i * i / 240.);
    }
  }

  /**
   * Checking consistency with method of underlying interpolator
   */
  @Test
  public void consistencyTest() {

    GeneralSmileInterpolator[] interpolators = new GeneralSmileInterpolator[] {
        new SmileInterpolatorMixedLogNormal(),
        new SmileInterpolatorSpline(), new SmileInterpolatorSABR() };
    int nInterps = interpolators.length;
    int nSamples = 40;

    for (int i = 0; i < nInterps; ++i) {
      Function1D<Double, Double> refFunc = interpolators[i].getVolatilityFunction(FORWARD, STRIKES, EXPIRY, VOLS);
      InterpolatedSmileFunction func = new InterpolatedSmileFunction(interpolators[i], FORWARD, STRIKES, EXPIRY, VOLS);
      assertEquals(interpolators[i], func.getInterpolator());
      for (int j = 0; j < nSamples; ++j) {
        Double key = FORWARD * (0.01 + 0.1 * j);
        assertEquals(refFunc.evaluate(key), func.getVolatility(key), 1.e-8); //due to randomness of SABR
      }
    }
  }

  /**
   * 
   */
  @Test
  public void hashCodeEqualsTest() {
    GeneralSmileInterpolator[] interpolators = new GeneralSmileInterpolator[] {new SmileInterpolatorSpline(),
        new SmileInterpolatorSABR() };
    InterpolatedSmileFunction func1 = new InterpolatedSmileFunction(interpolators[0], FORWARD, STRIKES, EXPIRY, VOLS);
    InterpolatedSmileFunction func2 = new InterpolatedSmileFunction(interpolators[1], FORWARD, STRIKES, EXPIRY, VOLS);
    InterpolatedSmileFunction func3 = new InterpolatedSmileFunction(interpolators[0], FORWARD * 0.9, STRIKES, EXPIRY,
        VOLS);
    InterpolatedSmileFunction func4 = new InterpolatedSmileFunction(new SmileInterpolatorSpline(), FORWARD, STRIKES,
        EXPIRY, VOLS);
    InterpolatedSmileFunction func5 = func1;

    assertTrue(func1.equals(func1));

    assertFalse(func1.hashCode() == func2.hashCode());
    assertFalse(func1.equals(func2));
    assertFalse(func1.equals(func2));

    assertFalse(func1.hashCode() == func3.hashCode());
    assertFalse(func1.equals(func3));
    assertFalse(func1.equals(func3));

    assertFalse(func1.hashCode() == func4.hashCode());
    assertFalse(func1.equals(func4));
    assertFalse(func1.equals(func4));

    assertTrue(func1.equals(func5));
    assertTrue(func5.equals(func1));
    assertTrue(func1.hashCode() == func5.hashCode());

    assertFalse(func1.equals(null));
    assertFalse(func1.equals(interpolators[0]));
  }
}
