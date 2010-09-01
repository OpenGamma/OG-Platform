/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.leastsquare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.ParameterizedFunction;
import com.opengamma.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.math.linearalgebra.LUDecompositionResult;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.DoubleMatrixUtils;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.math.matrix.OGMatrixAlgebra;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class NonLinearLeastSquareTest {

  private static final NormalDistribution NORMAL = new NormalDistribution(0, 1.0, new MersenneTwister64(MersenneTwister64.DEFAULT_SEED));
  private static final double[] X;
  private static final double[] Y;
  private static final double[] SIGMA;
  private static final NonLinearLeastSquare LS;

  private static final Function1D<Double, Double> TARRGET = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return Math.sin(x);
    }
  };

  private static final ParameterizedFunction<Double, DoubleMatrix1D, Double> PARM_FUNCTION = new ParameterizedFunction<Double, DoubleMatrix1D, Double>() {

    @Override
    public Double evaluate(final Double x, final DoubleMatrix1D a) {
      ArgumentChecker.isTrue(a.getNumberOfElements() == 4, "four parameters");
      return a.getEntry(0) * Math.sin(a.getEntry(1) * x + a.getEntry(2)) + a.getEntry(3);
    }
  };

  private static final ParameterizedFunction<Double, DoubleMatrix1D, DoubleMatrix1D> PARM_GRAD = new ParameterizedFunction<Double, DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(final Double x, final DoubleMatrix1D a) {
      ArgumentChecker.isTrue(a.getNumberOfElements() == 4, "four parameters");
      final double temp1 = Math.sin(a.getEntry(1) * x + a.getEntry(2));
      final double temp2 = Math.cos(a.getEntry(1) * x + a.getEntry(2));
      final double[] res = new double[4];
      res[0] = temp1;
      res[2] = a.getEntry(0) * temp2;
      res[1] = x * res[2];
      res[3] = 1.0;
      return new DoubleMatrix1D(res);
    }
  };

  static {
    X = new double[20];
    Y = new double[20];
    SIGMA = new double[20];

    for (int i = 0; i < 20; i++) {
      X[i] = -Math.PI + i * Math.PI / 10;
      Y[i] = TARRGET.evaluate(X[i]);
      SIGMA[i] = 0.1 * Math.exp(Math.abs(X[i]) / Math.PI);
    }

    LS = new NonLinearLeastSquare(X, Y, SIGMA);
  }

  @Test
  public void solveExactTest() {

    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {1.2, 0.8, -0.2, -0.3});
    final LeastSquareResults res = LS.solve(PARM_FUNCTION, PARM_GRAD, start);
    assertEquals(0.0, res.getChiSq(), 1e-8);
    assertEquals(1.0, res.getParameters().getEntry(0), 1e-8);
    assertEquals(1.0, res.getParameters().getEntry(1), 1e-8);
    assertEquals(0.0, res.getParameters().getEntry(2), 1e-8);
    assertEquals(0.0, res.getParameters().getEntry(3), 1e-8);
  }

  @Test
  public void solveExactWithoutGradientTest() {

    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {1.2, 0.8, -0.2, -0.3});
    final LeastSquareResults res = LS.solve(PARM_FUNCTION, start);
    assertEquals(0.0, res.getChiSq(), 1e-8);
    assertEquals(1.0, res.getParameters().getEntry(0), 1e-8);
    assertEquals(1.0, res.getParameters().getEntry(1), 1e-8);
    assertEquals(0.0, res.getParameters().getEntry(2), 1e-8);
    assertEquals(0.0, res.getParameters().getEntry(3), 1e-8);
  }

  /**
   * This tests a fit to random data, so it could fail or rare occasions. Only consecutive fails indicate a bug 
   */
  @Test
  public void solveRandomNoiseTest() {
    final MatrixAlgebra ma = new OGMatrixAlgebra();
    final double[] y = new double[20];
    for (int i = 0; i < 20; i++) {
      y[i] = Y[i] + SIGMA[i] * NORMAL.nextRandom();
    }
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {0.7, 1.4, 0.2, -0.3});
    final NonLinearLeastSquare ls = new NonLinearLeastSquare(X, y, SIGMA);
    final LeastSquareResults res = ls.solve(PARM_FUNCTION, PARM_GRAD, start);

    final double chiSqDoF = res.getChiSq() / 16;
    assertTrue(chiSqDoF > 0.25);
    assertTrue(chiSqDoF < 3.0);

    final DoubleMatrix1D trueValues = new DoubleMatrix1D(new double[] {1, 1, 0, 0});
    final DoubleMatrix1D delta = (DoubleMatrix1D) ma.subtract(res.getParameters(), trueValues);

    final LUDecompositionCommons decmp = new LUDecompositionCommons();
    final LUDecompositionResult decmpRes = decmp.evaluate(res.getCovariance());
    final DoubleMatrix2D invCovariance = decmpRes.solve(DoubleMatrixUtils.getIdentityMatrix2D(4));

    double z = ma.getInnerProduct(delta, ma.multiply(invCovariance, delta));
    z = Math.sqrt(z);

    assertTrue(chiSqDoF < 4.0);

    //    System.out.println("chiSqr: " + res.getChiSq());
    //    System.out.println("params: " + res.getParameters());
    //    System.out.println("covariance: " + res.getCovariance());
    //    System.out.println("z: " + z);
  }
}
