/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.leastsquare;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.ParameterizedFunction;
import com.opengamma.analytics.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.analytics.math.linearalgebra.LUDecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.DoubleMatrixUtils;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.analytics.math.minimization.BrentMinimizer1D;
import com.opengamma.analytics.math.minimization.ConjugateGradientVectorMinimizer;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NonLinearLeastSquareTest {
  private static final Logger s_logger = LoggerFactory.getLogger(NonLinearLeastSquareTest.class);
  private static final int HOTSPOT_WARMUP_CYCLES = 0;
  private static final int BENCHMARK_CYCLES = 1;

  private static final NormalDistribution NORMAL = new NormalDistribution(0, 1.0, new MersenneTwister64(MersenneTwister.DEFAULT_SEED));
  private static final DoubleMatrix1D X;
  private static final DoubleMatrix1D Y;
  private static final DoubleMatrix1D SIGMA;
  private static final NonLinearLeastSquare LS;

  private static final Function1D<Double, Double> TARGET = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return Math.sin(x);
    }
  };

  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> FUNCTION = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public DoubleMatrix1D evaluate(final DoubleMatrix1D a) {
      Validate.isTrue(a.getNumberOfElements() == 4, "four parameters");
      final int n = X.getNumberOfElements();
      final double[] res = new double[n];
      for (int i = 0; i < n; i++) {
        res[i] = a.getEntry(0) * Math.sin(a.getEntry(1) * X.getEntry(i) + a.getEntry(2)) + a.getEntry(3);
      }
      return new DoubleMatrix1D(res);
    }
  };

  private static final ParameterizedFunction<Double, DoubleMatrix1D, Double> PARAM_FUNCTION = new ParameterizedFunction<Double, DoubleMatrix1D, Double>() {

    @Override
    public Double evaluate(final Double x, final DoubleMatrix1D a) {
      Validate.isTrue(a.getNumberOfElements() == 4, "four parameters");
      return a.getEntry(0) * Math.sin(a.getEntry(1) * x + a.getEntry(2)) + a.getEntry(3);
    }
  };

  private static final ParameterizedFunction<Double, DoubleMatrix1D, DoubleMatrix1D> PARAM_GRAD = new ParameterizedFunction<Double, DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(final Double x, final DoubleMatrix1D a) {
      Validate.isTrue(a.getNumberOfElements() == 4, "four parameters");
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

  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> GRAD = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public DoubleMatrix2D evaluate(final DoubleMatrix1D a) {
      final int n = X.getNumberOfElements();
      final int m = a.getNumberOfElements();
      final double[][] res = new double[n][m];
      for (int i = 0; i < n; i++) {
        final DoubleMatrix1D temp = PARAM_GRAD.evaluate(X.getEntry(i), a);
        Validate.isTrue(m == temp.getNumberOfElements());
        for (int j = 0; j < m; j++) {
          res[i][j] = temp.getEntry(j);
        }
      }
      return new DoubleMatrix2D(res);
    }
  };

  static {
    X = new DoubleMatrix1D(new double[20]);
    Y = new DoubleMatrix1D(new double[20]);
    SIGMA = new DoubleMatrix1D(new double[20]);

    for (int i = 0; i < 20; i++) {
      X.getData()[i] = -Math.PI + i * Math.PI / 10;
      Y.getData()[i] = TARGET.evaluate(X.getEntry(i));
      SIGMA.getData()[i] = 0.1 * Math.exp(Math.abs(X.getEntry(i)) / Math.PI);
    }

    LS = new NonLinearLeastSquare();
  }

  @Test
  public void solveExactTest() {
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {1.2, 0.8, -0.2, -0.3 });
    LeastSquareResults result = LS.solve(X, Y, SIGMA, PARAM_FUNCTION, PARAM_GRAD, start);
    assertEquals(0.0, result.getChiSq(), 1e-8);
    assertEquals(1.0, result.getFitParameters().getEntry(0), 1e-8);
    assertEquals(1.0, result.getFitParameters().getEntry(1), 1e-8);
    assertEquals(0.0, result.getFitParameters().getEntry(2), 1e-8);
    assertEquals(0.0, result.getFitParameters().getEntry(3), 1e-8);
    result = LS.solve(X, Y, SIGMA.getEntry(0), PARAM_FUNCTION, PARAM_GRAD, start);
    assertEquals(0.0, result.getChiSq(), 1e-8);
    assertEquals(1.0, result.getFitParameters().getEntry(0), 1e-8);
    assertEquals(1.0, result.getFitParameters().getEntry(1), 1e-8);
    assertEquals(0.0, result.getFitParameters().getEntry(2), 1e-8);
    assertEquals(0.0, result.getFitParameters().getEntry(3), 1e-8);
    result = LS.solve(X, Y, PARAM_FUNCTION, PARAM_GRAD, start);
    assertEquals(0.0, result.getChiSq(), 1e-8);
    assertEquals(1.0, result.getFitParameters().getEntry(0), 1e-8);
    assertEquals(1.0, result.getFitParameters().getEntry(1), 1e-8);
    assertEquals(0.0, result.getFitParameters().getEntry(2), 1e-8);
    assertEquals(0.0, result.getFitParameters().getEntry(3), 1e-8);
  }

  @Test
  public void solveExactTest2() {
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {0.2, 1.8, 0.2, 0.3 });
    final LeastSquareResults result = LS.solve(Y, SIGMA, FUNCTION, start);
    assertEquals(0.0, result.getChiSq(), 1e-8);
    assertEquals(1.0, result.getFitParameters().getEntry(0), 1e-8);
    assertEquals(1.0, result.getFitParameters().getEntry(1), 1e-8);
    assertEquals(0.0, result.getFitParameters().getEntry(2), 1e-8);
    assertEquals(0.0, result.getFitParameters().getEntry(3), 1e-8);
  }

  public void solveExactFromChiSqTest() {
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {1.2, 0.8, -0.2, -0.3 });
    final Function1D<DoubleMatrix1D, Double> f = getChiSqFunction(X, Y, SIGMA, PARAM_FUNCTION);
    final ConjugateGradientVectorMinimizer minimizer = new ConjugateGradientVectorMinimizer(new BrentMinimizer1D());
    final DoubleMatrix1D solution = minimizer.minimize(f, start);
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
      final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on Levenberg-Marquardt", BENCHMARK_CYCLES);
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
      final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on Conjugate gradient", BENCHMARK_CYCLES);
      for (int i = 0; i < BENCHMARK_CYCLES; i++) {
        solveExactFromChiSqTest();
      }
      timer.finished();
    }
  }

  @Test
  public void solveExactWithoutGradientTest() {

    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {1.2, 0.8, -0.2, -0.3 });

    final NonLinearLeastSquare ls = new NonLinearLeastSquare();
    final LeastSquareResults result = ls.solve(X, Y, SIGMA, PARAM_FUNCTION, start);
    assertEquals(0.0, result.getChiSq(), 1e-8);
    assertEquals(1.0, result.getFitParameters().getEntry(0), 1e-8);
    assertEquals(1.0, result.getFitParameters().getEntry(1), 1e-8);
    assertEquals(0.0, result.getFitParameters().getEntry(2), 1e-8);
    assertEquals(0.0, result.getFitParameters().getEntry(3), 1e-8);
  }

  @Test
  public void solveRandomNoiseTest() {
    final MatrixAlgebra ma = new OGMatrixAlgebra();
    final double[] y = new double[20];
    for (int i = 0; i < 20; i++) {
      y[i] = Y.getEntry(i) + SIGMA.getEntry(i) * NORMAL.nextRandom();
    }
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {0.7, 1.4, 0.2, -0.3 });
    final NonLinearLeastSquare ls = new NonLinearLeastSquare();
    final LeastSquareResults res = ls.solve(X, new DoubleMatrix1D(y), SIGMA, PARAM_FUNCTION, PARAM_GRAD, start);

    final double chiSqDoF = res.getChiSq() / 16;
    assertTrue(chiSqDoF > 0.25);
    assertTrue(chiSqDoF < 3.0);

    final DoubleMatrix1D trueValues = new DoubleMatrix1D(new double[] {1, 1, 0, 0 });
    final DoubleMatrix1D delta = (DoubleMatrix1D) ma.subtract(res.getFitParameters(), trueValues);

    final LUDecompositionCommons decmp = new LUDecompositionCommons();
    final LUDecompositionResult decmpRes = decmp.evaluate(res.getCovariance());
    final DoubleMatrix2D invCovariance = decmpRes.solve(DoubleMatrixUtils.getIdentityMatrix2D(4));

    double z = ma.getInnerProduct(delta, ma.multiply(invCovariance, delta));
    z = Math.sqrt(z);

    assertTrue(z < 3.0);

    //     System.out.println("chiSqr: " + res.getChiSq());
    //     System.out.println("params: " + res.getParameters());
    //     System.out.println("covariance: " + res.getCovariance());
    //     System.out.println("z: " + z);
  }

  @Test
  public void smallPertubationTest() {
    final MatrixAlgebra ma = new OGMatrixAlgebra();
    final double[] dy = new double[20];
    for (int i = 0; i < 20; i++) {
      dy[i] = 0.1 * SIGMA.getEntry(i) * NORMAL.nextRandom();
    }
    final DoubleMatrix1D deltaY = new DoubleMatrix1D(dy);
    final DoubleMatrix1D solution = new DoubleMatrix1D(new double[] {1.0, 1.0, 0.0, 0.0 });
    final NonLinearLeastSquare ls = new NonLinearLeastSquare();
    final DoubleMatrix2D res = ls.calInverseJacobian(SIGMA, FUNCTION, GRAD, solution);
    //  System.out.println("invese Jac: " + res);

    final DoubleMatrix1D deltaParms = (DoubleMatrix1D) ma.multiply(res, deltaY);
    // System.out.println("delta parms: " + deltaParms);

    final DoubleMatrix1D y = (DoubleMatrix1D) ma.add(Y, deltaY);

    final LeastSquareResults lsRes = ls.solve(X, y, SIGMA, PARAM_FUNCTION, PARAM_GRAD, solution);
    final DoubleMatrix1D trueDeltaParms = (DoubleMatrix1D) ma.subtract(lsRes.getFitParameters(), solution);
    // System.out.println("true delta parms: " + trueDeltaParms);

    assertEquals(trueDeltaParms.getEntry(0), deltaParms.getEntry(0), 5e-5);
    assertEquals(trueDeltaParms.getEntry(1), deltaParms.getEntry(1), 5e-5);
    assertEquals(trueDeltaParms.getEntry(2), deltaParms.getEntry(2), 5e-5);
    assertEquals(trueDeltaParms.getEntry(3), deltaParms.getEntry(3), 5e-5);
  }

  private Function1D<DoubleMatrix1D, Double> getChiSqFunction(final DoubleMatrix1D x, final DoubleMatrix1D y, final DoubleMatrix1D sigma,
      final ParameterizedFunction<Double, DoubleMatrix1D, Double> paramFunc) {

    final int n = x.getNumberOfElements();
    if (y.getNumberOfElements() != n) {
      throw new IllegalArgumentException("y wrong length");
    }
    if (sigma.getNumberOfElements() != n) {
      throw new IllegalArgumentException("sigma wrong length");
    }

    final double[] invSigmaSq = new double[n];
    for (int i = 0; i < n; i++) {
      if (sigma.getEntry(i) <= 0.0) {
        throw new IllegalArgumentException("invalid sigma");
      }
      invSigmaSq[i] = 1 / sigma.getEntry(i) / sigma.getEntry(i);
    }

    final Function1D<DoubleMatrix1D, Double> func = new Function1D<DoubleMatrix1D, Double>() {
      @Override
      public Double evaluate(final DoubleMatrix1D params) {
        double sum = 0;
        for (int k = 0; k < n; k++) {
          sum += invSigmaSq[k] * FunctionUtils.square(y.getEntry(k) - paramFunc.evaluate(x.getEntry(k), params));
        }
        return sum;
      }

    };

    return func;
  }

}
