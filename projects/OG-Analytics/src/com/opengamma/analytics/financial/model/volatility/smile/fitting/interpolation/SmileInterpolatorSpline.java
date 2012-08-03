/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import com.opengamma.analytics.math.differentiation.ScalarFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;

import java.util.ArrayList;

import org.apache.commons.lang.ObjectUtils;

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

  private final Interpolator1D _interpolator;
  private final String _extrapolatorFailureBehaviour;

  public SmileInterpolatorSpline() {
    this(DEFAULT_INTERPOLATOR);
  }

  public SmileInterpolatorSpline(final Interpolator1D interpolator) {
    ArgumentChecker.notNull(interpolator, "null interpolator");
    _interpolator = interpolator;
    _extrapolatorFailureBehaviour = "Exception"; // This follows pattern of OG-Financial's BlackVolatilitySurfacePropertyNamesAndValues.EXCEPTION_SPLINE_EXTRAPOLATOR_FAILURE
  }

  public SmileInterpolatorSpline(final Interpolator1D interpolator, String extrapolatorFailureBehaviour) {
    ArgumentChecker.notNull(interpolator, "null interpolator");
    _interpolator = interpolator;
    _extrapolatorFailureBehaviour = extrapolatorFailureBehaviour;
  }

  /**
   * Gets the extrapolatorFailureBehaviour. If a shiftedLognormal model (Black with additional free paramter, F' = F*exp(mu)) fails to fit the boundary vol and the vol smile at that point...<p>
   * "Exception": an exception will be thrown <p>
   * "Quiet":  the failing vol/strike will be tossed away, and we try the closest interior point. This repeats until a solution is found.
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
    ArgumentChecker.isTrue(kL <= forward, "Cannot do left tail extrapolation when the lowest strike ({}) is greater than the forward ({})", kL, forward);
    ArgumentChecker.isTrue(kH >= forward, "Cannot do right tail extrapolation when the highest strike ({}) is less than the forward ({})", kH, forward);

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
    final Function1D<Double, Double> dSigmaDx = DIFFERENTIATOR.differentiate(interpFunc, domain); // 
    final ArrayList<Double> highParamsShiftVolStrike = new ArrayList<Double>();;
    final ArrayList<Double> lowParamsShiftVolStrike = new ArrayList<Double>();;
    if (_extrapolatorFailureBehaviour.equalsIgnoreCase("Quiet")) {
      final ArrayList<Double> tempHighExtrapParams = TAIL_FITTER.fitVolatilityAndGradRecursively(forward, strikes, impliedVols, dSigmaDx, expiry, false);
      final ArrayList<Double> tempLowExtrapParams = TAIL_FITTER.fitVolatilityAndGradRecursively(forward, strikes, impliedVols, dSigmaDx, expiry, true);
      highParamsShiftVolStrike.addAll(tempHighExtrapParams);
      lowParamsShiftVolStrike.addAll(tempLowExtrapParams);
    } else {
      final double[] shiftLnVolLow = TAIL_FITTER.fitVolatilityAndGrad(forward, kL, impliedVols[0], dSigmaDx.evaluate(kL), expiry);
      lowParamsShiftVolStrike.add(0, shiftLnVolLow[0]); // mu = ln(shiftedForward / originalForward)
      lowParamsShiftVolStrike.add(1, shiftLnVolLow[1]); // theta = new ln volatility to use
      lowParamsShiftVolStrike.add(2, strikes[0]); // new extapolation boundary
      final double[] shiftLnVolHigh = TAIL_FITTER.fitVolatilityAndGrad(forward, kH, impliedVols[n - 1], dSigmaDx.evaluate(kH), expiry);
      highParamsShiftVolStrike.add(0, shiftLnVolHigh[0]); // mu = ln(shiftedForward / originalForward)
      highParamsShiftVolStrike.add(1, shiftLnVolHigh[1]); // theta = new ln volatility to use
      highParamsShiftVolStrike.add(2, strikes[n - 1]); // new extapolation boundary
    }

    // Resulting Functional Vol Surface
    Function1D<Double, Double> volSmileFunction = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double k) {
        if (k < lowParamsShiftVolStrike.get(2)) {
          return ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, k, expiry, lowParamsShiftVolStrike.get(0), lowParamsShiftVolStrike.get(1));
        } else if (k > highParamsShiftVolStrike.get(2)) {
          return ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, k, expiry, highParamsShiftVolStrike.get(0), highParamsShiftVolStrike.get(1));
        } else {
          return interpFunc.evaluate(k);
        }
      }
    };

    //    for (int k = 0; k < 2000; k = k + 100) {
    //      System.out.println(k + "," + volSmileFunction.evaluate((double) k));
    //    }

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
