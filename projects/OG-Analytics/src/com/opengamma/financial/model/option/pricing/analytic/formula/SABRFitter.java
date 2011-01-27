/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import org.apache.activemq.util.BitArray;
import org.apache.commons.lang.Validate;

import com.opengamma.math.UtilFunctions;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.ParameterizedFunction;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.minimization.BrentMinimizer1D;
import com.opengamma.math.minimization.ConjugateDirectionVectorMinimizer;
import com.opengamma.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform;
import com.opengamma.math.minimization.ScalarMinimizer;
import com.opengamma.math.minimization.SingleRangeLimitTransform;
import com.opengamma.math.minimization.TransformParameters;
import com.opengamma.math.minimization.VectorMinimizer;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.rootfinding.VanWijngaardenDekkerBrentSingleRootFinder;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;

/**
 * 
 */
public class SABRFitter {

  private static final NonLinearLeastSquare SOLVER = new NonLinearLeastSquare();

  private static final int N_PARAMETERS = 4;
  private final SABRFormula _formula;

  private static final ParameterLimitsTransform[] TRANSFORMS;

  static {
    TRANSFORMS = new ParameterLimitsTransform[4];
    TRANSFORMS[0] = new SingleRangeLimitTransform(0, true); // alpha > 0
    TRANSFORMS[1] = new DoubleRangeLimitTransform(0, 2.0); // 0 <= beta <= 2
    TRANSFORMS[2] = new SingleRangeLimitTransform(0, true); // nu > 0
    TRANSFORMS[3] = new DoubleRangeLimitTransform(-1.0, 1.0); // -1 <= rho <= 1
    // TRANSFORMS[0] = new NullTransform();
    // TRANSFORMS[1] = new NullTransform();
    // TRANSFORMS[2] = new NullTransform();
    // TRANSFORMS[3] = new DoubleRangeLimitTransform(-1, 1); // -1 <= rho <= 1
  }

  public SABRFitter(SABRFormula formula) {
    _formula = formula;
  }

  @SuppressWarnings("synthetic-access")
  public LeastSquareResults solve(final double forward, final double maturity, final double[] strikes, final double[] blackVols, final double[] errors, final double[] initialValues,
      final BitArray fixed, final double atmVol, final boolean recoverATMVol) {

    final SABRATMVolSolver atmSover = new SABRATMVolSolver(_formula);
    if (recoverATMVol) {
      Validate.isTrue(atmVol > 0.0, "ATM  must be > 0");
      fixed.set(0, true);
    }

    int n = strikes.length;
    Validate.isTrue(n == blackVols.length, "strikes and vols must be same length");
    Validate.isTrue(n == errors.length, "errors and vols must be same length");

    final TransformParameters transforms = new TransformParameters(new DoubleMatrix1D(initialValues), TRANSFORMS, fixed);

    final ParameterizedFunction<Double, DoubleMatrix1D, Double> function = new ParameterizedFunction<Double, DoubleMatrix1D, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(Double strike, DoubleMatrix1D fp) {
        DoubleMatrix1D mp = transforms.inverseTransform(fp);
        double alpha = mp.getEntry(0);
        final double beta = mp.getEntry(1);
        final double nu = mp.getEntry(2);
        final double rho = mp.getEntry(3);
        if (recoverATMVol) {
          alpha = atmSover.solve(forward, maturity, atmVol, beta, nu, rho);
        }
        return _formula.impliedVolatility(forward, alpha, beta, nu, rho, strike, maturity);
      }
    };

    DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialValues));
    LeastSquareResults lsRes = SOLVER.solve(new DoubleMatrix1D(strikes), new DoubleMatrix1D(blackVols), new DoubleMatrix1D(errors), function, fp);
    DoubleMatrix1D mp = transforms.inverseTransform(lsRes.getParameters());
    if (recoverATMVol) {
      final double beta = mp.getEntry(1);
      final double nu = mp.getEntry(2);
      final double rho = mp.getEntry(3);
      mp.getData()[0] = atmSover.solve(forward, maturity, atmVol, beta, nu, rho);
    }

    return new LeastSquareResults(lsRes.getChiSq(), mp, new DoubleMatrix2D(new double[N_PARAMETERS][N_PARAMETERS]));
  }

  public LeastSquareResults solveByCG(final double forward, final double maturity, final double[] strikes, final double[] blackVols, final double[] errors, final double[] initialValues,
      final BitArray fixed) {

    final int n = strikes.length;
    Validate.isTrue(n == blackVols.length, "strikes and vols must be same length");
    Validate.isTrue(n == errors.length, "errors and vols must be same length");

    final TransformParameters transforms = new TransformParameters(new DoubleMatrix1D(initialValues), TRANSFORMS, fixed);
    final Function1D<DoubleMatrix1D, Double> function = new Function1D<DoubleMatrix1D, Double>() {

      @Override
      public Double evaluate(DoubleMatrix1D fp) {
        DoubleMatrix1D mp = transforms.inverseTransform(fp);
        final double alpha = mp.getEntry(0);
        final double beta = mp.getEntry(1);
        final double nu = mp.getEntry(2);
        final double rho = mp.getEntry(3);
        double chiSqr = 0;
        for (int i = 0; i < n; i++) {
          chiSqr += UtilFunctions.square((blackVols[i] - _formula.impliedVolatility(forward, alpha, beta, nu, rho, strikes[i], maturity)) / errors[i]);
        }

        return chiSqr;
        // final double beta1 = 1 - beta;
        // final double f1 = Math.pow(forward, beta1);
        // double x = (beta1 * beta1 * alpha * alpha / 24 / f1 / f1 + rho * alpha * beta * nu / 4 / f1 + nu * nu * (2 - 3 * rho * rho) / 24);
        // x = Math.max(0, -x);
        // double lambda = 1000.0;
        // return chiSqr + lambda * x * x;
      }
    };
    ScalarMinimizer lineMinimizer = new BrentMinimizer1D();
    VectorMinimizer minimzer = new ConjugateDirectionVectorMinimizer(lineMinimizer, 1e-6, 10000);
    DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialValues));
    DoubleMatrix1D minPos = minimzer.minimize(function, fp);
    double chiSquare = function.evaluate(minPos);
    DoubleMatrix1D res = transforms.inverseTransform(minPos);
    return new LeastSquareResults(chiSquare, res, new DoubleMatrix2D(new double[N_PARAMETERS][N_PARAMETERS]));

  }

  private final class SABRATMVolSolver {
    @SuppressWarnings("hiding")
    private final SABRFormula _formula;
    private final BracketRoot _bracketer = new BracketRoot();
    private final RealSingleRootFinder _rootFinder = new VanWijngaardenDekkerBrentSingleRootFinder();

    private SABRATMVolSolver(SABRFormula formula) {
      _formula = formula;
    }

    double solve(final double forward, final double maturity, final double atmVol, final double beta, final double nu, final double rho) {

      Function1D<Double, Double> f = new Function1D<Double, Double>() {
        @SuppressWarnings("synthetic-access")
        @Override
        public Double evaluate(Double alpha) {
          return _formula.impliedVolatility(forward, alpha, beta, nu, rho, forward, maturity) - atmVol;
        }
      };

      double alphaTry = atmVol * Math.pow(forward, 1 - beta);
      final double[] range = _bracketer.getBracketedPoints(f, alphaTry / 2.0, 2 * alphaTry);
      return _rootFinder.getRoot(f, range[0], range[1]);
    }

  }

}
