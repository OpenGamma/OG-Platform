/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting.sabr;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.financial.model.volatility.surface.Strike;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
public class StrikePiecewiseSABRSurfaceFitter implements PiecewiseSABRSurfaceFitter1<Strike> {
  private static final PiecewiseSABRFitter1 FITTER = new PiecewiseSABRFitter1();
  private static final Interpolator1D EXTRAPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private final boolean _useLogTime;
  private final boolean _useIntegratedVar;
  private final double _lambda;

  /**
   * @param useLogTime The x-axis is the log of time
   * @param useIntegratedVar the y-points are integrated variance (rather than volatility)
   * @param lambda zero the strikes are (almost) the same across fitted smiles, large lambda they scale as root-time
   */
  public StrikePiecewiseSABRSurfaceFitter(final boolean useLogTime, final boolean useIntegratedVar, final double lambda) {
    _useLogTime = useLogTime;
    _useIntegratedVar = useIntegratedVar;
    _lambda = lambda;
  }

  /**
   * For a given expiry and strike, perform an interpolation between either the volatility or integrated variances
   *  of points with the same moneyness on the fitted smiles. There is no guarantees a monotonically increasing integrated variance
   * (hence no calendar arbitrage and a real positive local volatility), but using log time to better space out the x-points
   * help.
   * @param data The surface data
   * @return A interpolated implied volatility surface
   */
  @Override
  public BlackVolatilitySurfaceStrike getVolatilitySurface(final SmileSurfaceDataBundle data) {
    final double[] expiries = data.getExpiries();
    final double[] forwards = data.getForwards();
    final double[][] strikes = data.getStrikes();
    final double[][] impliedVols = data.getVolatilities();
    final ForwardCurve forwardCurve = data.getForwardCurve();
    final int nExpiries = expiries.length;
    //TODO move this out of here - need a way to bump a point on the surface without having to re-fit unaffected slices
    @SuppressWarnings("unchecked")
    final Function1D<Double, Double>[] fitters = new Function1D[nExpiries];
    for (int i = 0; i < nExpiries; i++) {
      fitters[i] = FITTER.getVolatilityFunction(forwards[i], strikes[i], expiries[i], impliedVols[i]);
    }
    final Function<Double, Double> surFunc = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... tk) {
        final double t = tk[0];
        final double k = tk[1];
        final double forward = forwardCurve.getForward(t);
        final double d = Math.log(forward / k) / Math.sqrt(1 + _lambda * t);
        if (t <= expiries[0]) {
          final double k1 = forwards[0] * Math.exp(-d * Math.sqrt(1 + _lambda * expiries[0]));
          return fitters[0].evaluate(k1);
        }
        final int index = SurfaceArrayUtils.getLowerBoundIndex(expiries, t);

        int lower;
        if (index == 0) {
          lower = 0;
        } else if (index == nExpiries - 2) {
          lower = index - 2;
        } else if (index == nExpiries - 1) {
          lower = index - 3;
        } else {
          lower = index - 1;
        }
        final double[] times = Arrays.copyOfRange(expiries, lower, lower + 4);
        double[] xs = new double[4];
        double x = 0;
        if (_useLogTime) {
          for (int i = 0; i < 4; i++) {
            xs[i] = Math.log(times[i]);
            x = Math.log(t);
          }
        } else {
          xs = times;
          x = t;
        }

        final double[] strikes = new double[4];
        final double[] vols = new double[4];
        final double[] intVar = new double[4];
        double[] y = null;

        for (int i = 0; i < 4; i++) {
          strikes[i] = forwards[lower + i] * Math.exp(-d * Math.sqrt(1 + _lambda * times[i]));
          vols[i] = fitters[lower + i].evaluate(strikes[i]);

          intVar[i] = vols[i] * vols[i] * times[i];
          //if (i > 0) {
          //if (intVar[i] < intVar[i - 1]) {
          //Validate.isTrue(intVar[i] >= intVar[i - 1], "variance must increase");
          //}
          //}
          if (_useIntegratedVar) {
            y = intVar;
          } else {
            y = vols;
          }
        }

        final Interpolator1DDataBundle db = EXTRAPOLATOR.getDataBundle(xs, y);
        double sigma;

        final double res = EXTRAPOLATOR.interpolate(db, x);
        if (_useIntegratedVar) {
          Validate.isTrue(res >= 0.0, "Negative integrated variance");
          sigma = Math.sqrt(res / t);
        } else {
          sigma = res;
        }
        return sigma;
      }
    };

    return new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surFunc));
  }
}
