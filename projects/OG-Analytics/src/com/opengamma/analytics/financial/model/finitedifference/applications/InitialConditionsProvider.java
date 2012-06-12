/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class InitialConditionsProvider {

  public Function1D<Double, Double> getEuropeanPayoff(final double strike, final boolean isCall) {
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        if (isCall) {
          return Math.max(0, x - strike);
        }
        return Math.max(0, strike - x);
      }
    };
  }

  public Function1D<Double, Double> getLogNormalDensity(final double forward, final double t, final double vol) {
    ArgumentChecker.isTrue(forward > 0, "must have forward > 0");
    ArgumentChecker.isTrue(t > 0, "must have t > 0");
    ArgumentChecker.isTrue(vol > 0, "must have vol > 0");
    final double sigmaRootT = vol * Math.sqrt(t);
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double s) {
        if (s == 0) {
          return 0.0;
        }
        final double x = Math.log(s / forward);
        final NormalDistribution dist = new NormalDistribution(0, sigmaRootT);
        return dist.getPDF(x) / s;
      }
    };
  }

}
