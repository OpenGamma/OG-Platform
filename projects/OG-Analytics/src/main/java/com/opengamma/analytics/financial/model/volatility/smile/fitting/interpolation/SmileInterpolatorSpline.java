/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.differentiation.ScalarFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Fits a set of implied volatilities at given strikes by interpolating log-moneyness (ln(strike/forward)) against implied volatility using the supplied interpolator (the default
 * is double quadratic). While this will fit any input data, there is no guarantee that the smile is arbitrage free, or indeed always positive, and should therefore be used with
 * care, and only when other smile interpolators fail. The smile is extrapolated in both directions using shifted log-normals set to match the level and slope of the smile at
 * the end point.
 */
public class SmileInterpolatorSpline implements GeneralSmileInterpolator {
  //  private static final Logger LOG = LoggerFactory.getLogger(ShiftedLogNormalTailExtrapolationFitter.class);
  private static final Interpolator1D DEFAULT_INTERPOLATOR = new DoubleQuadraticInterpolator1D();
  private static final ScalarFirstOrderDifferentiator DIFFERENTIATOR = new ScalarFirstOrderDifferentiator();
  private static final ShiftedLogNormalTailExtrapolationFitter TAIL_FITTER = new ShiftedLogNormalTailExtrapolationFitter();

  private static final String s_exception = "Exception"; // OG-Financial's BlackVolatilitySurfacePropertyNamesAndValues.EXCEPTION_SPLINE_EXTRAPOLATOR_FAILURE;
  private static final String s_flat = "Flat"; // OG-Financial's BlackVolatilitySurfacePropertyNamesAndValues.FLAT_SPLINE_EXTRAPOLATOR_FAILURE;
  private static final String s_quiet = "Quiet"; // OG-Financial's BlackVolatilitySurfacePropertyNamesAndValues.QUIET_SPLINE_EXTRAPOLATOR_FAILURE;

  private final Interpolator1D _interpolator;
  private final String _extrapolatorFailureBehaviour;

  public SmileInterpolatorSpline() {
    this(DEFAULT_INTERPOLATOR);
  }

  public SmileInterpolatorSpline(final Interpolator1D interpolator) {
    ArgumentChecker.notNull(interpolator, "null interpolator");
    _interpolator = interpolator;
    _extrapolatorFailureBehaviour = s_exception; // This follows pattern of OG-Financial's BlackVolatilitySurfacePropertyNamesAndValues.EXCEPTION_SPLINE_EXTRAPOLATOR_FAILURE
  }

  public SmileInterpolatorSpline(final Interpolator1D interpolator, String extrapolatorFailureBehaviour) {
    ArgumentChecker.notNull(interpolator, "null interpolator");
    _interpolator = interpolator;
    _extrapolatorFailureBehaviour = extrapolatorFailureBehaviour;
  }

  /**
   * Gets the extrapolatorFailureBehaviour. If a shiftedLognormal model (Black with additional free parameter, F' = F*exp(mu)) fails to fit the boundary vol and the vol smile at that point...<p>
   * "Exception": an exception will be thrown <p>
   * "Quiet":  the target gradient is reduced until a solution is found.<p>
   * "Flat": the target gradient is zero. A trivial solution exists in which extrapolated volatilities equal target volatility.<p>
   * @return the extrapolatorFailureBehaviour
   */
  public final String getExtrapolatorFailureBehaviour() {
    return _extrapolatorFailureBehaviour;
  }

