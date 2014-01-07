/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.generator;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurveConstant;
import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurveInterpolatedNode;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the different inflation curve generators types.
 */
@Test(groups = TestGroup.UNIT)
public class GeneratorInflationCurveTest {

  private static final String CURVE_NAME_1 = "EU CPI XT";

  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double[] NODES = new double[] {0.01, 0.50, 1.00, 2.00, 5.05, 10.0 };
  private static final double[] CPI = new double[] {100, 102, 103, 102.5, 106, 101 };
  private static final GeneratorPriceIndexCurveInterpolatedNode GENERATOR_PRICE_INDEX_INTERPOLATED_NODE = new GeneratorPriceIndexCurveInterpolatedNode(NODES, LINEAR_FLAT);

  private static final double CST = 100;
  private static final GeneratorPriceIndexCurveConstant GENERATOR_PRICE_INDEX_CONSTANT = new GeneratorPriceIndexCurveConstant();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullYieldNodes() {
    new GeneratorPriceIndexCurveInterpolatedNode(null, LINEAR_FLAT);
  }

  @Test
  public void getterYieldInterpolated() {
    assertEquals(NODES.length, GENERATOR_PRICE_INDEX_INTERPOLATED_NODE.getNumberOfParameter());
  }

  @Test
  public void getterYieldInterpolatedZero() {
    assertEquals(NODES.length, GENERATOR_PRICE_INDEX_INTERPOLATED_NODE.getNumberOfParameter());
  }

  @Test
  public void generateCurveYieldInterpolated() {
    final PriceIndexCurve curveGenerated = GENERATOR_PRICE_INDEX_INTERPOLATED_NODE.generateCurve(CURVE_NAME_1, CPI);
    final PriceIndexCurve curveExpected = new PriceIndexCurve(new InterpolatedDoublesCurve(NODES, CPI, LINEAR_FLAT, true, CURVE_NAME_1));
    assertEquals("GeneratorPriceIndexCurveInterpolatedNode: generate curve", curveExpected, curveGenerated);
  }

  @Test
  public void generateCurveYieldConstant() {
    final PriceIndexCurve curveGenerated = GENERATOR_PRICE_INDEX_CONSTANT.generateCurve(CURVE_NAME_1, new double[] {CST });
    final PriceIndexCurve curveExpected = new PriceIndexCurve(new ConstantDoublesCurve(CST, CURVE_NAME_1));
    assertEquals("GeneratorPriceIndexCurveConstant: generate curve", curveExpected, curveGenerated);
  }

}
