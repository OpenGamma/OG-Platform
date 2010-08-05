/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.temp.ExtrapolatorOld1D;
import com.opengamma.math.interpolation.temp.ExtrapolatorMethod;
import com.opengamma.math.interpolation.temp.FixedNodeInterpolator1D;
import com.opengamma.math.interpolation.temp.FlatExtrapolatorWithSensitivities;
import com.opengamma.math.interpolation.temp.InterpolationResultWithSensitivities;
import com.opengamma.math.interpolation.temp.Interpolator1DWithSensitivities;
import com.opengamma.math.interpolation.temp.LinearInterpolator1DWithSensitivities;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class MultipleYieldCurveFinderJacobianTest {

  private static final String FUNDING_CURVE_NAME = "Some funding curve";
  private static final String FORWARD_CURVE_NAME = "Some forward curve";
  private static final List<InterestRateDerivative> CASH;
  private static final List<InterestRateDerivative> FRA;
  private static final List<InterestRateDerivative> MIXED_INSTRUMENT;
  private static final double[] FORWARD_NODES;
  private static final double[] FUNDING_NODES;
  private static final ExtrapolatorOld1D<Interpolator1DDataBundle, InterpolationResultWithSensitivities> EXTRAPOLATOR;
  private static final DoubleMatrix1D XN;
  private static final DoubleMatrix1D XM;
  private static final DoubleMatrix1D XNM;
  private static final JacobianCalculator CASH_ONLY;
  private static final JacobianCalculator FRA_ONLY;
  private static final JacobianCalculator MIXED;
  private static final LinkedHashMap<String, FixedNodeInterpolator1D> CASH_CURVES;
  private static final LinkedHashMap<String, FixedNodeInterpolator1D> FRA_CURVES;
  private static final LinkedHashMap<String, FixedNodeInterpolator1D> MIXED_CURVES;

  private static final InterestRateDerivativeVisitor<Map<String, List<Pair<Double, Double>>>> SENSITIVITY_CALCULATOR = ParRateCurveSensitivityCalculator.getInstance();

  private static final int N = 10;
  private static final int M = 5;

  static {
    CASH = new ArrayList<InterestRateDerivative>();
    FRA = new ArrayList<InterestRateDerivative>();
    MIXED_INSTRUMENT = new ArrayList<InterestRateDerivative>();
    FORWARD_NODES = new double[N];
    FUNDING_NODES = new double[M];
    double[] dataN = new double[N];
    double[] dataM = new double[M];
    final double[] dataNpM = new double[N + M];
    for (int i = 0; i < N; i++) {
      InterestRateDerivative ird = new ForwardRateAgreement(i, i + 0.5, 0.0, FUNDING_CURVE_NAME, FORWARD_CURVE_NAME);
      FRA.add(ird);
      MIXED_INSTRUMENT.add(ird);
      FORWARD_NODES[i] = i + 1;
      dataN[i] = Math.random() / 10;
      dataNpM[i] = dataN[i];
    }

    for (int i = 0; i < M; i++) {
      InterestRateDerivative ird = new Cash(i, 0.0, FUNDING_CURVE_NAME);
      CASH.add(ird);
      MIXED_INSTRUMENT.add(ird);
      FUNDING_NODES[i] = i;
      dataM[i] = Math.random() / 10;
      dataNpM[i + N] = dataM[i];
    }

    final Interpolator1DWithSensitivities<Interpolator1DDataBundle> interpolator = new LinearInterpolator1DWithSensitivities();
    final ExtrapolatorMethod<Interpolator1DDataBundle, InterpolationResultWithSensitivities> flat_em_sense = new FlatExtrapolatorWithSensitivities<Interpolator1DDataBundle, InterpolationResultWithSensitivities>();
    EXTRAPOLATOR = new ExtrapolatorOld1D<Interpolator1DDataBundle, InterpolationResultWithSensitivities>(flat_em_sense, interpolator);

    CASH_CURVES = new LinkedHashMap<String, FixedNodeInterpolator1D>();
    FRA_CURVES = new LinkedHashMap<String, FixedNodeInterpolator1D>();
    MIXED_CURVES = new LinkedHashMap<String, FixedNodeInterpolator1D>();
    FixedNodeInterpolator1D fundInterpolator = new FixedNodeInterpolator1D(FUNDING_NODES, EXTRAPOLATOR);
    FixedNodeInterpolator1D fwdInterpolator = new FixedNodeInterpolator1D(FORWARD_NODES, EXTRAPOLATOR);
    CASH_CURVES.put(FUNDING_CURVE_NAME, fundInterpolator);
    FRA_CURVES.put(FORWARD_CURVE_NAME, fwdInterpolator);
    MIXED_CURVES.put(FORWARD_CURVE_NAME, fwdInterpolator);
    MIXED_CURVES.put(FUNDING_CURVE_NAME, fundInterpolator);

    XM = new DoubleMatrix1D(dataM);
    XN = new DoubleMatrix1D(dataN);
    XNM = new DoubleMatrix1D(dataNpM);
    CASH_ONLY = new MultipleYieldCurveFinderJacobian(CASH, CASH_CURVES, null, SENSITIVITY_CALCULATOR);
    FRA_ONLY = new MultipleYieldCurveFinderJacobian(FRA, FRA_CURVES, null, SENSITIVITY_CALCULATOR);
    MIXED = new MultipleYieldCurveFinderJacobian(MIXED_INSTRUMENT, MIXED_CURVES, null, SENSITIVITY_CALCULATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDerivatives() {
    new MultipleYieldCurveFinderJacobian(null, MIXED_CURVES, null, SENSITIVITY_CALCULATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullInterpolator() {
    new MultipleYieldCurveFinderJacobian(CASH, null, null, SENSITIVITY_CALCULATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyDerivatives() {
    new MultipleYieldCurveFinderJacobian(new ArrayList<InterestRateDerivative>(), MIXED_CURVES, null, SENSITIVITY_CALCULATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongNumberOfNodes() {
    new MultipleYieldCurveFinderJacobian(MIXED_INSTRUMENT, CASH_CURVES, null, SENSITIVITY_CALCULATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalculator() {
    new MultipleYieldCurveFinderJacobian(MIXED_INSTRUMENT, CASH_CURVES, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullVector() {
    CASH_ONLY.evaluate((DoubleMatrix1D) null, (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongNumberOfElements() {
    CASH_ONLY.evaluate(XN, (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCurveAlreadyPresent() {
    new MultipleYieldCurveFinderJacobian(CASH, CASH_CURVES, new YieldCurveBundle(Collections.<String, YieldAndDiscountCurve> singletonMap(FUNDING_CURVE_NAME, new ConstantYieldCurve(2.))),
        SENSITIVITY_CALCULATOR);
  }

  @Test
  public void testCashOnly() {
    final DoubleMatrix2D jacobian = CASH_ONLY.evaluate(XM, (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
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
    final DoubleMatrix2D jacobian = FRA_ONLY.evaluate(XN, (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
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
    final DoubleMatrix2D jacobian = MIXED.evaluate(XNM, (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
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
