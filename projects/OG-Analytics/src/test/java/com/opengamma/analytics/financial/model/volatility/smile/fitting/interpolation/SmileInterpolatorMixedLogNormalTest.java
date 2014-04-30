/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SmileInterpolatorMixedLogNormalTest extends SmileInterpolatorTestCase {

  private static final GeneralSmileInterpolator INTERPOLATOR = new SmileInterpolatorMixedLogNormal();

  @Override
  public GeneralSmileInterpolator getSmileInterpolator() {
    return INTERPOLATOR;
  }

  @Test
  public void testEqualsHashCode() {
    GeneralSmileInterpolator other = new SmileInterpolatorMixedLogNormal();
    assertEquals(INTERPOLATOR, other);
    assertEquals(INTERPOLATOR.hashCode(), other.hashCode());
    other = new SmileInterpolatorMixedLogNormal(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION);
    assertEquals(INTERPOLATOR, other);
    assertEquals(INTERPOLATOR.hashCode(), other.hashCode());
    other = new SmileInterpolatorMixedLogNormal(WeightingFunctionFactory.LINEAR_WEIGHTING_FUNCTION);
    assertFalse(INTERPOLATOR.equals(other));
  }
}
