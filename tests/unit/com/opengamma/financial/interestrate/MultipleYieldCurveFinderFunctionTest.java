/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.temp.FixedNodeInterpolator1D;
import com.opengamma.math.interpolation.temp.InterpolationResult;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class MultipleYieldCurveFinderFunctionTest {

  private static final String CURVE_NAME = "Test";
  private static final List<InterestRateDerivative> DERIVATIVES;
  private static final double[] SIMPLE_RATES;
  private static final double[] CONTINUOUS_RATES;
  private static final double[] NODES;

  private static final InterestRateDerivativeVisitor<Double> CALCULATOR = ParRateDifferenceCalculator.getInstance();
  private static final InterestRateDerivativeVisitor<Map<String, List<Pair<Double, Double>>>> SENSITIVITY_CALCULATOR = ParRateCurveSensitivityCalculator.getInstance();

  private static final Interpolator1D<Interpolator1DDataBundle, InterpolationResult> INTERPOLATOR = new LinearInterpolator1D();
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> FINDER;
  private static final LinkedHashMap<String, FixedNodeInterpolator1D> UNKNOWN_CURVES;
  static {
    final int n = 10;
    DERIVATIVES = new ArrayList<InterestRateDerivative>();
    SIMPLE_RATES = new double[n];
    CONTINUOUS_RATES = new double[n];
    NODES = new double[n];
    double t;
    for (int i = 0; i < n; i++) {
      t = i / 10.;
      SIMPLE_RATES[i] = Math.random() * 0.05;
      DERIVATIVES.add(new Cash(t, SIMPLE_RATES[i], CURVE_NAME));
      CONTINUOUS_RATES[i] = (t == 0 ? SIMPLE_RATES[i] : Math.log(1 + SIMPLE_RATES[i] * t) / t);
      NODES[i] = t;
    }

    UNKNOWN_CURVES = new LinkedHashMap<String, FixedNodeInterpolator1D>();
    final FixedNodeInterpolator1D fnInterpolator = new FixedNodeInterpolator1D(NODES, INTERPOLATOR);
    UNKNOWN_CURVES.put(CURVE_NAME, fnInterpolator);
    FINDER = new MultipleYieldCurveFinderFunction(DERIVATIVES, UNKNOWN_CURVES, null, CALCULATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDerivatives() {
    new MultipleYieldCurveFinderFunction(null, UNKNOWN_CURVES, null, CALCULATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFixedNodeInterpolator() {
    new MultipleYieldCurveFinderFunction(DERIVATIVES, null, null, CALCULATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalculator() {
    new MultipleYieldCurveFinderFunction(DERIVATIVES, UNKNOWN_CURVES, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNameClash() {
    final YieldCurveBundle bundle = new YieldCurveBundle();
    final YieldAndDiscountCurve curve = new ConstantYieldCurve(0.05);
    bundle.setCurve(CURVE_NAME, curve);
    new MultipleYieldCurveFinderFunction(DERIVATIVES, UNKNOWN_CURVES, bundle, CALCULATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyDerivatives() {
    new MultipleYieldCurveFinderFunction(new ArrayList<InterestRateDerivative>(), UNKNOWN_CURVES, null, CALCULATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullVector() {
    FINDER.evaluate((DoubleMatrix1D) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMismatchingVector() {
    FINDER.evaluate(new DoubleMatrix1D(new double[] {1, 2, 3, 4, 5, 6, 7, 8}));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongNodeNumber() {
    final List<InterestRateDerivative> list = new ArrayList<InterestRateDerivative>();
    list.add(new Cash(1, 0.01, CURVE_NAME));
    list.add(new Cash(0.5, 0.01, CURVE_NAME));
    new MultipleYieldCurveFinderFunction(list, UNKNOWN_CURVES, null, CALCULATOR);
  }

  @Test
  public void test() {
    final DoubleMatrix1D results = FINDER.evaluate(new DoubleMatrix1D(CONTINUOUS_RATES));
    for (final double r : results.getData()) {
      assertEquals(0.0, r, 1e-14);
    }
  }
}
