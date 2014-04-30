/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.generator;

import static org.testng.AssertJUnit.assertEquals;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveAddYield;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolatedNode;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the curve generator related to curves based on several underlying curve for which the rates (continously compounded) are added.
 */
@Test(groups = TestGroup.UNIT)
public class GeneratorCurveAddYieldTest {

  private static final String CURVE_NAME = "EUR Curve";
  private static final String CURVE_NAME_0 = CURVE_NAME + "-0";
  private static final String CURVE_NAME_1 = CURVE_NAME + "-1";

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D INTERPOLATOR_DQ = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double[] NODES_0 = new double[] {0.01, 0.50, 1.00, 2.00, 5.05, 10.0};
  private static final double[] RATE_0 = new double[] {0.02, 0.02, 0.03, 0.01, 0.02, 0.01};
  private static final double[] NODES_1 = new double[] {5.05, 10.0, 20.0, 30.0};
  private static final double[] RATE_1 = new double[] {0.0010, 0.0020, 0.0050, 0.0000};
  private static final double[] RATE = ArrayUtils.addAll(RATE_0, RATE_1);

  private static final GeneratorCurveYieldInterpolatedNode GENERATOR_YIELD_INTERPOLATED_NODE_1 = new GeneratorCurveYieldInterpolatedNode(NODES_0, INTERPOLATOR_LINEAR);
  private static final GeneratorCurveYieldInterpolatedNode GENERATOR_YIELD_INTERPOLATED_NODE_2 = new GeneratorCurveYieldInterpolatedNode(NODES_1, INTERPOLATOR_DQ);

  private static final GeneratorCurveAddYield GENERATOR_ADD = new GeneratorCurveAddYield(new GeneratorYDCurve[] {GENERATOR_YIELD_INTERPOLATED_NODE_1, GENERATOR_YIELD_INTERPOLATED_NODE_2}, false);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void constructorNullGenerators() {
    new GeneratorCurveAddYield(null, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void generateCurveNullName() {
    GENERATOR_ADD.generateCurve(null, new double[2]);
  }

  @Test
  public void getNumberOfParameter() {
    final int nbParamAdd = GENERATOR_ADD.getNumberOfParameter();
    assertEquals("GeneratorCurveAddYield: getNumberOfParameter()", GENERATOR_YIELD_INTERPOLATED_NODE_1.getNumberOfParameter() + GENERATOR_YIELD_INTERPOLATED_NODE_2.getNumberOfParameter(), nbParamAdd);
  }

  @Test
  public void generateCurve() {
    final YieldAndDiscountCurve curveAdd = GENERATOR_ADD.generateCurve(CURVE_NAME, RATE);
    final YieldAndDiscountCurve curve1 = GENERATOR_YIELD_INTERPOLATED_NODE_1.generateCurve(CURVE_NAME_0, RATE_0);
    final YieldAndDiscountCurve curve2 = GENERATOR_YIELD_INTERPOLATED_NODE_2.generateCurve(CURVE_NAME_1, RATE_1);
    final YieldAndDiscountCurve curveExpected = new YieldAndDiscountAddZeroSpreadCurve(CURVE_NAME, false, new YieldAndDiscountCurve[] {curve1, curve2});
    assertEquals("GeneratorCurveAddYield: generateCurve()", curveExpected, curveAdd);
  }

}
