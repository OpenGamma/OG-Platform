/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculator;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.tuple.DoublesPair;

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
  private static final Interpolator1D<? extends Interpolator1DDataBundle> EXTRAPOLATOR;
  private static final Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> EXTRAPOLATING_SENSITIVITY_CALCULATOR;
  private static final DoubleMatrix1D XN;
  private static final DoubleMatrix1D XM;
  private static final DoubleMatrix1D XNM;
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> CASH_ONLY;
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> FRA_ONLY;
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> MIXED;
  private static final LinkedHashMap<String, double[]> CASH_NODES;
  private static final LinkedHashMap<String, double[]> FRA_NODES;
  private static final LinkedHashMap<String, double[]> MIXED_NODES;
  private static final LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> CASH_INTERPOLATORS;
  private static final LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> FRA_INTERPOLATORS;
  private static final LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> MIXED_INTERPOLATORS;
  private static final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> CASH_SENSITIVITY_CALCULATOR;
  private static final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> FRA_SENSITIVITY_CALCULATOR;
  private static final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> MIXED_SENSITIVITY_CALCULATOR;

  private static final InterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> SENSITIVITY_CALCULATOR = ParRateCurveSensitivityCalculator.getInstance();

  private static final int N = 10;
  private static final int M = 5;

  static {
    CASH = new ArrayList<InterestRateDerivative>();
    FRA = new ArrayList<InterestRateDerivative>();
    MIXED_INSTRUMENT = new ArrayList<InterestRateDerivative>();
    FORWARD_NODES = new double[N];
    FUNDING_NODES = new double[M];
    final double[] dataN = new double[N];
    final double[] dataM = new double[M];
    final double[] dataNpM = new double[N + M];
    for (int i = 0; i < N; i++) {
      final InterestRateDerivative ird = new ForwardRateAgreement(i, i + 0.5, 0.0, FUNDING_CURVE_NAME, FORWARD_CURVE_NAME);
      FRA.add(ird);
      MIXED_INSTRUMENT.add(ird);
      FORWARD_NODES[i] = i + 1;
      dataN[i] = Math.random() / 10;
      dataNpM[i] = dataN[i];
    }

    for (int i = 0; i < M; i++) {
      final InterestRateDerivative ird = new Cash(i, 0.0, FUNDING_CURVE_NAME);
      CASH.add(ird);
      MIXED_INSTRUMENT.add(ird);
      FUNDING_NODES[i] = i;
      dataM[i] = Math.random() / 10;
      dataNpM[i + N] = dataM[i];
    }
    EXTRAPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    EXTRAPOLATING_SENSITIVITY_CALCULATOR = CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory.getSensitivityCalculator(Interpolator1DFactory.LINEAR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR, false);

    CASH_NODES = new LinkedHashMap<String, double[]>();
    CASH_NODES.put(FUNDING_CURVE_NAME, FUNDING_NODES);
    FRA_NODES = new LinkedHashMap<String, double[]>();
    FRA_NODES.put(FORWARD_CURVE_NAME, FORWARD_NODES);
    MIXED_NODES = new LinkedHashMap<String, double[]>();
    MIXED_NODES.put(FORWARD_CURVE_NAME, FORWARD_NODES);
    MIXED_NODES.put(FUNDING_CURVE_NAME, FUNDING_NODES);
    CASH_INTERPOLATORS = new LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>>();
    CASH_INTERPOLATORS.put(FUNDING_CURVE_NAME, EXTRAPOLATOR);
    FRA_INTERPOLATORS = new LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>>();
    FRA_INTERPOLATORS.put(FORWARD_CURVE_NAME, EXTRAPOLATOR);
    MIXED_INTERPOLATORS = new LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>>();
    MIXED_INTERPOLATORS.put(FORWARD_CURVE_NAME, EXTRAPOLATOR);
    MIXED_INTERPOLATORS.put(FUNDING_CURVE_NAME, EXTRAPOLATOR);
    CASH_SENSITIVITY_CALCULATOR = new LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>>();
    CASH_SENSITIVITY_CALCULATOR.put(FUNDING_CURVE_NAME, EXTRAPOLATING_SENSITIVITY_CALCULATOR);
    FRA_SENSITIVITY_CALCULATOR = new LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>>();
    FRA_SENSITIVITY_CALCULATOR.put(FORWARD_CURVE_NAME, EXTRAPOLATING_SENSITIVITY_CALCULATOR);
    MIXED_SENSITIVITY_CALCULATOR = new LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>>();
    MIXED_SENSITIVITY_CALCULATOR.put(FORWARD_CURVE_NAME, EXTRAPOLATING_SENSITIVITY_CALCULATOR);
    MIXED_SENSITIVITY_CALCULATOR.put(FUNDING_CURVE_NAME, EXTRAPOLATING_SENSITIVITY_CALCULATOR);

    XM = new DoubleMatrix1D(dataM);
    XN = new DoubleMatrix1D(dataN);
    XNM = new DoubleMatrix1D(dataNpM);
    CASH_ONLY = new MultipleYieldCurveFinderJacobian(new MultipleYieldCurveFinderDataBundle(CASH, null, CASH_NODES, CASH_INTERPOLATORS, CASH_SENSITIVITY_CALCULATOR), SENSITIVITY_CALCULATOR);
    FRA_ONLY = new MultipleYieldCurveFinderJacobian(new MultipleYieldCurveFinderDataBundle(FRA, null, FRA_NODES, FRA_INTERPOLATORS, FRA_SENSITIVITY_CALCULATOR), SENSITIVITY_CALCULATOR);
    MIXED = new MultipleYieldCurveFinderJacobian(new MultipleYieldCurveFinderDataBundle(MIXED_INSTRUMENT, null, MIXED_NODES, MIXED_INTERPOLATORS, MIXED_SENSITIVITY_CALCULATOR), SENSITIVITY_CALCULATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalculator() {
    new MultipleYieldCurveFinderJacobian(new MultipleYieldCurveFinderDataBundle(MIXED_INSTRUMENT, null, MIXED_NODES, MIXED_INTERPOLATORS, MIXED_SENSITIVITY_CALCULATOR), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    new MultipleYieldCurveFinderJacobian(null, SENSITIVITY_CALCULATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullVector() {
    CASH_ONLY.evaluate((DoubleMatrix1D) null);
  }

  @Test(expected = IllegalArgumentException.class)
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
