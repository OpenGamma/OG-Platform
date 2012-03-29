/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.surface.Strike;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
public class LinearPiecewiseSABRSurfaceFitter implements PiecewiseSABRSurfaceFitter1<Strike> {
  private static final PiecewiseSABRFitter FITTER = new PiecewiseSABRFitter();

  /**
   * For a given expiry and strike, perform a linear interpolation between the integrated variances of points with
   * the same strike on the two adjacent fitted smiles. This guarantees a monotonically increasing integrated variance
   * (hence no calendar arbitrage and a real positive local volatility), but at the cost of having jumps in the local
   * volatility surface
   * @param data The surface data
   * @return A interpolated implied Volatility surface
   */
  @Override
  public BlackVolatilitySurfaceStrike getVolatilitySurface(final SmileSurfaceDataBundle data) {
    final double[] expiries = data.getExpiries();
    final double[] forwards = data.getForwards();
    final double[][] strikes = data.getStrikes();
    final double[][] impliedVols = data.getVolatilities();
    final int nExpiries = expiries.length;
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
        if (t <= expiries[0]) { //linear extrapolation in sigma
          final double sigma1 = fitters[0].evaluate(k);
          final double sigma2 = fitters[1].evaluate(k);
          final double dt = expiries[1] - expiries[0];
          return ((expiries[1] - t) * sigma1 + (t - expiries[0]) * sigma2) / dt;
        }
        if (t >= expiries[nExpiries - 1]) { //flat extrapolation
          return fitters[nExpiries - 1].evaluate(k);
        }

        final int index = SurfaceArrayUtils.getLowerBoundIndex(expiries, t);
        final double[] sample = new double[2];
        double[] times = new double[2];
        int lower;
        if (index == 0) {
          lower = 0;
        } else if (index >= nExpiries - 1) {
          lower = index - 1;
        } else {
          lower = index;
        }
        for (int i = 0; i < 2; i++) {
          final double vol = fitters[i + lower].evaluate(k);
          sample[i] = vol * vol * expiries[i + lower]; //interpolate the variance
          if (i > 0) {
            Validate.isTrue(sample[i] >= sample[i - 1], "variance must increase");
          }
        }
        times = Arrays.copyOfRange(expiries, lower, lower + 2);

        final double dt = times[1] - times[0];
        final double var = ((times[1] - t) * sample[0] + (t - times[0]) * sample[1]) / dt;

        // double var = INTERPOLATOR_1D.interpolate(db, t);
        if (var >= 0) {
          return Math.sqrt(var / t);
        }
        throw new MathException("negative var " + var);
      }
    };

    return new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surFunc));
  }

}
