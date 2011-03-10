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
import com.opengamma.financial.model.option.pricing.fourier.CharacteristicExponent1;
import com.opengamma.financial.model.option.pricing.fourier.FourierPricer1;
import com.opengamma.financial.model.option.pricing.fourier.HestonCharacteristicExponent1;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.function.ParameterizedFunction;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.minimization.DoubleRangeLimitTransform;
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
public class HestonFourierNonLinearLeastSquareFitter extends LeastSquareSmileFitter {
  private static final double DEFAULT_ALPHA = -0.5;
  private static final double DEFAULT_LIMIT_TOLERANCE = 1e-8;
  private static final NonLinearLeastSquare SOLVER = new NonLinearLeastSquare();
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL_FORMULA = new BlackImpliedVolatilityFormula();
  private static final FourierPricer1 FOURIER_PRICER = new FourierPricer1();
  private static final int N_PARAMETERS = 5;
  private static final ParameterLimitsTransform[] TRANSFORMS;
  private final double _alpha;
  private final double _limitTolerance;
  static {
    TRANSFORMS = new ParameterLimitsTransform[N_PARAMETERS];
    TRANSFORMS[0] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // kappa > 0
    TRANSFORMS[1] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // theta > 0
    TRANSFORMS[2] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // vol0 > 0
    TRANSFORMS[3] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // omega > 0
    TRANSFORMS[4] = new DoubleRangeLimitTransform(-1.0, 1.0); // -1 <= rho <= 1
  }

  public HestonFourierNonLinearLeastSquareFitter() {
    this(Interpolator1DFactory.getInterpolator("DoubleQuadratic"), DEFAULT_ALPHA, DEFAULT_LIMIT_TOLERANCE);
  }

  public HestonFourierNonLinearLeastSquareFitter(final Interpolator1D<Interpolator1DDataBundle> interpolator) {
    this(interpolator, DEFAULT_ALPHA, DEFAULT_LIMIT_TOLERANCE);
  }

  public HestonFourierNonLinearLeastSquareFitter(final Interpolator1D<Interpolator1DDataBundle> interpolator, final double alpha, final double limitTolerance) {
    Validate.notNull(interpolator, "interpolator");
    Validate.isTrue(alpha != 0 && alpha != -1, "Alpha cannot be 0 or -1");
    Validate.isTrue(limitTolerance > 0, "limit tolerance must be > 0");
    _alpha = alpha;
    _limitTolerance = limitTolerance;
  }

  @Override
  public LeastSquareResults getFitResult(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] initialFitParameters, final BitSet fixed) {
    return getFitResult(options, data, null, initialFitParameters, fixed);
  }

  @Override
  public LeastSquareResults getFitResult(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] errors, final double[] initialFitParameters, final BitSet fixed) {
    testData(options, data, errors, initialFitParameters, fixed, N_PARAMETERS);
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
    final TransformParameters transforms = new TransformParameters(new DoubleMatrix1D(initialFitParameters), TRANSFORMS, fixed);
    final double limitSigma = (blackVols[0] + blackVols[blackVols.length - 1]) / 2.0;
    final BlackFunctionData blackData = new BlackFunctionData(forward, 1, limitSigma);
    final ParameterizedFunction<Double, DoubleMatrix1D, Double> function = new ParameterizedFunction<Double, DoubleMatrix1D, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double strike, final DoubleMatrix1D fp) {
        final DoubleMatrix1D mp = transforms.inverseTransform(fp);
        final double kappa = mp.getEntry(0);
        final double theta = mp.getEntry(1);
        final double vol0 = mp.getEntry(2);
        final double omega = mp.getEntry(3);
        final double rho = mp.getEntry(4);
        final CharacteristicExponent1 ce = new HestonCharacteristicExponent1(kappa, theta, vol0, omega, rho);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, maturity, true);
        final double price = FOURIER_PRICER.priceFromVol(blackData, option, ce, _alpha, _limitTolerance, true);
        final double vol = BLACK_IMPLIED_VOL_FORMULA.getImpliedVolatility(blackData, option, price);
        return vol;
      }
    };

    final DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialFitParameters));
    final LeastSquareResults results = errors == null ? SOLVER.solve(new DoubleMatrix1D(strikes), new DoubleMatrix1D(blackVols), function, fp) : SOLVER.solve(new DoubleMatrix1D(strikes),
        new DoubleMatrix1D(blackVols), new DoubleMatrix1D(errors), function, fp);
    return new LeastSquareResults(results.getChiSq(), transforms.inverseTransform(results.getParameters()), new DoubleMatrix2D(new double[N_PARAMETERS][N_PARAMETERS]));
  }

}
