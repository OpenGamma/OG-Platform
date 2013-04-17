/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.leastsquare;

import java.util.List;

import org.testng.annotations.Test;

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
import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.rootfinding.newton.NewtonDefaultVectorRootFinder;

/**
 * 
 */
public class NonLinearLeastSquareWithPenaltyTest {
  private static final MatrixAlgebra MA = new ColtMatrixAlgebra();

  private static BasisFunctionGenerator GEN = new BasisFunctionGenerator();
  private static NonLinearLeastSquareWithPenalty NLLSWP = new NonLinearLeastSquareWithPenalty();
  private static double[] TENORS = new double[] {1, 2, 3, 5, 7, 10, 15, 20};
  private static double[] RATES = new double[] {0.02, 0.025, 0.03, 0.031, 0.028, 0.032, 0.035, 0.04};
  private static int FREQ = 2;
  private static int N_SWAPS = 8;
  private static Function1D<Curve<Double, Double>, DoubleMatrix1D> swapRateFunction;

  // pSpline parameters
  private static int N_KNOTS = 20;
  private static int DEGREE = 3;
  private static int DIFFERENCE_ORDER = 2;
  private static double LAMBDA = 1;
  private static DoubleMatrix2D PENALTY_MAT;
  private static List<Function1D<Double, Double>> B_SPLINES;
  private static Function1D<DoubleMatrix1D, DoubleMatrix1D> WEIGHTS_TO_SWAP_FUNC;

  static {
    B_SPLINES = GEN.generateSet(0.0, TENORS[TENORS.length - 1], N_KNOTS, DEGREE);
    PSplineFitter psf = new PSplineFitter();
    final int nWeights = B_SPLINES.size();
    PENALTY_MAT = (DoubleMatrix2D) MA.scale(psf.getPenaltyMatrix(nWeights, DIFFERENCE_ORDER), LAMBDA);

    // map from curve to swap rates
    swapRateFunction = new Function1D<Curve<Double, Double>, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(Curve<Double, Double> curve) {
        double[] res = new double[N_SWAPS];
        double sum = 0.0;

        for (int i = 0; i < N_SWAPS; i++) {
          int start = (int) (i == 0 ? 0 : TENORS[i - 1] * FREQ);
          int end = (int) (TENORS[i] * FREQ - 1);
          for (int k = start; k < end; k++) {
            double t = (k + 1) * 1.0 / FREQ;
            sum += Math.exp(-t * curve.getYValue(t));
          }
          double last = Math.exp(-TENORS[i] * curve.getYValue(TENORS[i]));
          sum += last;
          res[i] = FREQ * (1 - last) / sum;
        }

        return new DoubleMatrix1D(res);
      }
    };

    WEIGHTS_TO_SWAP_FUNC = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        final Function1D<Double, Double> func = new BasisFunctionAggregation<>(B_SPLINES, x.getData());
        FunctionalDoublesCurve curve = FunctionalDoublesCurve.from(func);
        return swapRateFunction.evaluate(curve);
      }
    };

  }

  @Test(enabled = false)
  public void printTest() {
    System.out.println("NonLinearLeastSquareWithPenaltyTest");
    int n = B_SPLINES.size();

    for (int j = 0; j < 101; j++) {
      double x = j * 20. / 100;
      System.out.print(x);
      for (int i = 0; i < n; i++) {
        System.out.print("\t" + B_SPLINES.get(i).evaluate(x));
      }
      System.out.print("\n");
    }

  }

  @Test(enabled = false)
  public void test() {
    final int nWeights = B_SPLINES.size();
    LeastSquareResults res = NLLSWP.solve(new DoubleMatrix1D(RATES), WEIGHTS_TO_SWAP_FUNC, new DoubleMatrix1D(nWeights, 0.03), PENALTY_MAT);
    System.out.println(res.getChiSq());

    System.out.println();
    DoubleMatrix1D fittedSwaps = WEIGHTS_TO_SWAP_FUNC.evaluate(res.getFitParameters());
    for (int i = 0; i < N_SWAPS; i++) {
      System.out.println(RATES[i] + "\t" + fittedSwaps.getEntry(i));
    }

    final Function1D<Double, Double> func = new BasisFunctionAggregation<>(B_SPLINES, res.getFitParameters().getData());
    System.out.println();
    for (int i = 0; i < 101; i++) {
      double t = i * 20.0 / 100;
      System.out.println(t + "\t" + func.evaluate(t));
    }

  }

  @Test(enabled = false)
  public void rootTest() {
    final Interpolator1D baseInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);

    NewtonDefaultVectorRootFinder rootFinder = new NewtonDefaultVectorRootFinder();

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> residualFunc = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {
      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
        InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(TENORS, x.getData(), baseInterpolator, true);

        DoubleMatrix1D modelRates = swapRateFunction.evaluate(curve);
        return (DoubleMatrix1D) MA.subtract(new DoubleMatrix1D(RATES), modelRates);
      }
    };

    DoubleMatrix1D rootRes = rootFinder.getRoot(residualFunc, new DoubleMatrix1D(N_SWAPS, 0.03));

    InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(TENORS, rootRes.getData(), baseInterpolator, true);
    System.out.println();
    for (int i = 0; i < 101; i++) {
      double t = i * 20.0 / 100;
      System.out.println(t + "\t" + curve.getYValue(t));
    }

  }

}
