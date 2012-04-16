/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.math.differentiation.ScalarFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Fits a set of implied volatilities at gives strikes by interpolating log-moneyness (ln(strike/forward)) against implied volatility using the supplied interpolator (the default
 * is double quadratic). While this will fit any input data, there is no guarantee that the smile is arbitrage free, or indeed always positive, and should therefore be used with
 * care, and only when other smile interpolators fail. The smile is extrapolated in both directions using shifted log-normals set to match the level and slope of the smile at
 * the end point.
 */
public class SmileInterpolatorSpline implements GeneralSmileInterpolator {
  private static final Interpolator1D DEFAULT_INTERPOLATOR = new DoubleQuadraticInterpolator1D(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION);
  private static final ScalarFirstOrderDifferentiator DIFFERENTIATOR = new ScalarFirstOrderDifferentiator();
  private static final ShiftedLogNormalTailExtrapolationFitter TAIL_FITTER = new ShiftedLogNormalTailExtrapolationFitter();

  private final Interpolator1D _interpolator;

  public SmileInterpolatorSpline() {
    this(DEFAULT_INTERPOLATOR);
  }

  public SmileInterpolatorSpline(final Interpolator1D interpolator) {
    ArgumentChecker.notNull(interpolator, "null interpolator");
    _interpolator = interpolator;
  }

  @Override
  public Function1D<Double, Double> getVolatilityFunction(final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {
    ArgumentChecker.notNull(strikes, "strikes");
    ArgumentChecker.notNull(impliedVols, "implied vols");
    final int n = strikes.length;
    ArgumentChecker.isTrue(impliedVols.length == n, "#strikes {} does not match #vols {}", n, impliedVols.length);
    final double kL = strikes[0];
    final double kH = strikes[n - 1];
    ArgumentChecker.isTrue(kL <= forward, "Cannot do left tail extrapolation when the lowest strike ({}) is greater than the forward ({})", kL, forward);
    ArgumentChecker.isTrue(kH >= forward, "Cannot do right tail extrapolation when the highest strike ({}) is less than the forward ({})", kH, forward);
    final double volL = impliedVols[0];
    final double volH = impliedVols[n - 1];

    final double[] x = new double[n];
    for (int i = 0; i < n; i++) {
      x[i] = Math.log(strikes[i] / forward);
    }
    final Interpolator1DDataBundle data = _interpolator.getDataBundle(x, impliedVols);

    final Function1D<Double, Double> interpFunc = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double k) {
        final double m = Math.log(k / forward);
        return _interpolator.interpolate(data, m);
      }
    };

    final Function1D<Double, Boolean> domain = new Function1D<Double, Boolean>() {
      @Override
      public Boolean evaluate(final Double k) {
        return k >= kL && k <= kH;
      }
    };

    final Function1D<Double, Double> dSigmaDx = DIFFERENTIATOR.differentiate(interpFunc, domain);

    final double gradL = dSigmaDx.evaluate(kL);
    final double gradH = dSigmaDx.evaluate(kH);

    final double[] res1 = TAIL_FITTER.fitVolatilityAndGrad(forward, kL, volL, gradL, expiry);
    final double[] res2 = TAIL_FITTER.fitVolatilityAndGrad(forward, kH, volH, gradH, expiry);

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double k) {
        if (k < kL) {
          return ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, k, expiry, res1[0], res1[1]);
        } else if (k > kH) {
          return ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, k, expiry, res2[0], res2[1]);
        } else {
          return interpFunc.evaluate(k);
        }
      }
    };

  }

  public Interpolator1D getInterpolator() {
    return _interpolator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _interpolator.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SmileInterpolatorSpline other = (SmileInterpolatorSpline) obj;
    return ObjectUtils.equals(_interpolator, other._interpolator);
  }

}
