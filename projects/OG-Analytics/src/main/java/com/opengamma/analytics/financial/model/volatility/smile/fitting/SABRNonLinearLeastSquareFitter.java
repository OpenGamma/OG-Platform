/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.function.ParameterizedFunction;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.MatrixAlgebraFactory;
import com.opengamma.analytics.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.UncoupledParameterTransforms;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.util.CompareUtils;

/**
 * @deprecated Please use SABRModelFitter
 */
@Deprecated
public class SABRNonLinearLeastSquareFitter extends LeastSquareSmileFitter {
  private static final NonLinearLeastSquare SOLVER = new NonLinearLeastSquare(DecompositionFactory.SV_COLT, MatrixAlgebraFactory.OG_ALGEBRA, 1e-4);
  private static final int N_PARAMETERS = 4;
  private static final ParameterLimitsTransform[] TRANSFORMS;

  static {
    TRANSFORMS = new ParameterLimitsTransform[4];
    TRANSFORMS[0] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // alpha > 0
    TRANSFORMS[1] = new DoubleRangeLimitTransform(0, 2.0); // 0 <= beta <= 2
    TRANSFORMS[2] = new DoubleRangeLimitTransform(-1.0, 1.0); // -1 <= rho <= 1
    TRANSFORMS[3] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // nu > 0

  }
  private final VolatilityFunctionProvider<SABRFormulaData> _formula;
  private final SABRATMVolatilityCalculator _atmCalculator;

  public static NonLinearLeastSquare getSolver() {
    return SOLVER;
  }

  public SABRNonLinearLeastSquareFitter(final VolatilityFunctionProvider<SABRFormulaData> formula) {
    Validate.notNull(formula, "SABR formula");
    _formula = formula;
    _atmCalculator = new SABRATMVolatilityCalculator(formula);
  }

  @Override
  public LeastSquareResultsWithTransform getFitResult(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] initialFitParameters, final BitSet fixed) {
    return getFitResult(options, data, initialFitParameters, fixed, 0, false);
  }

  @Override
  public LeastSquareResultsWithTransform getFitResult(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] errors, final double[] initialFitParameters,
      final BitSet fixed) {
    return getFitResult(options, data, errors, initialFitParameters, fixed, 0, false);
  }

  public LeastSquareResultsWithTransform getFitResult(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] initialFitParameters, final BitSet fixed,
      final double atmVol, final boolean recoverATMVol) {
    return getFitResult(options, data, null, initialFitParameters, fixed, atmVol, recoverATMVol);
  }

  public LeastSquareResultsWithTransform getFitResult(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] errors, final double[] initialFitParameters,
      final BitSet fixed, final double atmVol, final boolean recoverATMVol) {
    testData(options, data, errors, initialFitParameters, fixed, N_PARAMETERS);
    if (recoverATMVol) {
      Validate.isTrue(atmVol > 0.0, "ATM volatility must be > 0");
      fixed.set(0, true);
    }
    final int n = options.length;
    final double[] strikes = new double[n];
    final double[] blackVols = new double[n];
    final double maturity = options[0].getTimeToExpiry();
    final double forward = data[0].getForward();
    strikes[0] = options[0].getStrike();
    blackVols[0] = data[0].getBlackVolatility();
    for (int i = 1; i < n; i++) {
      Validate.isTrue(CompareUtils.closeEquals(options[i].getTimeToExpiry(), maturity),
          "All options must have the same maturity " + maturity + "; have one with maturity " + options[i].getTimeToExpiry());
      strikes[i] = options[i].getStrike();
      blackVols[i] = data[i].getBlackVolatility();
    }
    final UncoupledParameterTransforms transforms = new UncoupledParameterTransforms(new DoubleMatrix1D(initialFitParameters), TRANSFORMS, fixed);
    final EuropeanVanillaOption atmOption = new EuropeanVanillaOption(forward, maturity, true);
    final ParameterizedFunction<Double, DoubleMatrix1D, Double> function = new ParameterizedFunction<Double, DoubleMatrix1D, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double strike, final DoubleMatrix1D fp) {
        final DoubleMatrix1D mp = transforms.inverseTransform(fp);
        double alpha = mp.getEntry(0);
        final double beta = mp.getEntry(1);
        final double rho = mp.getEntry(2);
        final double nu = mp.getEntry(3);

        final SABRFormulaData sabrFormulaData;
        if (recoverATMVol) {
          alpha = _atmCalculator.calculate(new SABRFormulaData(alpha, beta, rho, nu), atmOption, forward, atmVol);
          sabrFormulaData = new SABRFormulaData(alpha, beta, rho, nu);
        } else {
          sabrFormulaData = new SABRFormulaData(alpha, beta, rho, nu);
        }
        final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, maturity, true);
        return _formula.getVolatilityFunction(option, forward).evaluate(sabrFormulaData);
      }

      @Override
      public int getNumberOfParameters() {
        return 4;
      }
    };

    final DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialFitParameters));
    LeastSquareResults lsRes = errors == null ? SOLVER.solve(new DoubleMatrix1D(strikes), new DoubleMatrix1D(blackVols), function, fp) : SOLVER.solve(new DoubleMatrix1D(strikes), new DoubleMatrix1D(
        blackVols), new DoubleMatrix1D(errors), function, fp);
    final double[] mp = transforms.inverseTransform(lsRes.getFitParameters()).toArray();
    if (recoverATMVol) {
      final double beta = mp[1];
      final double nu = mp[2];
      final double rho = mp[3];
      final EuropeanVanillaOption option = new EuropeanVanillaOption(forward, maturity, true);
      final SABRFormulaData sabrFormulaData = new SABRFormulaData(mp[0], beta, rho, nu);
      final double value = _atmCalculator.calculate(sabrFormulaData, option, forward, atmVol);
      mp[0] = value;
      lsRes = new LeastSquareResults(lsRes.getChiSq(), new DoubleMatrix1D(mp), lsRes.getCovariance());
    }
    return new LeastSquareResultsWithTransform(lsRes, transforms);
    //return new LeastSquareResults(lsRes.getChiSq(), new DoubleMatrix1D(mp), new DoubleMatrix2D(new double[N_PARAMETERS][N_PARAMETERS]), lsRes.getFittingParameterSensitivityToData());
  }
}
