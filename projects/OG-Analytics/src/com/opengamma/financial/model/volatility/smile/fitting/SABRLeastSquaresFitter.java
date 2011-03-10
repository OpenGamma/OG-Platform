/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.math.FunctionUtils;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.ParameterizedFunction;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.minimization.BrentMinimizer1D;
import com.opengamma.math.minimization.ConjugateDirectionVectorMinimizer;
import com.opengamma.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.math.minimization.ScalarMinimizer;
import com.opengamma.math.minimization.SingleRangeLimitTransform;
import com.opengamma.math.minimization.TransformParameters;
import com.opengamma.math.minimization.VectorMinimizer;
import com.opengamma.math.rootfinding.BracketRoot;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.rootfinding.VanWijngaardenDekkerBrentSingleRootFinder;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class SABRLeastSquaresFitter implements LeastSquareSmileFitter<SABRFormulaData> {
  private static final NonLinearLeastSquare SOLVER = new NonLinearLeastSquare();
  private static final int N_PARAMETERS = 4;
  private static final ParameterLimitsTransform[] TRANSFORMS;

  static {
    TRANSFORMS = new ParameterLimitsTransform[4];
    TRANSFORMS[0] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // alpha > 0
    TRANSFORMS[1] = new DoubleRangeLimitTransform(0, 2.0); // 0 <= beta <= 2
    TRANSFORMS[2] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // nu > 0
    TRANSFORMS[3] = new DoubleRangeLimitTransform(-1.0, 1.0); // -1 <= rho <= 1
  }

  private final VolatilityFunctionProvider<SABRFormulaData> _formula;
  private final SABRATMVolSolver _atmSolver;

  @SuppressWarnings("synthetic-access")
  public SABRLeastSquaresFitter(final VolatilityFunctionProvider<SABRFormulaData> formula) {
    _formula = formula;
    _atmSolver = new SABRATMVolSolver(_formula);
  }

  @Override
  public LeastSquareResults getFitResult(final EuropeanVanillaOption[] options, final SABRFormulaData sabrModelData, final double[] blackVols, final double[] errors, final double[] initialValues,
      final BitSet fixed) {
    return solve(options, sabrModelData, blackVols, errors, initialValues, fixed, 0, false);
  }

  public LeastSquareResults solve(final EuropeanVanillaOption[] options, final SABRFormulaData sabrModelData, final double[] blackVols, final double[] errors, final double[] initialValues,
      final BitSet fixed, final double atmVol, final boolean recoverATMVol) {
    Validate.notEmpty(options, "options");
    final int n = options.length;
    Validate.notNull(sabrModelData, "SABR model data");
    Validate.notNull(initialValues, "initial values");
    Validate.isTrue(initialValues.length == N_PARAMETERS, "must have length of initial values array equal to number of parameters");
    Validate.notNull(fixed, "fixed");
    if (recoverATMVol) {
      Validate.isTrue(atmVol > 0.0, "ATM volatility must be > 0");
      fixed.set(0, true);
    }
    final double[] strikes = new double[n];
    final double maturity = options[0].getTimeToExpiry();
    final double forward = sabrModelData.getForward();
    strikes[0] = options[0].getStrike();
    for (int i = 1; i < n; i++) {
      Validate.isTrue(CompareUtils.closeEquals(options[i].getTimeToExpiry(), maturity), "All options must have the same maturity " + maturity + "; have one with maturity " + options[i].getTimeToExpiry());
      strikes[i] = options[i].getStrike();
    }
    final TransformParameters transforms = new TransformParameters(new DoubleMatrix1D(initialValues), TRANSFORMS, fixed);

    final ParameterizedFunction<Double, DoubleMatrix1D, Double> function = new ParameterizedFunction<Double, DoubleMatrix1D, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double strike, final DoubleMatrix1D fp) {
        final DoubleMatrix1D mp = transforms.inverseTransform(fp);
        double alpha = mp.getEntry(0);
        final double beta = mp.getEntry(1);
        final double nu = mp.getEntry(2);
        final double rho = mp.getEntry(3);
        final SABRFormulaData data;
        if (recoverATMVol) {
          final EuropeanVanillaOption atmOption = new EuropeanVanillaOption(forward, maturity, true);
          alpha = _atmSolver.solve(new SABRFormulaData(forward, alpha, beta, nu, rho), atmOption, atmVol);
          data = new SABRFormulaData(forward, alpha, beta, nu, rho);
        } else {
          data = new SABRFormulaData(forward, alpha, beta, nu, rho);
        }
        final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, maturity, true);
        return _formula.getVolatilityFunction(option).evaluate(data);
      }
    };

    final DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialValues));
    final LeastSquareResults lsRes = SOLVER.solve(new DoubleMatrix1D(strikes), new DoubleMatrix1D(blackVols), new DoubleMatrix1D(errors), function, fp);
    final double[] mp = transforms.inverseTransform(lsRes.getParameters()).toArray();
    if (recoverATMVol) {
      final double beta = mp[1];
      final double nu = mp[2];
      final double rho = mp[3];
      final EuropeanVanillaOption option = new EuropeanVanillaOption(forward, maturity, true);
      final SABRFormulaData data = new SABRFormulaData(forward, mp[0], beta, nu, rho);
      final double value = _atmSolver.solve(data, option, atmVol);
      mp[0] = value;
    }
    return new LeastSquareResults(lsRes.getChiSq(), new DoubleMatrix1D(mp), new DoubleMatrix2D(new double[N_PARAMETERS][N_PARAMETERS]));
  }

  public LeastSquareResults solveByConjugateGradient(final EuropeanVanillaOption[] options, final SABRFormulaData sabrModelData, final double[] blackVols, final double[] errors,
      final double[] initialValues, final BitSet fixed) {
    Validate.notEmpty(options, "options");
    final int n = options.length;
    Validate.notNull(sabrModelData, "SABR model data");
    Validate.notNull(initialValues, "initial values");
    Validate.isTrue(initialValues.length == N_PARAMETERS, "must have length of initial values array equal to number of parameters");
    Validate.notNull(fixed, "fixed");

    final double forward = sabrModelData.getForward();
    final TransformParameters transforms = new TransformParameters(new DoubleMatrix1D(initialValues), TRANSFORMS, fixed);
    final Function1D<DoubleMatrix1D, Double> function = new Function1D<DoubleMatrix1D, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final DoubleMatrix1D fp) {
        final DoubleMatrix1D mp = transforms.inverseTransform(fp);
        final double alpha = mp.getEntry(0);
        final double beta = mp.getEntry(1);
        final double nu = mp.getEntry(2);
        final double rho = mp.getEntry(3);
        double chiSqr = 0;
        final SABRFormulaData data = new SABRFormulaData(forward, alpha, beta, nu, rho);
        for (int i = 0; i < n; i++) {
          chiSqr += FunctionUtils.square((blackVols[i] - _formula.getVolatilityFunction(options[i]).evaluate(data)) / errors[i]);
        }

        return chiSqr;
      }
    };
    final ScalarMinimizer lineMinimizer = new BrentMinimizer1D();
    final VectorMinimizer minimzer = new ConjugateDirectionVectorMinimizer(lineMinimizer, 1e-6, 10000);
    final DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialValues));
    final DoubleMatrix1D minPos = minimzer.minimize(function, fp);
    final double chiSquare = function.evaluate(minPos);
    final DoubleMatrix1D res = transforms.inverseTransform(minPos);
    return new LeastSquareResults(chiSquare, res, new DoubleMatrix2D(new double[N_PARAMETERS][N_PARAMETERS]));

  }

  private final class SABRATMVolSolver {
    private final VolatilityFunctionProvider<SABRFormulaData> _sabrFormula;
    private final BracketRoot _bracketer = new BracketRoot();
    private final RealSingleRootFinder _rootFinder = new VanWijngaardenDekkerBrentSingleRootFinder();

    private SABRATMVolSolver(final VolatilityFunctionProvider<SABRFormulaData> formula) {
      _sabrFormula = formula;
    }

    double solve(final SABRFormulaData data, final EuropeanVanillaOption option, final double atmVol) {
      Validate.notNull(data, "data");
      final Function1D<Double, Double> f = new Function1D<Double, Double>() {

        @SuppressWarnings("synthetic-access")
        @Override
        public Double evaluate(final Double alpha) {
          final SABRFormulaData newData = new SABRFormulaData(data.getForward(), alpha, data.getBeta(), data.getNu(), data.getRho());
          return _sabrFormula.getVolatilityFunction(option).evaluate(newData) - atmVol;
        }
      };
      final double alphaTry = atmVol * Math.pow(data.getForward(), 1 - data.getBeta());
      final double[] range = _bracketer.getBracketedPoints(f, alphaTry / 2.0, 2 * alphaTry);
      return _rootFinder.getRoot(f, range[0], range[1]);
    }

  }

}
