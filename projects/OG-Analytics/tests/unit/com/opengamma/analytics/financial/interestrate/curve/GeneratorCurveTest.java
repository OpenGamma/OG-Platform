/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;

public class GeneratorCurveTest {

  private static final String CURVE_NAME_1 = "EUR Discounting";
  private static final String CURVE_NAME_2 = "EUR Discounting-Spread";

  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double[] NODES = new double[] {0.01, 0.50, 1.00, 2.00, 5.05, 10.0};
  private static final double[] YIELD = new double[] {0.02, 0.02, 0.03, 0.01, 0.02, 0.01};
  private static final GeneratorCurveYieldInterpolated GENERATOR_YIELD_INTERPOLATED = new GeneratorCurveYieldInterpolated(NODES, LINEAR_FLAT);

  private static final double CST = 0.0050;
  private static final GeneratorCurveYieldConstant GENERATOR_YIELD_CONSTANT = new GeneratorCurveYieldConstant();

  private static final GeneratorCurveAddYield GENERATOR_SPREAD = new GeneratorCurveAddYield(new GeneratorCurve[] {GENERATOR_YIELD_INTERPOLATED, GENERATOR_YIELD_CONSTANT}, false);

  private static final GeneratorCurveAddYieldExisiting GENERATOR_EXISTING = new GeneratorCurveAddYieldExisiting(GENERATOR_YIELD_CONSTANT, true, CURVE_NAME_1);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullYieldNodes() {
    new GeneratorCurveYieldInterpolated(null, LINEAR_FLAT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullYieldInterpolator() {
    new GeneratorCurveYieldInterpolated(NODES, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSpreadGen() {
    new GeneratorCurveAddYield(null, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSpreadExGen() {
    new GeneratorCurveAddYieldExisiting(null, true, CURVE_NAME_1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSpreadExName() {
    new GeneratorCurveAddYieldExisiting(GENERATOR_YIELD_CONSTANT, true, null);
  }

  @Test
  public void getterYieldInterpolated() {
    assertEquals(NODES.length, GENERATOR_YIELD_INTERPOLATED.getNumberOfParameter());
  }

  @Test
  public void generateCurveYieldInterpolated() {
    YieldAndDiscountCurve curveGenerated = GENERATOR_YIELD_INTERPOLATED.generateCurve("EUR Discounting", YIELD);
    YieldAndDiscountCurve curveExpected = new YieldCurve(CURVE_NAME_1, new InterpolatedDoublesCurve(NODES, YIELD, LINEAR_FLAT, true, CURVE_NAME_1));
    assertEquals("GeneratorCurveYieldInterpolated: generate curve", curveExpected, curveGenerated);
  }

  @Test
  public void generateCurveYieldConstant() {
    YieldAndDiscountCurve curveGenerated = GENERATOR_YIELD_CONSTANT.generateCurve("EUR Discounting", new double[] {CST});
    YieldAndDiscountCurve curveExpected = new YieldCurve(CURVE_NAME_1, new ConstantDoublesCurve(CST, CURVE_NAME_1));
    assertEquals("GeneratorCurveYieldConstant: generate curve", curveExpected, curveGenerated);
  }

  @Test
  public void generateCurveYieldSpread() {
    double[] x = new double[YIELD.length + 1];
    System.arraycopy(YIELD, 0, x, 0, YIELD.length);
    x[YIELD.length] = CST;
    YieldAndDiscountCurve curveGenerated = GENERATOR_SPREAD.generateCurve("EUR Discounting", x);
    YieldAndDiscountCurve curveExpected0 = new YieldCurve(CURVE_NAME_1 + "-0", new InterpolatedDoublesCurve(NODES, YIELD, LINEAR_FLAT, true, CURVE_NAME_1 + "-0"));
    YieldAndDiscountCurve curveExpected1 = new YieldCurve(CURVE_NAME_1 + "-1", new ConstantDoublesCurve(CST, CURVE_NAME_1 + "-1"));
    YieldAndDiscountCurve curveExpected = new YieldAndDiscountAddZeroSpreadCurve(CURVE_NAME_1, false, curveExpected0, curveExpected1);
    assertEquals("GeneratorCurveYieldConstant: generate curve", curveExpected, curveGenerated);
  }

  @Test
  /**
   * The the curve generator with yield spread with two levels of spread.
   */
  public void generateCurveYieldSpread2() {
    GeneratorCurveAddYield generatorSpread1 = new GeneratorCurveAddYield(new GeneratorCurve[] {GENERATOR_YIELD_INTERPOLATED, GENERATOR_YIELD_CONSTANT}, false);
    GeneratorCurveAddYield generatorSpread2 = new GeneratorCurveAddYield(new GeneratorCurve[] {generatorSpread1, GENERATOR_YIELD_CONSTANT}, false);
    double[] x = new double[YIELD.length + 2];
    System.arraycopy(YIELD, 0, x, 0, YIELD.length);
    x[YIELD.length] = CST;
    x[YIELD.length + 1] = CST;
    YieldAndDiscountCurve curveGenerated = generatorSpread2.generateCurve("EUR Discounting", x);
    YieldAndDiscountCurve curveExpected00 = new YieldCurve(CURVE_NAME_1 + "-0-0", new InterpolatedDoublesCurve(NODES, YIELD, LINEAR_FLAT, true, CURVE_NAME_1 + "-0-0"));
    YieldAndDiscountCurve curveExpected01 = new YieldCurve(CURVE_NAME_1 + "-0-1", new ConstantDoublesCurve(CST, CURVE_NAME_1 + "-0-1"));
    YieldAndDiscountCurve curveExpected0 = new YieldAndDiscountAddZeroSpreadCurve(CURVE_NAME_1 + "-0", false, curveExpected00, curveExpected01);
    YieldAndDiscountCurve curveExpected1 = new YieldCurve(CURVE_NAME_1 + "-1", new ConstantDoublesCurve(CST, CURVE_NAME_1 + "-1"));
    YieldAndDiscountCurve curveExpected = new YieldAndDiscountAddZeroSpreadCurve(CURVE_NAME_1, false, curveExpected0, curveExpected1);
    assertEquals("GeneratorCurveYieldConstant: generate curve", curveExpected, curveGenerated);
  }

  @Test
  public void generateCurveYieldSpreadExisting() {
    YieldAndDiscountCurve curveExisting = new YieldCurve(CURVE_NAME_1, new InterpolatedDoublesCurve(NODES, YIELD, LINEAR_FLAT, true, CURVE_NAME_1));
    YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(CURVE_NAME_1, curveExisting);
    YieldAndDiscountCurve curveGenerated = GENERATOR_EXISTING.generateCurve(CURVE_NAME_2, bundle, new double[] {CST});
    YieldAndDiscountCurve curveExpected0 = new YieldCurve(CURVE_NAME_2 + "-0", new ConstantDoublesCurve(CST, CURVE_NAME_2 + "-0"));
    YieldAndDiscountCurve curveExpected = new YieldAndDiscountAddZeroSpreadCurve(CURVE_NAME_2, true, curveExisting, curveExpected0);
    assertEquals("GeneratorCurveYieldConstant: generate curve", curveExpected, curveGenerated);
  }

}
