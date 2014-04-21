/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.interpolation.data.InterpolatorNDDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RadialBasisFunctionInterpolatorSensitivityCalculatorTest extends InterpolatorNDTestCase {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);

  @Test
  public void testFlat() {
    final double r0 = 1.0;
    final InterpolatorND interpolator = new RadialBasisFunctionInterpolatorND(new GaussianRadialBasisFunction(r0), false);

    final InterpolatorNDDataBundle dataBundle = interpolator.getDataBundle(FLAT_DATA);
    final double[] point = FLAT_DATA.get(5).getFirst();
    final Map<double[], Double> res = interpolator.getNodeSensitivitiesForValue(dataBundle, point);
    assertEquals(1.0, res.get(point), 0.0);
    res.remove(point);

    for (final Map.Entry<double[], Double> entry : res.entrySet()) {
      assertEquals(0.0, entry.getValue(), 0.0);
    }
  }

  @Test
  public void testInverseMultiquadraticRadialBasisFunction() {
    final double r0 = 2;
    final InterpolatorND interpolator = new RadialBasisFunctionInterpolatorND(new InverseMultiquadraticRadialBasisFunction(r0), true);
    final InterpolatorNDDataBundle dataBundle = interpolator.getDataBundle(COS_EXP_DATA);
    final double[] point = COS_EXP_DATA.get(3).getFirst();
    final Map<double[], Double> res = interpolator.getNodeSensitivitiesForValue(dataBundle, point);
    assertEquals(1.0, res.get(point), 1e-13);
    res.remove(point);

    for (final Map.Entry<double[], Double> entry : res.entrySet()) {
      assertEquals(0.0, entry.getValue(), 1e-11);
    }
  }

  @Override
  protected RandomEngine getRandom() {
    return RANDOM;
  }

}
