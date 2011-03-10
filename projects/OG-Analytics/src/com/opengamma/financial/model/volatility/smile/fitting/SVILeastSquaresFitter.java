/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.smile.function.SVIFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SVIVolatilityFunction;
import com.opengamma.math.function.ParameterizedFunction;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.minimization.NullTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.math.minimization.SingleRangeLimitTransform;
import com.opengamma.math.minimization.TransformParameters;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class SVILeastSquaresFitter implements LeastSquareSmileFitter<BlackFunctionData> {
  private static final NonLinearLeastSquare SOLVER = new NonLinearLeastSquare();
  private static final int N_PARAMETERS = 5;
  private static final ParameterLimitsTransform[] TRANSFORMS;
  private static final SVIVolatilityFunction FORMULA = new SVIVolatilityFunction();

  static {
    TRANSFORMS = new ParameterLimitsTransform[N_PARAMETERS];
    TRANSFORMS[0] = new NullTransform();
    TRANSFORMS[1] = new NullTransform();
    TRANSFORMS[2] = new NullTransform();
    TRANSFORMS[3] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN);
    TRANSFORMS[4] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN);
  }

  @Override
  public LeastSquareResults getFitResult(final EuropeanVanillaOption[] options, final BlackFunctionData data, final double[] blackVols, final double[] errors, final double[] initialValues,
      final BitSet fixed) {
    Validate.notEmpty(options, "options");
    final int n = options.length;
    Validate.notNull(data, "data");
    Validate.notNull(blackVols, "black volatility data");
    Validate.isTrue(blackVols.length == n, "Black volatility data array must be the same length as the option array");
    Validate.notNull(errors, "volatility error data");
    Validate.isTrue(errors.length == n, "Volatility error data array must be the same length as the option array");
    Validate.notNull(initialValues, "initial values");
    Validate.isTrue(initialValues.length == N_PARAMETERS, "must have length of initial values array equal to number of parameters");
    Validate.notNull(fixed, "fixed");
    final double[] strikes = new double[n];
    final double maturity = options[0].getTimeToExpiry();
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
        final double a = mp.getEntry(0);
        final double b = mp.getEntry(1);
        final double rho = mp.getEntry(2);
        final double sigma = mp.getEntry(3);
        final double m = mp.getEntry(4);
        final SVIFormulaData newData = new SVIFormulaData(a, b, rho, sigma, m);
        return FORMULA.getVolatilityFunction(new EuropeanVanillaOption(strike, maturity, true)).evaluate(newData);
      }
    };

    final DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialValues));
    final LeastSquareResults lsRes = SOLVER.solve(new DoubleMatrix1D(strikes), new DoubleMatrix1D(blackVols), new DoubleMatrix1D(errors), function, fp);
    final DoubleMatrix1D mp = transforms.inverseTransform(lsRes.getParameters());
    return new LeastSquareResults(lsRes.getChiSq(), mp, new DoubleMatrix2D(new double[N_PARAMETERS][N_PARAMETERS]));

  }
}
