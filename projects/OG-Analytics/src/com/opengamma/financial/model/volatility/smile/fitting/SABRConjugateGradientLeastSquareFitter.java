/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.math.FunctionUtils;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.minimization.BrentMinimizer1D;
import com.opengamma.math.minimization.ConjugateDirectionVectorMinimizer;
import com.opengamma.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.math.minimization.ScalarMinimizer;
import com.opengamma.math.minimization.SingleRangeLimitTransform;
import com.opengamma.math.minimization.UncoupledParameterTransforms;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class SABRConjugateGradientLeastSquareFitter extends LeastSquareSmileFitter {
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

  public SABRConjugateGradientLeastSquareFitter(final VolatilityFunctionProvider<SABRFormulaData> formula) {
    Validate.notNull(formula, "SABR formula");
    _formula = formula;
  }

  @Override
  public LeastSquareResults getFitResult(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] initialFitParameters, final BitSet fixed) {
    throw new UnsupportedOperationException("Cannot calculate SABR parameters using conjugate gradient method without error estimates for the black volatilities");
  }

  @Override
  public LeastSquareResults getFitResult(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] errors, final double[] initialFitParameters, final BitSet fixed) {
    testData(options, data, errors, initialFitParameters, fixed, N_PARAMETERS);
    final int n = options.length;
    final double forward = data[0].getForward();
    final double maturity = options[0].getTimeToExpiry();
    for (int i = 1; i < n; i++) {
      Validate.isTrue(CompareUtils.closeEquals(options[i].getTimeToExpiry(), maturity),
          "All options must have the same maturity " + maturity + "; have one with maturity " + options[i].getTimeToExpiry());
    }
    final UncoupledParameterTransforms transforms = new UncoupledParameterTransforms(new DoubleMatrix1D(initialFitParameters), TRANSFORMS, fixed);
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
        final SABRFormulaData sabrFormulaData = new SABRFormulaData(alpha, beta, rho, nu);
        for (int i = 0; i < n; i++) {
          chiSqr += FunctionUtils.square((data[i].getBlackVolatility() - _formula.getVolatilityFunction(options[i], forward).evaluate(sabrFormulaData)) / errors[i]);
        }
        return chiSqr;
      }
    };
    final ScalarMinimizer lineMinimizer = new BrentMinimizer1D();
    final ConjugateDirectionVectorMinimizer minimzer = new ConjugateDirectionVectorMinimizer(lineMinimizer, 1e-6, 10000);
    final DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialFitParameters));
    final DoubleMatrix1D minPos = minimzer.minimize(function, fp);
    final double chiSquare = function.evaluate(minPos);
    final DoubleMatrix1D res = transforms.inverseTransform(minPos);
    return new LeastSquareResults(chiSquare, res, new DoubleMatrix2D(new double[N_PARAMETERS][N_PARAMETERS]));
  }

  //TODO add method that recovers ATM vol
}
