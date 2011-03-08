/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import java.util.BitSet;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.function.Function1D;
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

/**
 * 
 */
public class HestonFitter1 {
  private static final double DEFAULT_ALPHA = -0.5;
  private static final double DEFAULT_LIMIT_TOLERANCE = 1e-8;
  private static final NonLinearLeastSquare SOLVER = new NonLinearLeastSquare();
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL_FORMULA = new BlackImpliedVolatilityFormula();
  private static final BlackPriceFunction BLACK_PRICE_FUNCTION = new BlackPriceFunction();
  private static final FFTPricer1 FFT_PRICER = new FFTPricer1();
  private static final FourierPricer1 FOURIER_PRICER = new FourierPricer1();
  private static final int N_PARAMETERS = 5;
  private static final ParameterLimitsTransform[] TRANSFORMS;
  private final Interpolator1D<Interpolator1DDataBundle> _interpolator;
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

  public HestonFitter1() {
    this(Interpolator1DFactory.getInterpolator("DoubleQuadratic"), DEFAULT_ALPHA, DEFAULT_LIMIT_TOLERANCE);
  }

  public HestonFitter1(final Interpolator1D<Interpolator1DDataBundle> interpolator) {
    this(interpolator, DEFAULT_ALPHA, DEFAULT_LIMIT_TOLERANCE);
  }

  public HestonFitter1(final Interpolator1D<Interpolator1DDataBundle> interpolator, final double alpha, final double limitTolerance) {
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    _alpha = alpha;
    _limitTolerance = limitTolerance;
  }

  public LeastSquareResults solve(final double forward, final double maturity, final double[] strikes, final double[] blackVols, final double[] errors, final double[] initialValues, final BitSet fixed) {
    final int n = strikes.length;
    Validate.isTrue(n == blackVols.length, "strikes and vols must be same length");
    Validate.isTrue(n == errors.length, "errors and vols must be same length");

    final TransformParameters transforms = new TransformParameters(new DoubleMatrix1D(initialValues), TRANSFORMS, fixed);
    final double alpha = -0.5;
    final double tol = 1e-8;
    final double limitSigma = (blackVols[0] + blackVols[blackVols.length - 1]) / 2.0;
    final double sL = strikes[0];
    final double sH = strikes[n - 1];

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> hestonVols = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D fp) {
        final DoubleMatrix1D mp = transforms.inverseTransform(fp);
        final double kappa = mp.getEntry(0);
        final double theta = mp.getEntry(1);
        final double vol0 = mp.getEntry(2);
        final double omega = mp.getEntry(3);
        final double rho = mp.getEntry(4);
        final CharacteristicExponent1 ce = new HestonCharacteristicExponent1(kappa, theta, vol0, omega, rho);
        final BlackFunctionData data = new BlackFunctionData(forward, 1.0, limitSigma);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(strikes[0], maturity, true);
        final double[][] strikeNPrice = FFT_PRICER.price(data, option, ce, sL, sH, n, alpha, tol);
        final int nStrikes = strikeNPrice.length;
        final double[] k = new double[nStrikes];
        final double[] vol = new double[nStrikes];
        for (int i = 0; i < nStrikes; i++) {
          k[i] = strikeNPrice[i][0];
          try {
            vol[i] = BLACK_IMPLIED_VOL_FORMULA.getImpliedVolatility(data, new EuropeanVanillaOption(k[i], maturity, true), strikeNPrice[i][1]);
          } catch (final Exception e) {
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

    final DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialValues));

    final LeastSquareResults results = SOLVER.solve(new DoubleMatrix1D(blackVols), new DoubleMatrix1D(errors), hestonVols, fp);
    return new LeastSquareResults(results.getChiSq(), transforms.inverseTransform(results.getParameters()), new DoubleMatrix2D(new double[N_PARAMETERS][N_PARAMETERS]));
  }

