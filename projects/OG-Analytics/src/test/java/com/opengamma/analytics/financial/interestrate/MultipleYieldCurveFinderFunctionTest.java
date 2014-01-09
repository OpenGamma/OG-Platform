/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class MultipleYieldCurveFinderFunctionTest {
  private static final Currency CUR = Currency.AUD;
  private static final String CURVE_NAME = "Test";
  private static final List<InstrumentDerivative> DERIVATIVES;
  private static final double[] SIMPLE_RATES;
  private static final double[] CONTINUOUS_RATES;
  private static final double[] TIMES;

  private static final InstrumentDerivativeVisitor<YieldCurveBundle, Double> CALCULATOR = ParRateCalculator.getInstance();

  private static final Interpolator1D INTERPOLATOR = new LinearInterpolator1D();
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> FINDER;
  private static final LinkedHashMap<String, double[]> NODES = new LinkedHashMap<>();
  private static final LinkedHashMap<String, Interpolator1D> INTERPOLATORS = new LinkedHashMap<>();
  private static final MultipleYieldCurveFinderDataBundle DATA;
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.EUR);

  static {
    final int n = 10;
    DERIVATIVES = new ArrayList<>();
    SIMPLE_RATES = new double[n];
    CONTINUOUS_RATES = new double[n];
    TIMES = new double[n];
    double t;
    for (int i = 0; i < n; i++) {
      t = i / 10.;
      SIMPLE_RATES[i] = Math.random() * 0.05;
      DERIVATIVES.add(new Cash(CUR, 0, t, 1, SIMPLE_RATES[i], t, CURVE_NAME));
      CONTINUOUS_RATES[i] = (t == 0 ? SIMPLE_RATES[i] : Math.log(1 + SIMPLE_RATES[i] * t) / t);
      TIMES[i] = t;
    }
    NODES.put(CURVE_NAME, TIMES);
    INTERPOLATORS.put(CURVE_NAME, INTERPOLATOR);
    DATA = new MultipleYieldCurveFinderDataBundle(DERIVATIVES, SIMPLE_RATES, null, NODES, INTERPOLATORS, false, FX_MATRIX);
    FINDER = new MultipleYieldCurveFinderFunction(DATA, CALCULATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    new MultipleYieldCurveFinderFunction(null, CALCULATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator() {
    new MultipleYieldCurveFinderFunction(DATA, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVector() {
    FINDER.evaluate((DoubleMatrix1D) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMismatchingVector() {
    FINDER.evaluate(new DoubleMatrix1D(new double[] {1, 2, 3, 4, 5, 6, 7, 8}));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongNodeNumber() {
    final List<InstrumentDerivative> list = new ArrayList<>();
    list.add(new Cash(CUR, 0, 1, 1, 0.01, 1, CURVE_NAME));
    list.add(new Cash(CUR, 0, 0.5, 1, 0.01, 0.5, CURVE_NAME));
    new MultipleYieldCurveFinderFunction(new MultipleYieldCurveFinderDataBundle(list, new double[list.size()], null, NODES, INTERPOLATORS, false, FX_MATRIX), CALCULATOR);
  }

  @Test
  public void test() {
    final DoubleMatrix1D results = FINDER.evaluate(new DoubleMatrix1D(CONTINUOUS_RATES));
    for (final double r : results.getData()) {
      assertEquals(0.0, r, 1e-14);
    }
  }
}
