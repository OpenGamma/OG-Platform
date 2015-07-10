/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.inflation.generator;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveMultiplyFixedCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;

/**
 * Tests related to the generation of price index curves as the multiplication of two curves.
 * One curve is fixed (i.e. with sensitivity to its parameters).
 */
public class GeneratorPriceIndexCurveMultiplyFixedCurveTest {
  
  private static final Interpolator1D INTERPOLATOR_LINEAR = 
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, 
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double ANCHOR_NODE = 0.5;
  private static final double ANCHOR_VALUE = 1.0;
  private static final double[] NODES = {1.0, 2.0};
  private static final int NB_NODES = NODES.length;
  
  private static final GeneratorPriceIndexCurveInterpolatedAnchorNode GENERATOR_1 =
      new GeneratorPriceIndexCurveInterpolatedAnchorNode(NODES, INTERPOLATOR_LINEAR, ANCHOR_NODE, ANCHOR_VALUE);
  
  private static final DoublesCurve FIXED_CURVE = new ConstantDoublesCurve(0.01, "Fixed");
  
  private static final GeneratorPriceIndexCurveMultiplyFixedCurve GENERATOR_MULT = 
      new GeneratorPriceIndexCurveMultiplyFixedCurve(GENERATOR_1, FIXED_CURVE);


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullGenerator() {
    new GeneratorPriceIndexCurveMultiplyFixedCurve(null, FIXED_CURVE);
  }


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurve() {
    new GeneratorPriceIndexCurveMultiplyFixedCurve(GENERATOR_1, null);
  }
  
  @Test
  public void getter() {
    assertEquals("GeneratorPriceIndexCurveMultiplyFixedCurve", GENERATOR_MULT.getNumberOfParameter(), GENERATOR_1.getNumberOfParameter());
  }

  @Test
  public void generateCurve() {
    String name = "CRV";
    double[] values = new double[NB_NODES];
    PriceIndexCurve curveGenerated = GENERATOR_MULT.generateCurve(name, values);
    assertTrue("GeneratorPriceIndexCurveMultiplyFixedCurve", 
        curveGenerated instanceof PriceIndexCurveMultiplyFixedCurve);
    PriceIndexCurveMultiplyFixedCurve curveMult = (PriceIndexCurveMultiplyFixedCurve) curveGenerated;
    assertEquals("GeneratorPriceIndexCurveMultiplyFixedCurve", curveMult.getName(), name);
    assertEquals("GeneratorPriceIndexCurveMultiplyFixedCurve", curveMult.getNumberOfParameters(), NB_NODES);
  }
  
}