  @Override
  public Function1D<Double, Double> getVolatilityFunction(final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {
    ArgumentChecker.notNull(strikes, "strikes");
    ArgumentChecker.notNull(impliedVols, "implied vols");
    final int n = strikes.length;
    ArgumentChecker.isTrue(impliedVols.length == n, "#strikes {} does not match #vols {}", n, impliedVols.length);
    final double kL = strikes[0];
    final double kH = strikes[n - 1];


    final double[] x = new double[n];
    for (int i = 0; i < n; i++) {
      x[i] = Math.log(strikes[i] / forward);
    }

    // Interpolator
    final Interpolator1DDataBundle data = _interpolator.getDataBundle(x, impliedVols);

    final Function1D<Double, Double> interpFunc = new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
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

    // Extrapolation of High and Low Strikes by ShiftedLogNormalTailExtrapolationFitter

    // Solutions contain two parameters: [0] = mu = ln(shiftedForward / originalForward), [1] = theta = new ln volatility to use
    final double[] shiftLnVolHighTail;
    final double[] shiftLnVolLowTail;

    // Volatility gradient (dVol/dStrike) of interpolator
    final Function1D<Double, Double> dSigmaDx = DIFFERENTIATOR.differentiate(interpFunc, domain);

    // The 'quiet' method reduces smile if the volatility gradient is either out of bounds of ShiftedLognormal model, or if root-finder fails to find solution
    if (_extrapolatorFailureBehaviour.equalsIgnoreCase(s_quiet)) {
      ArgumentChecker.isTrue(kL <= forward, "Cannot do left tail extrapolation when the lowest strike ({}) is greater than the forward ({})", kL, forward);
      ArgumentChecker.isTrue(kH >= forward, "Cannot do right tail extrapolation when the highest strike ({}) is less than the forward ({})", kH, forward);
      shiftLnVolHighTail = TAIL_FITTER.fitVolatilityAndGradRecursivelyByReducingSmile(forward, strikes[n - 1], impliedVols[n - 1], dSigmaDx.evaluate(kH), expiry);
      shiftLnVolLowTail = TAIL_FITTER.fitVolatilityAndGradRecursivelyByReducingSmile(forward, kL, impliedVols[0], dSigmaDx.evaluate(kL), expiry);
      // 'Exception' will throw an exception if it fails to fit to target vol and gradient provided by interpolating function at the boundary
    } else if (_extrapolatorFailureBehaviour.equalsIgnoreCase(s_exception)) {
      ArgumentChecker.isTrue(kL <= forward, "Cannot do left tail extrapolation when the lowest strike ({}) is greater than the forward ({})", kL, forward);
      ArgumentChecker.isTrue(kH >= forward, "Cannot do right tail extrapolation when the highest strike ({}) is less than the forward ({})", kH, forward);
      shiftLnVolHighTail = TAIL_FITTER.fitVolatilityAndGrad(forward, kH, impliedVols[n - 1], dSigmaDx.evaluate(kH), expiry);
      shiftLnVolLowTail = TAIL_FITTER.fitVolatilityAndGrad(forward, kL, impliedVols[0], dSigmaDx.evaluate(kL), expiry);
      // 'Flat' will simply return the target volatility at the boundary. Thus the target gradient is zero.
    } else if (_extrapolatorFailureBehaviour.equalsIgnoreCase(s_flat)) {
      shiftLnVolHighTail = TAIL_FITTER.fitVolatilityAndGrad(forward, kH, impliedVols[n - 1], 0.0, expiry);
      shiftLnVolLowTail = TAIL_FITTER.fitVolatilityAndGrad(forward, kL, impliedVols[0], 0.0, expiry);
    } else {
      throw new OpenGammaRuntimeException("Unrecognized _extrapolatorFailureBehaviour. Looking for one of Exception, Quiet, or Flat");
    }

    // Resulting Functional Vol Surface
    Function1D<Double, Double> volSmileFunction = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double k) {
        if (k < kL) {
          return ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, k, expiry, shiftLnVolLowTail[0], shiftLnVolLowTail[1]);
        } else if (k > kH) {
          return ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, k, expiry, shiftLnVolHighTail[0], shiftLnVolHighTail[1]);
        } else {
          return interpFunc.evaluate(k);
        }
      }
    };

    return volSmileFunction;
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
