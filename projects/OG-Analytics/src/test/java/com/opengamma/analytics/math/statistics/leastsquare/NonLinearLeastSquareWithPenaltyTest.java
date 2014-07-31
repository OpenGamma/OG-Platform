/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.leastsquare;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.BasisFunctionAggregation;
import com.opengamma.analytics.math.interpolation.BasisFunctionGenerator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.PSplineFitter;
import com.opengamma.analytics.math.interpolation.PenaltyMatrixGenerator;
import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.rootfinding.newton.NewtonDefaultVectorRootFinder;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NonLinearLeastSquareWithPenaltyTest {
  private static final MatrixAlgebra MA = new ColtMatrixAlgebra();

  private static BasisFunctionGenerator GEN = new BasisFunctionGenerator();
  private static NonLinearLeastSquareWithPenalty NLLSWP = new NonLinearLeastSquareWithPenalty();
  private static double[] TENORS = new double[] {1, 2, 3, 5, 7, 10, 15, 20 };
  private static double[] RATES = new double[] {0.02, 0.025, 0.03, 0.031, 0.028, 0.032, 0.035, 0.04 };
  private static int FREQ = 2;
  static int N_SWAPS = 8;
  private static Function1D<Curve<Double, Double>, DoubleMatrix1D> swapRateFunction;

  // pSpline parameters
  private static int N_KNOTS = 20;
  private static int DEGREE = 3;
  private static int DIFFERENCE_ORDER = 2;
  private static double LAMBDA = 1e5;
  private static DoubleMatrix2D PENALTY_MAT;
  private static List<Function1D<Double, Double>> B_SPLINES;
  private static Function1D<DoubleMatrix1D, DoubleMatrix1D> WEIGHTS_TO_SWAP_FUNC;

  static {
    B_SPLINES = GEN.generateSet(0.0, TENORS[TENORS.length - 1], N_KNOTS, DEGREE);
    final PSplineFitter psf = new PSplineFitter();
    final int nWeights = B_SPLINES.size();
    PENALTY_MAT = (DoubleMatrix2D) MA.scale(PenaltyMatrixGenerator.getPenaltyMatrix(nWeights, DIFFERENCE_ORDER), LAMBDA);

    // map from curve to swap rates
    swapRateFunction = new Function1D<Curve<Double, Double>, DoubleMatrix1D>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final Curve<Double, Double> curve) {
        final double[] res = new double[N_SWAPS];
        double sum = 0.0;

        for (int i = 0; i < N_SWAPS; i++) {
          final int start = (int) (i == 0 ? 0 : TENORS[i - 1] * FREQ);
          final int end = (int) (TENORS[i] * FREQ - 1);
          for (int k = start; k < end; k++) {
            final double t = (k + 1) * 1.0 / FREQ;
            sum += Math.exp(-t * curve.getYValue(t));
          }
          final double last = Math.exp(-TENORS[i] * curve.getYValue(TENORS[i]));
          sum += last;
          res[i] = FREQ * (1 - last) / sum;
        }

        return new DoubleMatrix1D(res);
      }
    };

    WEIGHTS_TO_SWAP_FUNC = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final Function1D<Double, Double> func = new BasisFunctionAggregation<>(B_SPLINES, x.getData());
        final FunctionalDoublesCurve curve = FunctionalDoublesCurve.from(func);
        return swapRateFunction.evaluate(curve);
      }
    };

  }

  @Test
  public void linearTest() {
    final boolean print = false;
    if (print) {
      System.out.println("NonLinearLeastSquareWithPenaltyTest.linearTest");
    }
    final PSplineFitter psf = new PSplineFitter();
    final int nWeights = 20;
    final int diffOrder = 2;
    final double lambda = 100.0;
    final DoubleMatrix2D penalty = (DoubleMatrix2D) MA.scale(PenaltyMatrixGenerator.getPenaltyMatrix(nWeights, diffOrder), lambda);
    // final boolean[] on = new boolean[nWeights];
    final int[] onIndex = new int[] {1, 4, 11, 12, 15, 17 };
    final double[] obs = new double[] {0, 1.0, 1.0, 1.0, 0.0, 0.0 };
    final int n = onIndex.length;

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final double[] temp = new double[n];
        for (int i = 0; i < n; i++) {
          temp[i] = x.getEntry(onIndex[i]);
        }
        return new DoubleMatrix1D(temp);
      }
    };

    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jac = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
        final DoubleMatrix2D res = new DoubleMatrix2D(n, nWeights);
        for (int i = 0; i < n; i++) {
          res.getData()[i][onIndex[i]] = 1.0;
        }
        return res;
      }
    };

    final RandomEngine ran = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
    final double[] temp = new double[nWeights];
    for (int i = 0; i < nWeights; i++) {
      temp[i] = ran.nextDouble();
    }
    final DoubleMatrix1D start = new DoubleMatrix1D(temp);

    final LeastSquareResults lsRes = NLLSWP.solve(new DoubleMatrix1D(obs), new DoubleMatrix1D(n, 0.01), func, jac, start, penalty);
    if (print) {
      System.out.println("chi2: " + lsRes.getChiSq());
      System.out.println(lsRes.getFitParameters());
    }
    for (int i = 0; i < n; i++) {
      assertEquals(obs[i], lsRes.getFitParameters().getEntry(onIndex[i]), 0.01);
    }
  }

  /**
   * This simply prints out all the basis functions
   */
  @Test(enabled = false)
  public void printTest() {
    System.out.println("NonLinearLeastSquareWithPenaltyTest");

    final List<Function1D<Double, Double>> bSplines = GEN.generateSet(new double[] {0, 1.0, 2.0, 3.5, 5.0, 7.0, 10., 15, 20 }, 5);
    final int n = bSplines.size();

    final double[] weights = new double[n];
    Arrays.fill(weights, 1.0);
    weights[2] = -0.0;
    weights[3] = -0.0;
    weights[n - 2] = -0.0;
    final BasisFunctionAggregation<Double> func = new BasisFunctionAggregation<>(bSplines, weights);

    for (int j = 0; j < 101; j++) {
      final double x = j * 20. / 100;
      System.out.print(x);
      for (int i = 0; i < n; i++) {
        System.out.print("\t" + bSplines.get(i).evaluate(x));
      }
      System.out.print("\t" + func.evaluate(x));
      System.out.print("\n");
    }

  }

  @Test
  // (enabled = false)
  public void test() {
    final boolean print = false;
    if (print) {
      System.out.println("NonLinearLeastSquareWithPenaltyTest");
    }
    final int nWeights = B_SPLINES.size();
    final LeastSquareResults res = NLLSWP.solve(new DoubleMatrix1D(RATES), new DoubleMatrix1D(RATES.length, 1e-4), WEIGHTS_TO_SWAP_FUNC, new DoubleMatrix1D(nWeights, 0.03), PENALTY_MAT);
    if (print) {
      System.out.println("chi2: " + res.getChiSq());
      System.out.println();
    }
    final DoubleMatrix1D fittedSwaps = WEIGHTS_TO_SWAP_FUNC.evaluate(res.getFitParameters());
    for (int i = 0; i < N_SWAPS; i++) {
      if (print) {
        System.out.println("swap rates: " + RATES[i] + "\t" + fittedSwaps.getEntry(i));
      }
      assertEquals(RATES[i], fittedSwaps.getEntry(i), 1e-4);
    }

    if (print) {
      final Function1D<Double, Double> func = new BasisFunctionAggregation<>(B_SPLINES, res.getFitParameters().getData());
      System.out.println();
      System.out.println("t\t yield");
      for (int i = 0; i < 101; i++) {
        final double t = i * 20.0 / 100;
        System.out.println(t + "\t" + func.evaluate(t));
      }
    }
  }

  @Test(enabled = false)
  public void rootTest() {
    final Interpolator1D baseInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);

    final NewtonDefaultVectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder();

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> residualFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(TENORS, x.getData(), baseInterpolator, true);

        final DoubleMatrix1D modelRates = swapRateFunction.evaluate(curve);
        return (DoubleMatrix1D) MA.subtract(new DoubleMatrix1D(RATES), modelRates);
      }
    };

    final DoubleMatrix1D rootRes = rootFinder.getRoot(residualFunc, new DoubleMatrix1D(N_SWAPS, 0.03));

    final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(TENORS, rootRes.getData(), baseInterpolator, true);
    System.out.println();
    for (int i = 0; i < 101; i++) {
      final double t = i * 20.0 / 100;
      System.out.println(t + "\t" + curve.getYValue(t));
    }

  }

}
