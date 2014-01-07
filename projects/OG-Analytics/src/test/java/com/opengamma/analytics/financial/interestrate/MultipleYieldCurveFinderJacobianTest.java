/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class MultipleYieldCurveFinderJacobianTest {
  private static final Currency CCY = Currency.AUD;
  private static final String FUNDING_CURVE_NAME = "Some funding curve";
  private static final String FORWARD_CURVE_NAME = "Some forward curve";
  private static final List<InstrumentDerivative> CASH;
  private static final List<InstrumentDerivative> FRA;
  private static final List<InstrumentDerivative> MIXED_INSTRUMENT;
  private static final double[] FORWARD_NODES;
  private static final double[] FUNDING_NODES;
  private static final Interpolator1D EXTRAPOLATOR;
  private static final DoubleMatrix1D XN;
  private static final DoubleMatrix1D XM;
  private static final DoubleMatrix1D XNM;
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> CASH_ONLY;
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> FRA_ONLY;
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> MIXED;
  private static final LinkedHashMap<String, double[]> CASH_NODES;
  private static final LinkedHashMap<String, double[]> FRA_NODES;
  private static final LinkedHashMap<String, double[]> MIXED_NODES;
  private static final LinkedHashMap<String, Interpolator1D> CASH_INTERPOLATORS;
  private static final LinkedHashMap<String, Interpolator1D> FRA_INTERPOLATORS;
  private static final LinkedHashMap<String, Interpolator1D> MIXED_INTERPOLATORS;
  private static final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> SENSITIVITY_CALCULATOR = ParRateCurveSensitivityCalculator.getInstance();
  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.EUR);

  private static final int N = 10;
  private static final int M = 5;

  static {
    final IborIndex index = new IborIndex(CCY, Period.ofMonths(1), 0, DayCounts.ACT_365,
        BusinessDayConventions.FOLLOWING, true);
    CASH = new ArrayList<>();
    FRA = new ArrayList<>();
    MIXED_INSTRUMENT = new ArrayList<>();
    FORWARD_NODES = new double[N];
    FUNDING_NODES = new double[M];
    final double[] dataN = new double[N];
    final double[] dataM = new double[M];
    final double[] dataNpM = new double[N + M];
    for (int i = 0; i < N; i++) {
      final InstrumentDerivative ird = new ForwardRateAgreement(CCY, i, FUNDING_CURVE_NAME, 0.5, 1, index, i, i, i + 0.5, 0.5, 0, FORWARD_CURVE_NAME);
      FRA.add(ird);
      MIXED_INSTRUMENT.add(ird);
      FORWARD_NODES[i] = i + 1;
      dataN[i] = Math.random() / 10;
      dataNpM[i] = dataN[i];
    }

    for (int i = 0; i < M; i++) {
      final InstrumentDerivative ird = new Cash(CCY, 0, i, 1, 0.0, i, FUNDING_CURVE_NAME);
      CASH.add(ird);
      MIXED_INSTRUMENT.add(ird);
      FUNDING_NODES[i] = i;
      dataM[i] = Math.random() / 10;
      dataNpM[i + N] = dataM[i];
    }
    EXTRAPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

    CASH_NODES = new LinkedHashMap<>();
    CASH_NODES.put(FUNDING_CURVE_NAME, FUNDING_NODES);
    FRA_NODES = new LinkedHashMap<>();
    FRA_NODES.put(FORWARD_CURVE_NAME, FORWARD_NODES);
    MIXED_NODES = new LinkedHashMap<>();
    MIXED_NODES.put(FORWARD_CURVE_NAME, FORWARD_NODES);
    MIXED_NODES.put(FUNDING_CURVE_NAME, FUNDING_NODES);
    CASH_INTERPOLATORS = new LinkedHashMap<>();
    CASH_INTERPOLATORS.put(FUNDING_CURVE_NAME, EXTRAPOLATOR);
    FRA_INTERPOLATORS = new LinkedHashMap<>();
    FRA_INTERPOLATORS.put(FORWARD_CURVE_NAME, EXTRAPOLATOR);
    MIXED_INTERPOLATORS = new LinkedHashMap<>();
    MIXED_INTERPOLATORS.put(FORWARD_CURVE_NAME, EXTRAPOLATOR);
    MIXED_INTERPOLATORS.put(FUNDING_CURVE_NAME, EXTRAPOLATOR);

    XM = new DoubleMatrix1D(dataM);
    XN = new DoubleMatrix1D(dataN);
    XNM = new DoubleMatrix1D(dataNpM);
    CASH_ONLY = new MultipleYieldCurveFinderJacobian(new MultipleYieldCurveFinderDataBundle(CASH, new double[CASH.size()], null, CASH_NODES, CASH_INTERPOLATORS, false, FX_MATRIX), SENSITIVITY_CALCULATOR);
    FRA_ONLY = new MultipleYieldCurveFinderJacobian(new MultipleYieldCurveFinderDataBundle(FRA, new double[FRA.size()], null, FRA_NODES, FRA_INTERPOLATORS, false, FX_MATRIX), SENSITIVITY_CALCULATOR);
    MIXED = new MultipleYieldCurveFinderJacobian(new MultipleYieldCurveFinderDataBundle(MIXED_INSTRUMENT, new double[MIXED_INSTRUMENT.size()], null, MIXED_NODES, MIXED_INTERPOLATORS, false, FX_MATRIX),
        SENSITIVITY_CALCULATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator() {
    new MultipleYieldCurveFinderJacobian(new MultipleYieldCurveFinderDataBundle(MIXED_INSTRUMENT, new double[MIXED_INSTRUMENT.size()], null, MIXED_NODES, MIXED_INTERPOLATORS, false, FX_MATRIX), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    new MultipleYieldCurveFinderJacobian(null, SENSITIVITY_CALCULATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVector() {
    CASH_ONLY.evaluate((DoubleMatrix1D) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongNumberOfElements() {
    CASH_ONLY.evaluate(XN);
  }

  @Test
  public void testCashOnly() {
    final DoubleMatrix2D jacobian = CASH_ONLY.evaluate(XM);
    assertEquals(M, jacobian.getNumberOfRows());
    assertEquals(M, jacobian.getNumberOfColumns());
    for (int i = 0; i < M; i++) {
      for (int j = 0; j < M; j++) {
        if (i == j) {
          assertEquals(Math.exp(XM.getEntry(i) * i), jacobian.getEntry(i, i), 1e-8);
        } else {
          assertEquals(0.0, jacobian.getEntry(i, j), 0);
        }
      }
    }
  }

  @Test
  public void testFRAOnly() {
    final DoubleMatrix2D jacobian = FRA_ONLY.evaluate(XN);
    assertEquals(N, jacobian.getNumberOfRows());
    assertEquals(N, jacobian.getNumberOfColumns());
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < N; j++) {
        if (i == j) {
          assertTrue(jacobian.getEntry(i, j) > 0.0);
        } else if (i == (j + 1)) {
          assertTrue(jacobian.getEntry(i, j) < 0.0);
        } else {
          assertEquals(0.0, jacobian.getEntry(i, j), 0.0);
        }

      }
    }
  }

  @Test
  public void testMixed() {
    final DoubleMatrix2D jacobian = MIXED.evaluate(XNM);
    assertEquals(N + M, jacobian.getNumberOfRows());
    assertEquals(N + M, jacobian.getNumberOfColumns());
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < N + M; j++) {
        if (i == j) {
          assertTrue(jacobian.getEntry(i, j) > 0.0);
        } else if (i == (j + 1)) {
          assertTrue(jacobian.getEntry(i, j) < 0.0);
        } else {
          assertEquals(0.0, jacobian.getEntry(i, j), 0.0);
        }
      }
    }
    for (int i = N; i < N + M; i++) {
      for (int j = 0; j < N + M; j++) {
        if (i == j) {
          assertEquals(Math.exp(XNM.getEntry(i) * (i - N)), jacobian.getEntry(i, i), 1e-8);
        } else {
          assertEquals(0.0, jacobian.getEntry(i, j), 0.0);
        }
      }
    }
  }
}
