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
import com.opengamma.financial.model.option.pricing.fourier.FFTPricer;
import com.opengamma.financial.model.option.pricing.fourier.HestonCharacteristicExponent;
import com.opengamma.financial.model.option.pricing.fourier.MartingaleCharacteristicExponent;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.MatrixAlgebraFactory;
import com.opengamma.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.math.minimization.SingleRangeLimitTransform;
import com.opengamma.math.minimization.UncoupledParameterTransforms;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;

/**
 * 
 */
public class HestonFFTSmileFitter extends LeastSquareSmileFitter {
  private static final double DEFAULT_ALPHA = -0.5;
  private static final double DEFAULT_LIMIT_TOLERANCE = 1e-12;
  static final NonLinearLeastSquare SOLVER = new NonLinearLeastSquare(DecompositionFactory.SV_COLT, MatrixAlgebraFactory.OG_ALGEBRA, 1e-4);
  static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL_FORMULA = new BlackImpliedVolatilityFormula();
  static final FFTPricer FFT_PRICER = new FFTPricer();

  private final int _nParams;
  private final ParameterLimitsTransform[] _transforms;
  private final Interpolator1D _interpolator;
  private final double _alpha;
  private final double _limitTolerance;

  /**
   * @param fixVol0 True if initial value of vol the same as mean reversion level 
   */
  public HestonFFTSmileFitter(boolean fixVol0) {
    this(Interpolator1DFactory.getInterpolator("DoubleQuadratic"), DEFAULT_ALPHA, DEFAULT_LIMIT_TOLERANCE, fixVol0);
  }

  public HestonFFTSmileFitter(final Interpolator1D interpolator, boolean fixVol0) {
    this(interpolator, DEFAULT_ALPHA, DEFAULT_LIMIT_TOLERANCE, fixVol0);
  }

  public HestonFFTSmileFitter(final Interpolator1D interpolator, final double alpha, final double limitTolerance, boolean fixVol0) {
    Validate.notNull(interpolator, "interpolator");
    Validate.isTrue(alpha != 0 && alpha != -1, "alpha cannot be 0 or -1");
    Validate.isTrue(limitTolerance > 0, "limit tolerance must be > 0");
    _interpolator = interpolator;
    _alpha = alpha;
    _limitTolerance = limitTolerance;

    _nParams = (fixVol0 ? 4 : 5);
    _transforms = new ParameterLimitsTransform[_nParams];
    _transforms[0] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // kappa > 0
    _transforms[1] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // theta > 0
    if (fixVol0) {
      _transforms[2] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // omega > 0
      _transforms[3] = new DoubleRangeLimitTransform(-1.0, 1.0); // -1 <= rho <= 1
    } else {
      _transforms[2] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // vol0 > 0
      _transforms[3] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // omega > 0
      _transforms[4] = new DoubleRangeLimitTransform(-1.0, 1.0); // -1 <= rho <= 1
    }
  }

  @Override
  public LeastSquareResults getFitResult(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] initialFitParameters, final BitSet fixed) {
    return getFitResult(options, data, null, initialFitParameters, fixed);
  }

  @Override
  public LeastSquareResults getFitResult(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] errors, final double[] initialFitParameters, final BitSet fixed) {
    testData(options, data, errors, initialFitParameters, fixed, _nParams);
    final int n = options.length;
    final double[] strikes = new double[n];
    final double[] blackVols = new double[n];
    final double maturity = options[0].getTimeToExpiry();
    final double forward = data[0].getForward();

    for (int i = 0; i < n; i++) {
      strikes[i] = options[i].getStrike();
      blackVols[i] = data[i].getBlackVolatility();
    }

    final double limitSigma = (blackVols[0] + blackVols[n - 1]) / 2.0;
    final double lowestStrike = strikes[0];
    final double highestStrike = strikes[n - 1];
    final UncoupledParameterTransforms transforms = new UncoupledParameterTransforms(new DoubleMatrix1D(initialFitParameters), _transforms, fixed);

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> hestonVols = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D fp) {
        final MartingaleCharacteristicExponent ce = getCharacteristicExponent(transforms, fp);
        final double[][] strikeNPrice = FFT_PRICER.price(forward, 1.0, maturity, true, ce, lowestStrike, highestStrike, n, limitSigma, _alpha, _limitTolerance);
        final int nStrikes = strikeNPrice.length;
        final double[] k = new double[nStrikes];
        final double[] vol = new double[nStrikes];
        for (int i = 0; i < nStrikes; i++) {
          k[i] = strikeNPrice[i][0];
          try {
            //TODO have implied vol interface which mean not having to create new objects inside loops 
            vol[i] = BLACK_IMPLIED_VOL_FORMULA.getImpliedVolatility(new BlackFunctionData(forward, 1.0, 0.0), new EuropeanVanillaOption(k[i], maturity, true), strikeNPrice[i][1]);
          } catch (Exception e) {
            vol[i] = 0.0;
          }
        }
        final Interpolator1DDataBundle dataBundle = _interpolator.getDataBundleFromSortedArrays(k, vol);
        final double[] res = new double[n];
        for (int i = 0; i < n; i++) {
          res[i] = _interpolator.interpolate(dataBundle, strikes[i]);
        }
        return new DoubleMatrix1D(res);
      }
    };
    final DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialFitParameters));
    final LeastSquareResults results = errors == null ? SOLVER.solve(new DoubleMatrix1D(blackVols), hestonVols, fp) : SOLVER.solve(new DoubleMatrix1D(blackVols), new DoubleMatrix1D(errors),
        hestonVols, fp);
    return new LeastSquareResults(results.getChiSq(), transforms.inverseTransform(results.getParameters()), new DoubleMatrix2D(new double[_nParams][_nParams]));
  }

  /**
   * Number of fit parameters
   * @return the nParams
   */
  protected int getnParams() {
    return _nParams;
  }

  /**
   * Gets the transforms.
   * @return the transforms
   */
  protected ParameterLimitsTransform[] getTransforms() {
    return _transforms;
  }

  /**
   * Gets the interpolator.
   * @return the interpolator
   */
  protected Interpolator1D getInterpolator() {
    return _interpolator;
  }

  /**
   * Gets the alpha.
   * @return the alpha
   */
  protected double getAlpha() {
    return _alpha;
  }

  /**
   * Gets the limitTolerance.
   * @return the limitTolerance
   */
  protected double getLimitTolerance() {
    return _limitTolerance;
  }

  /**
   * @param transforms
   * @param fp
   * @return
   */
  MartingaleCharacteristicExponent getCharacteristicExponent(final UncoupledParameterTransforms transforms, final DoubleMatrix1D fp) {
    final DoubleMatrix1D mp = transforms.inverseTransform(fp);
    final double kappa = mp.getEntry(0);
    final double theta = mp.getEntry(1);
    final double vol0;
    final double omega;
    final double rho;
    if (mp.getNumberOfElements() == 5) {
      vol0 = mp.getEntry(2);
      omega = mp.getEntry(3);
      rho = mp.getEntry(4);
    } else {
      vol0 = theta;
      omega = mp.getEntry(2);
      rho = mp.getEntry(3);
    }

    final MartingaleCharacteristicExponent ce = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
    return ce;
  }
}
