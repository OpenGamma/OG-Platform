/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.Interpolator1DWithSensitivities;
import com.opengamma.math.interpolation.LinearInterpolator1DWithSensitivities;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;

/**
 * 
 */
public class SingeCurveJacobianTest {
  private static final List<InterestRateDerivative> CASH;
  private static final List<InterestRateDerivative> FRA;
  private static final double SPOT_RATE = 0.03;
  private static final double[] NODES;
  private static final Interpolator1DWithSensitivities<Interpolator1DDataBundle> INTERPOLATOR = new LinearInterpolator1DWithSensitivities();
  private static final DoubleMatrix1D X;
  private static final JacobianCalculator CASH_ONLY_JACOBIAN;
  private static final JacobianCalculator FRA_ONLY_JACOBIAN;
  private static final int N = 10;

  static {
    CASH = new ArrayList<InterestRateDerivative>();
    FRA = new ArrayList<InterestRateDerivative>();
    NODES = new double[N];
    final double[] data = new double[N];
    for (int i = 0; i < N; i++) {
      CASH.add(new Cash(i));
      FRA.add(new ForwardRateAgreement(i, i + 0.5));
      NODES[i] = i + 1;
      data[i] = Math.random() / 10;
    }
    CASH_ONLY_JACOBIAN = new SingleCurveJacobian<Interpolator1DDataBundle>(CASH, SPOT_RATE, NODES, INTERPOLATOR);
    FRA_ONLY_JACOBIAN = new SingleCurveJacobian<Interpolator1DDataBundle>(FRA, SPOT_RATE, NODES, INTERPOLATOR);
    X = new DoubleMatrix1D(data);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDerivatives() {
    new SingleCurveJacobian<Interpolator1DDataBundle>(null, SPOT_RATE, NODES, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTimes() {
    new SingleCurveJacobian<Interpolator1DDataBundle>(CASH, SPOT_RATE, null, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullInterpolator() {
    new SingleCurveJacobian<Interpolator1DDataBundle>(CASH, SPOT_RATE, NODES, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyDerivatives() {
    new SingleCurveJacobian<Interpolator1DDataBundle>(new ArrayList<InterestRateDerivative>(), SPOT_RATE, NODES, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyTimes() {
    new SingleCurveJacobian<Interpolator1DDataBundle>(CASH, SPOT_RATE, new double[0], INTERPOLATOR);
  }

  @Test
  public void testFundingOnlySensitivities() {
    final DoubleMatrix2D jacobian = CASH_ONLY_JACOBIAN.evaluate(X, (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
    final double[][] data = jacobian.getData();
    assertEquals(data.length, N);
    double[] row;
    for (int i = 0; i < N; i++) {
      row = data[i];
      assertEquals(row.length, N);
      for (int j = 0; j < N; j++) {
        if (i == 0 || j + 1 != i) {
          assertEquals(row[j], 0, 0);
        } else {
          assertEquals(row[j], 1, 0);
        }
      }
    }
  }

  @Test
  public void testForwardOnlySensitivities() {
    final DoubleMatrix2D jacobian = FRA_ONLY_JACOBIAN.evaluate(X, (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
    final double[][] data = jacobian.getData();
    assertEquals(data.length, N);
    double[] row;
    for (int i = 0; i < N; i++) {
      row = data[i];
      assertEquals(row.length, N);
      for (int j = 0; j < N; j++) {
        if (i == 0) {
          if (j != 0) {
            assertEquals(row[j], 0, 0);
          }
        } else if (j != i && j + 1 != i) {
          assertEquals(row[j], 0, 0);
        }
      }
    }
  }

}