  public LeastSquareResults solvePrice(final double forward, final double maturity, final double[] strikes, final double[] blackVols, final double[] errors, final double[] initialValues,
      final BitSet fixed) {

    final int n = strikes.length;
    Validate.isTrue(n == blackVols.length, "strikes and vols must be same length");
    Validate.isTrue(n == errors.length, "errors and vols must be same length");

    final TransformParameters transforms = new TransformParameters(new DoubleMatrix1D(initialValues), TRANSFORMS, fixed);
    final double sL = strikes[0];
    final double sH = strikes[n - 1];

    final double alpha = -0.5;
    final double tol = 1e-8;
    final double[] prices = new double[blackVols.length];
    for (int i = 0; i < blackVols.length; i++) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(strikes[i], maturity, true);
      final BlackFunctionData data = new BlackFunctionData(forward, 1, blackVols[i]);
      prices[i] = BLACK_PRICE_FUNCTION.getPriceFunction(option).evaluate(data);
    }
    final double limitSigma = (blackVols[0] + blackVols[blackVols.length - 1]) / 2.0;

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> hestonVols = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D fp) {
        final DoubleMatrix1D mp = transforms.inverseTransform(fp);
        final double kappa = mp.getEntry(0);
        final double theta = mp.getEntry(1);
        final double vol0 = mp.getEntry(2);
        final double omega = mp.getEntry(3);
        final double rho = mp.getEntry(4);
        final CharacteristicExponent1 ce = new HestonCharacteristicExponent1(kappa, theta, vol0, omega, rho);
        final BlackFunctionData data = new BlackFunctionData(forward, 1.0, limitSigma);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(strikes[0], maturity, true);
        final double[][] strikeNPrice = FFT_PRICER.price(data, option, ce, sL, sH, n, alpha, tol);
        final int nStrikes = strikeNPrice.length;
        final double[] k = new double[nStrikes];
        final double[] price = new double[nStrikes];
        for (int i = 0; i < nStrikes; i++) {
          k[i] = strikeNPrice[i][0];
          price[i] = strikeNPrice[i][1];
        }
        final Interpolator1DDataBundle dataBundle = _interpolator.getDataBundle(k, price);
        final int m = strikes.length;
        final double[] res = new double[m];
        for (int i = 0; i < m; i++) {
          res[i] = _interpolator.interpolate(dataBundle, strikes[i]);
        }
        return new DoubleMatrix1D(res);
      }
    };

    final DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialValues));

    final LeastSquareResults results = SOLVER.solve(new DoubleMatrix1D(prices), new DoubleMatrix1D(errors), hestonVols, fp);
    return new LeastSquareResults(results.getChiSq(), transforms.inverseTransform(results.getParameters()), new DoubleMatrix2D(new double[N_PARAMETERS][N_PARAMETERS]));
  }

  public LeastSquareResults solveFourierIntegral(final double forward, final double maturity, final double[] strikes, final double[] blackVols, final double[] errors, final double[] initialValues,
      final BitSet fixed) {

    final int n = strikes.length;
    Validate.isTrue(n == blackVols.length, "strikes and vols must be same length");
    Validate.isTrue(n == errors.length, "errors and vols must be same length");

    final TransformParameters transforms = new TransformParameters(new DoubleMatrix1D(initialValues), TRANSFORMS, fixed);

    final double limitSigma = (blackVols[0] + blackVols[blackVols.length - 1]) / 2.0;

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
        final BlackFunctionData data = new BlackFunctionData(forward, 1, limitSigma);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, maturity, true);
        final double price = FOURIER_PRICER.price(data, option, ce, _alpha, _limitTolerance);
        final double vol = BLACK_IMPLIED_VOL_FORMULA.getImpliedVolatility(data, option, price);
        return vol;
      }
    };

    final DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialValues));
    final LeastSquareResults results = SOLVER.solve(new DoubleMatrix1D(strikes), new DoubleMatrix1D(blackVols), new DoubleMatrix1D(errors), function, fp);
    return new LeastSquareResults(results.getChiSq(), transforms.inverseTransform(results.getParameters()), new DoubleMatrix2D(new double[N_PARAMETERS][N_PARAMETERS]));
  }

}
