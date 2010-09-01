/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.LeastSquare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.UtilFunctions;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.ParameterizedFunction;
import com.opengamma.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.math.linearalgebra.LUDecompositionResult;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.DoubleMatrixUtils;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.math.matrix.OGMatrixAlgebra;
import com.opengamma.math.minimization.BrentMinimizer1D;
import com.opengamma.math.minimization.ConjugateGradientVectorMinimizer;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.OperationTimer;

/**
 * 
 */
public class NonlinearLeastSquareTest {
  private static final Logger s_logger = LoggerFactory.getLogger(NonlinearLeastSquareTest.class);
  private static final int HOTSPOT_WARMUP_CYCLES = 200;
  private static final int BENCHMARK_CYCLES = 1000;

  private static final NormalDistribution NORMAL = new NormalDistribution(0, 1.0);
  private static final double[] X;
  private static final double[] Y;
  private static final double[] SIGMA;
  private static final NonlinearLeastSquare LS;

  private static final Function1D<Double, Double> TARRGET = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(Double x) {
      return Math.sin(x);
    }
  };

  private static final ParameterizedFunction<Double, DoubleMatrix1D, Double> PARM_FUNCTION = new ParameterizedFunction<Double, DoubleMatrix1D, Double>() {

    @Override
    public Double evaluate(Double x, DoubleMatrix1D a) {
      ArgumentChecker.isTrue(a.getNumberOfElements() == 4, "four parameters");
      return a.getEntry(0) * Math.sin(a.getEntry(1) * x + a.getEntry(2)) + a.getEntry(3);
    }
  };

  private static final ParameterizedFunction<Double, DoubleMatrix1D, DoubleMatrix1D> PARM_GRAD = new ParameterizedFunction<Double, DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(Double x, DoubleMatrix1D a) {
      ArgumentChecker.isTrue(a.getNumberOfElements() == 4, "four parameters");
      double temp1 = Math.sin(a.getEntry(1) * x + a.getEntry(2));
      double temp2 = Math.cos(a.getEntry(1) * x + a.getEntry(2));
      double[] res = new double[4];
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

    LS = new NonlinearLeastSquare(X, Y, SIGMA);
  }

  @Test
  public void solveExactTest() {

    DoubleMatrix1D start = new DoubleMatrix1D(new double[] {1.2, 0.8, -0.2, -0.3});
    LeastSquareResults res = LS.solve(PARM_FUNCTION, PARM_GRAD, start);
    assertEquals(0.0, res.getChiSq(), 1e-8);
    assertEquals(1.0, res.getParameters().getEntry(0), 1e-8);
    assertEquals(1.0, res.getParameters().getEntry(1), 1e-8);
    assertEquals(0.0, res.getParameters().getEntry(2), 1e-8);
    assertEquals(0.0, res.getParameters().getEntry(3), 1e-8);
  }

  public void solveExactFromChiSqTest() {
    DoubleMatrix1D start = new DoubleMatrix1D(new double[] {1.2, 0.8, -0.2, -0.3});
    Function1D<DoubleMatrix1D, Double> f = getChiSqFunction(X, Y, SIGMA, PARM_FUNCTION);
    ConjugateGradientVectorMinimizer minimizer = new ConjugateGradientVectorMinimizer(new BrentMinimizer1D());
    DoubleMatrix1D solution = minimizer.minimize(f, start);
    assertEquals(0.0, f.evaluate(solution), 1e-8);
    assertEquals(1.0, solution.getEntry(0), 1e-8);
    assertEquals(1.0, solution.getEntry(1), 1e-8);
    assertEquals(0.0, solution.getEntry(2), 1e-8);
    assertEquals(0.0, solution.getEntry(3), 1e-8);

  }

  @Test
  public void doExactHotSpot() {
    for (int i = 0; i < HOTSPOT_WARMUP_CYCLES; i++) {
      solveExactWithoutGradientTest();
    }
    if (BENCHMARK_CYCLES > 0) {
      final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on Levebberg-Marquardt",
          BENCHMARK_CYCLES);
      for (int i = 0; i < BENCHMARK_CYCLES; i++) {
        solveExactWithoutGradientTest();
      }
      timer.finished();
    }
  }

  @Test
  public void doChiSqHotSpot() {
    for (int i = 0; i < HOTSPOT_WARMUP_CYCLES; i++) {
      solveExactFromChiSqTest();
    }
    if (BENCHMARK_CYCLES > 0) {
      final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on Conugate gradient",
          BENCHMARK_CYCLES);
      for (int i = 0; i < BENCHMARK_CYCLES; i++) {
        solveExactFromChiSqTest();
      }
      timer.finished();
    }
  }

  @Test
  public void solveExactWithoutGradientTest() {

    DoubleMatrix1D start = new DoubleMatrix1D(new double[] {1.2, 0.8, -0.2, -0.3});
    LeastSquareResults res = LS.solve(PARM_FUNCTION, start);
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
    MatrixAlgebra ma = new OGMatrixAlgebra();
    double[] y = new double[20];
    for (int i = 0; i < 20; i++) {
      y[i] = Y[i] + SIGMA[i] * NORMAL.nextRandom();
    }
    DoubleMatrix1D start = new DoubleMatrix1D(new double[] {0.7, 1.4, 0.2, -0.3});
    NonlinearLeastSquare ls = new NonlinearLeastSquare(X, y, SIGMA);
    LeastSquareResults res = ls.solve(PARM_FUNCTION, PARM_GRAD, start);

    double chiSqDoF = res.getChiSq() / 16;
    assertTrue(chiSqDoF > 0.25);
    assertTrue(chiSqDoF < 3.0);

    DoubleMatrix1D trueValues = new DoubleMatrix1D(new double[] {1, 1, 0, 0});
    DoubleMatrix1D delta = (DoubleMatrix1D) ma.subtract(res.getParameters(), trueValues);

    LUDecompositionCommons decmp = new LUDecompositionCommons();
    LUDecompositionResult decmpRes = decmp.evaluate(res.getCovariance());
    DoubleMatrix2D invCovariance = decmpRes.solve(DoubleMatrixUtils.getIdentityMatrix2D(4));

    double z = ma.getInnerProduct(delta, ma.multiply(invCovariance, delta));
    z = Math.sqrt(z);

    assertTrue(chiSqDoF < 4.0);

    //    System.out.println("chiSqr: " + res.getChiSq());
    //    System.out.println("params: " + res.getParameters());
    //    System.out.println("covariance: " + res.getCovariance());
    //    System.out.println("z: " + z);
  }

  private Function1D<DoubleMatrix1D, Double> getChiSqFunction(final double[] x, final double[] y, double[] sigma,
      final ParameterizedFunction<Double, DoubleMatrix1D, Double> paramFunc) {

    final int n = x.length;
    if (y.length != n) {
      throw new IllegalArgumentException("y wrong length");
    }
    if (sigma.length != n) {
      throw new IllegalArgumentException("sigma wrong length");
    }

    final double[] invSigmaSq = new double[n];
    for (int i = 0; i < n; i++) {
      if (sigma[i] <= 0.0) {
        throw new IllegalArgumentException("invalide sigma");
      }
      invSigmaSq[i] = 1 / sigma[i] / sigma[i];
    }

    Function1D<DoubleMatrix1D, Double> func = new Function1D<DoubleMatrix1D, Double>() {
      @Override
      public Double evaluate(DoubleMatrix1D params) {
        double sum = 0;
        for (int k = 0; k < n; k++) {
          sum += invSigmaSq[k] * UtilFunctions.square(y[k] - paramFunc.evaluate(x[k], params));
        }
        return sum;
      }

    };

    return func;
  }

}
