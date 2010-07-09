/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

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
import com.opengamma.math.rootfinding.newton.JacobianCalculator;

/**
 * 
 */
public class DoubleCurveJacobianTest {
  private static final List<InterestRateDerivative> CASH;
  private static final List<InterestRateDerivative> FRA;
  private static final List<InterestRateDerivative> MIXED_INSTRUMENT;
  private static final double SPOT_RATE = 0.04;
  private static final double[] FORWARD_NODES;
  private static final double[] FUNDING_NODES;
  private static final Interpolator1DWithSensitivities<Interpolator1DDataBundle> INTERPOLATOR = new LinearInterpolator1DWithSensitivities();
  private static final DoubleMatrix1D X0;
  //  private static final DoubleMatrix1D X1;
  //  private static final DoubleMatrix1D X2;
  private static final JacobianCalculator CASH_ONLY;
  //  private static final JacobianCalculator FRA_ONLY;
  //  private static final JacobianCalculator MIXED;
  private static final int N = 10;
  private static final int M = 5;

  static {
    CASH = new ArrayList<InterestRateDerivative>();
    FRA = new ArrayList<InterestRateDerivative>();
    MIXED_INSTRUMENT = new ArrayList<InterestRateDerivative>();
    FORWARD_NODES = new double[N];
    FUNDING_NODES = new double[M];
    double[] data1 = new double[N];
    final double[] data2 = new double[N + M];
    for (int i = 0; i < N; i++) {
      FRA.add(new ForwardRateAgreement(i, i + 0.5));
      MIXED_INSTRUMENT.add(new Cash(i));
      FORWARD_NODES[i] = i + 1;
      data1[i] = Math.random() / 10;
      data2[i] = data1[i];
    }
    X0 = new DoubleMatrix1D(data1);
    data1 = new double[M];
    for (int i = 0; i < M; i++) {
      CASH.add(new Cash(i));
      MIXED_INSTRUMENT.add(new ForwardRateAgreement(i + N, i + 0.5 + N));
      FUNDING_NODES[i] = i + 1;
      data1[i] = Math.random() / 10;
      data2[i + N] = data1[i];
    }
    //    X1 = new DoubleMatrix1D(data1);
    //    X2 = new DoubleMatrix1D(data2);
    CASH_ONLY = new DoubleCurveJacobian<Interpolator1DDataBundle>(CASH, SPOT_RATE, null, FUNDING_NODES, INTERPOLATOR, INTERPOLATOR);
    //    FRA_ONLY = new DoubleCurveJacobian<Interpolator1DDataBundle>(FRA, SPOT_RATE, FORWARD_NODES, null, INTERPOLATOR, INTERPOLATOR);
    //    MIXED = new DoubleCurveJacobian<Interpolator1DDataBundle>(MIXED_INSTRUMENT, SPOT_RATE, FORWARD_NODES, FUNDING_NODES, INTERPOLATOR, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDerivatives() {
    new DoubleCurveJacobian<Interpolator1DDataBundle>(null, SPOT_RATE, FORWARD_NODES, FUNDING_NODES, INTERPOLATOR, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullForwardInterpolator() {
    new DoubleCurveJacobian<Interpolator1DDataBundle>(MIXED_INSTRUMENT, SPOT_RATE, FORWARD_NODES, FUNDING_NODES, null, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingInterpolator() {
    new DoubleCurveJacobian<Interpolator1DDataBundle>(MIXED_INSTRUMENT, SPOT_RATE, FORWARD_NODES, FUNDING_NODES, INTERPOLATOR, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyDerivatives() {
    new DoubleCurveJacobian<Interpolator1DDataBundle>(new ArrayList<InterestRateDerivative>(), SPOT_RATE, FORWARD_NODES, FUNDING_NODES, INTERPOLATOR, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongNumberOfNodes() {
    new DoubleCurveJacobian<Interpolator1DDataBundle>(MIXED_INSTRUMENT, SPOT_RATE, FORWARD_NODES, FORWARD_NODES, INTERPOLATOR, INTERPOLATOR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullVector() {
    CASH_ONLY.evaluate((DoubleMatrix1D) null, (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongNumberOfElements() {
    CASH_ONLY.evaluate(X0, (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
  }

  @Test
  public void test() {
  }
}
