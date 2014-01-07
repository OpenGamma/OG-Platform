/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.interpolation.LogLinearInterpolator1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class MultipleYieldCurveFinderDataBundleTest {
  private static final Currency CUR = Currency.AUD;
  private static final String CURVE_NAME1 = "Test1";
  private static final String CURVE_NAME2 = "Test2";
  private static final List<InstrumentDerivative> DERIVATIVES;
  private static final double[] TIMES1;
  private static final double[] TIMES2;
  private static final double[] PAR_RATES;
  private static final LinkedHashMap<String, double[]> NODES = new LinkedHashMap<>();
  private static final LinkedHashMap<String, Interpolator1D> INTERPOLATORS = new LinkedHashMap<>();
  private static final Interpolator1D INTERPOLATOR1 = new LinearInterpolator1D();
  private static final Interpolator1D INTERPOLATOR2 = new LogLinearInterpolator1D();
  private static final MultipleYieldCurveFinderDataBundle DATA;
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.EUR);

  static {
    final int n = 10;
    DERIVATIVES = new ArrayList<>();
    TIMES1 = new double[n];
    TIMES2 = new double[n];
    PAR_RATES = new double[2 * n];
    for (int i = 0; i < n; i++) {
      final double t1 = i / 10.;
      final double t2 = t1 + 0.005;
      DERIVATIVES.add(new Cash(CUR, 0, t1, 1, Math.random(), t1, CURVE_NAME1));
      DERIVATIVES.add(new Cash(CUR, 0, t2, 1, Math.random(), t2, CURVE_NAME2));
      TIMES1[i] = t1;
      TIMES2[i] = t2;
      PAR_RATES[i] = 0.05;
      PAR_RATES[n + i] = 0.05;
    }
    NODES.put(CURVE_NAME1, TIMES1);
    INTERPOLATORS.put(CURVE_NAME1, INTERPOLATOR1);
    NODES.put(CURVE_NAME2, TIMES2);
    INTERPOLATORS.put(CURVE_NAME2, INTERPOLATOR2);
    DATA = new MultipleYieldCurveFinderDataBundle(DERIVATIVES, PAR_RATES, null, NODES, INTERPOLATORS, false, FX_MATRIX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDerivatives() {
    new MultipleYieldCurveFinderDataBundle(null, PAR_RATES, null, NODES, INTERPOLATORS, false, FX_MATRIX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullParRates() {
    new MultipleYieldCurveFinderDataBundle(null, null, null, NODES, INTERPOLATORS, false, FX_MATRIX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNodes() {
    new MultipleYieldCurveFinderDataBundle(DERIVATIVES, PAR_RATES, null, null, INTERPOLATORS, false, FX_MATRIX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolators() {
    new MultipleYieldCurveFinderDataBundle(DERIVATIVES, PAR_RATES, null, NODES, null, false, FX_MATRIX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthParRates() {
    new MultipleYieldCurveFinderDataBundle(DERIVATIVES, new double[] {0.01}, null, NODES, INTERPOLATORS, false, FX_MATRIX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNameClash() {
    final YieldCurveBundle bundle = new YieldCurveBundle();
    final YieldAndDiscountCurve curve = YieldCurve.from(ConstantDoublesCurve.from(0.05));
    bundle.setCurve(CURVE_NAME1, curve);
    new MultipleYieldCurveFinderDataBundle(DERIVATIVES, PAR_RATES, bundle, NODES, INTERPOLATORS, false, FX_MATRIX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyDerivatives() {
    new MultipleYieldCurveFinderDataBundle(new ArrayList<InstrumentDerivative>(), PAR_RATES, null, NODES, INTERPOLATORS, false, FX_MATRIX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCurveAlreadyPresent() {
    new MultipleYieldCurveFinderDataBundle(DERIVATIVES, PAR_RATES, new YieldCurveBundle(Collections.<String, YieldAndDiscountCurve> singletonMap(CURVE_NAME1,
        YieldCurve.from(ConstantDoublesCurve.from(2.)))), NODES, INTERPOLATORS, false, FX_MATRIX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongSize1() {
    final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<>();
    interpolators.put(CURVE_NAME2, INTERPOLATOR1);
    new MultipleYieldCurveFinderDataBundle(DERIVATIVES, PAR_RATES, null, NODES, interpolators, false, FX_MATRIX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongNames1() {
    final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<>();
    interpolators.put(CURVE_NAME2, INTERPOLATOR1);
    interpolators.put(CURVE_NAME1, INTERPOLATOR2);
    new MultipleYieldCurveFinderDataBundle(DERIVATIVES, PAR_RATES, null, NODES, interpolators, false, FX_MATRIX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValue1() {
    final LinkedHashMap<String, double[]> nodes = new LinkedHashMap<>();
    nodes.put(CURVE_NAME1, TIMES1);
    nodes.put(CURVE_NAME2, null);
    new MultipleYieldCurveFinderDataBundle(DERIVATIVES, PAR_RATES, null, nodes, INTERPOLATORS, false, FX_MATRIX);

  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValue2() {
    final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<>();
    interpolators.put(CURVE_NAME2, INTERPOLATOR1);
    interpolators.put(CURVE_NAME1, null);
    new MultipleYieldCurveFinderDataBundle(DERIVATIVES, PAR_RATES, null, NODES, interpolators, false, FX_MATRIX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName1() {
    DATA.getCurveNodePointsForCurve(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName2() {
    DATA.getInterpolatorForCurve(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongName1() {
    DATA.getCurveNodePointsForCurve("X");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongName2() {
    DATA.getInterpolatorForCurve("Y");
  }

  @Test
  public void testGetters() {
    final List<String> names = new ArrayList<>();
    for (final String name : NODES.keySet()) {
      names.add(name);
    }
    assertEquals(DATA.getCurveNames(), names);
    assertArrayEquals(DATA.getCurveNodePointsForCurve(CURVE_NAME1), TIMES1, 1E-10);
    assertArrayEquals(DATA.getCurveNodePointsForCurve(CURVE_NAME2), TIMES2, 1E-10);
    for (int i = 0; i < DERIVATIVES.size(); i++) {
      assertEquals(DERIVATIVES.get(i), DATA.getDerivative(i));
    }
    assertEquals(DATA.getDerivatives(), DERIVATIVES);
    assertEquals(DATA.getInterpolatorForCurve(CURVE_NAME1), INTERPOLATOR1);
    assertEquals(DATA.getInterpolatorForCurve(CURVE_NAME2), INTERPOLATOR2);
    assertNull(DATA.getKnownCurves());
    assertEquals(DATA.getTotalNodes(), TIMES1.length * 2);
    assertEquals(DATA.getUnknownCurveInterpolators(), INTERPOLATORS);
    assertEquals(DATA.getUnknownCurveNodePoints(), NODES);
  }

  @Test
  public void testEqualsAndHashCode() {
    MultipleYieldCurveFinderDataBundle other = new MultipleYieldCurveFinderDataBundle(DERIVATIVES, PAR_RATES, null, NODES, INTERPOLATORS, false, FX_MATRIX);
    assertEquals(DATA, other);
    assertEquals(DATA.hashCode(), other.hashCode());
    final List<InstrumentDerivative> derivatives = new ArrayList<>(DERIVATIVES);
    derivatives.set(0, new Cash(CUR, 0, 1000, 1, 0.05, 1000, CURVE_NAME1));
    other = new MultipleYieldCurveFinderDataBundle(derivatives, PAR_RATES, null, NODES, INTERPOLATORS, false, FX_MATRIX);
    assertFalse(other.equals(DATA));
    other = new MultipleYieldCurveFinderDataBundle(derivatives, new double[PAR_RATES.length], null, NODES, INTERPOLATORS, false, FX_MATRIX);
    assertFalse(other.equals(DATA));
    final YieldCurveBundle knownCurves = new YieldCurveBundle();
    other = new MultipleYieldCurveFinderDataBundle(DERIVATIVES, PAR_RATES, knownCurves, NODES, INTERPOLATORS, false, FX_MATRIX);
    assertFalse(other.equals(DATA));
    final LinkedHashMap<String, double[]> nodes = new LinkedHashMap<>();
    nodes.put(CURVE_NAME1, TIMES1);
    nodes.put(CURVE_NAME2, TIMES1);
    other = new MultipleYieldCurveFinderDataBundle(DERIVATIVES, PAR_RATES, null, nodes, INTERPOLATORS, false, FX_MATRIX);
    assertFalse(other.equals(DATA));
    final LinkedHashMap<String, Interpolator1D> interpolators = new LinkedHashMap<>();
    interpolators.put(CURVE_NAME1, INTERPOLATOR1);
    interpolators.put(CURVE_NAME2, INTERPOLATOR1);
    other = new MultipleYieldCurveFinderDataBundle(DERIVATIVES, PAR_RATES, null, NODES, interpolators, false, FX_MATRIX);
    assertFalse(other.equals(DATA));
    other = new MultipleYieldCurveFinderDataBundle(DERIVATIVES, PAR_RATES, null, NODES, INTERPOLATORS, true, FX_MATRIX);
    assertFalse(other.equals(DATA));
  }
}
