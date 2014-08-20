/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.discrete;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.math.function.DoublesVectorFunctionProvider;
import com.opengamma.analytics.math.function.InterpolatedVectorFunctionProvider;
import com.opengamma.analytics.math.function.ParameterizedCurve;
import com.opengamma.analytics.math.function.ParameterizedCurveVectorFunctionProvider;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.util.AssertMatrix;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class ParameterizedSABRModelDiscreteVolatilityFunctionProviderTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final ForwardCurve s_fwdCurve = new ForwardCurve(0.01, 0.02);
  private static final ParameterizedCurve s_flat = new ParameterizedCurve() {
    @Override
    public int getNumberOfParameters() {
      return 1;
    }

    @Override
    public Double evaluate(Double x, DoubleMatrix1D parameters) {
      ArgumentChecker.notNull(parameters, "parameters");
      ArgumentChecker.isTrue(parameters.getNumberOfElements() == getNumberOfParameters(), "parameters wrong size");
      return parameters.getEntry(0);
    }
  };

  /**
   * set up using constant (across expiry) SABR parameters 
   */
  @Test
  public void test() {

    ParameterizedCurve[] curves = new ParameterizedCurve[4];
    Arrays.fill(curves, s_flat);

    ParameterizedSABRModelDiscreteVolatilityFunctionProvider dvfp = new ParameterizedSABRModelDiscreteVolatilityFunctionProvider(s_fwdCurve, curves);

    double[] expiries = new double[] {0.5, 1.0, 4.0 };
    double[] strikes = new double[] {0.002, 0.004, 0.006, 0.01, 0.02 };
    int nExp = expiries.length;
    int nStrikes = strikes.length;
    List<DoublesPair> points = new ArrayList<>(nExp * nStrikes);
    for (int i = 0; i < nExp; i++) {
      for (int j = 0; j < nStrikes; j++) {
        points.add(DoublesPair.of(expiries[i], strikes[j]));
      }
    }
    DiscreteVolatilityFunction func = dvfp.from(points);
    double alpha = 0.2;
    double beta = 0.78;
    double rho = -0.3;
    double nu = 0.5;
    DoubleMatrix1D sabrParms = new DoubleMatrix1D(alpha, beta, rho, nu);
    DoubleMatrix1D vols = func.evaluate(sabrParms);
    SABRHaganVolatilityFunction hagan = new SABRHaganVolatilityFunction();

    for (int i = 0; i < nExp; i++) {
      double t = expiries[i];
      double fwd = s_fwdCurve.getForward(t);
      for (int j = 0; j < nStrikes; j++) {
        double vol = hagan.getVolatility(fwd, strikes[j], t, alpha, beta, rho, nu);
        assertEquals(vol, vols.getEntry(i * nStrikes + j), 1e-15);
      }
    }

    DoubleMatrix2D jac = func.calculateJacobian(sabrParms);
    DoubleMatrix2D jacFD = func.calculateJacobianViaFD(sabrParms);
    AssertMatrix.assertEqualsMatrix(jacFD, jac, 1e-8);
  }

  /**
   * set up with a mix of interpolated and parameterised curves representing the SABR parameter term structures 
   */
  @Test
  public void mixedTSTest() {
    Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);

    DoublesVectorFunctionProvider alpha = new InterpolatedVectorFunctionProvider(interpolator, new double[] {1.0, 3.0, 5.0, 7.0, 10.0 });
    DoublesVectorFunctionProvider beta = new ParameterizedCurveVectorFunctionProvider(s_flat);
    DoublesVectorFunctionProvider rho = new InterpolatedVectorFunctionProvider(interpolator, new double[] {3.0, 7.0, 10.0 });
    DoublesVectorFunctionProvider nu = new InterpolatedVectorFunctionProvider(interpolator, new double[] {1.0, 3.0, 5.0, 7.0, 10.0 });
    DoublesVectorFunctionProvider[] toSmileParms = new DoublesVectorFunctionProvider[] {alpha, beta, rho, nu };

    DiscreteVolatilityFunctionProvider dvfp = new ParameterizedSABRModelDiscreteVolatilityFunctionProvider(s_fwdCurve, toSmileParms);

    final int nSamples = 50;
    DoublesPair[] points = new DoublesPair[nSamples];
    for (int i = 0; i < nSamples; i++) {
      double t = 10.0 * RANDOM.nextDouble();
      double k = 0.03 * RANDOM.nextDouble();
      points[i] = DoublesPair.of(t, k);
    }

    DiscreteVolatilityFunction func = dvfp.from(points);

    assertEquals(nSamples, func.getLengthOfRange());
    DoubleMatrix1D parms = new DoubleMatrix1D(func.getLengthOfDomain());
    int pos = 0;
    for (; pos < 5; pos++) {
      parms.getData()[pos] = 0.1 + 0.2 * RANDOM.nextDouble();
    }
    parms.getData()[pos++] = 0.8;
    for (; pos < 9; pos++) {
      parms.getData()[pos] = -0.3 + 0.6 * RANDOM.nextDouble();
    }
    for (; pos < 14; pos++) {
      parms.getData()[pos] = 0.2 + 0.6 * RANDOM.nextDouble();
    }

    DoubleMatrix2D jac = func.calculateJacobian(parms);
    DoubleMatrix2D jacFD = func.calculateJacobianViaFD(parms);
    AssertMatrix.assertEqualsMatrix(jacFD, jac, 5e-5);
  }
}
