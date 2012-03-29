/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDEDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.ExtendedConvectionDiffusionPDEDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.ExtendedCoupledPDEDataBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.surface.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
public class LocalVolDensity {

  public static ConvectionDiffusionPDEDataBundle getConvectionDiffusionPDEDataBundle(final ForwardCurve forward, final LocalVolatilitySurfaceStrike localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double sigma = localVol.getVolatility(t, s) * s;
        return -0.5 * sigma * sigma;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double lvDiv = getLocalVolFirstDiv(localVol, t, s);
        final double lv = localVol.getVolatility(t, s);
        return s * (forward.getDrift(t) - 2 * lv * (s * lvDiv + lv));
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double lv1Div = getLocalVolFirstDiv(localVol, t, s);
        final double lv2Div = getLocalVolSecondDiv(localVol, t, s);
        final double lv = localVol.getVolatility(t, s);
        final double temp1 = (lv + s * lv1Div);
        final double temp2 = lv * s * (s * lv2Div + 2 * lv1Div);

        return forward.getDrift(t) - temp1 * temp1 - temp2;
      }
    };

    //using a log-normal distribution with a very small Standard deviation as a proxy for a Dirac delta
    final Function1D<Double, Double> initialCondition = new Function1D<Double, Double>() {
      private final double _volRootTOffset = 0.01;

      @Override
      public Double evaluate(final Double s) {
        if (s == 0) {
          return 0.0;
        }
        final double x = Math.log(s / forward.getSpot());
        final NormalDistribution dist = new NormalDistribution(0, _volRootTOffset);
        return dist.getPDF(x) / s;
      }
    };

    return new ConvectionDiffusionPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), initialCondition);

  }

  public static ExtendedConvectionDiffusionPDEDataBundle getExtendedConvectionDiffusionPDEDataBundle(final ForwardCurve forward, final LocalVolatilitySurfaceStrike localVol) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return -1.0;
      }
    };

    final Function<Double, Double> alpha = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double temp = s * localVol.getVolatility(t, s);

        return 0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        return s * forward.getDrift(t);
      }
    };

    final Function<Double, Double> beta = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return 1.0;
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];

        return forward.getDrift(t);
      }
    };

    //using a log-normal distribution with a very small Standard deviation as a proxy for a Dirac delta
    final Function1D<Double, Double> initialCondition = new Function1D<Double, Double>() {
      private final double _volRootTOffset = 0.01;

      @Override
      public Double evaluate(final Double s) {
        if (s == 0) {
          return 0.0;
        }
        final double x = Math.log(s / forward.getSpot());
        final NormalDistribution dist = new NormalDistribution(0, _volRootTOffset);
        return dist.getPDF(x) / s;
      }
    };

    return new ExtendedConvectionDiffusionPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), FunctionalDoublesSurface.from(alpha),
        FunctionalDoublesSurface.from(beta), initialCondition);
  }

  public static ExtendedCoupledPDEDataBundle getExtendedCoupledPDEDataBundle(final ForwardCurve forward, final LocalVolatilitySurfaceStrike localVol, final double lambda1, final double lambda2,
      final double initialProb) {

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return -1.0;
      }
    };

    final Function<Double, Double> alpha = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        final double temp = s * localVol.getVolatility(t, s);

        return 0.5 * temp * temp;
      }
    };

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];
        final double s = ts[1];
        return s * forward.getDrift(t);
      }
    };

    final Function<Double, Double> beta = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        return 1.0;
      }
    };

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... ts) {
        Validate.isTrue(ts.length == 2);
        final double t = ts[0];

        return forward.getDrift(t) + lambda1;
      }
    };

    //using a log-normal distribution with a very small Standard deviation as a proxy for a Dirac delta
    final Function1D<Double, Double> initialCondition = new Function1D<Double, Double>() {
      private final double _volRootTOffset = 0.01;

      @Override
      public Double evaluate(final Double s) {
        if (s == 0) {
          return 0.0;
        }
        final double x = Math.log(s / forward.getSpot());
        final NormalDistribution dist = new NormalDistribution(0, _volRootTOffset);
        return initialProb * dist.getPDF(x) / s;
      }
    };

    return new ExtendedCoupledPDEDataBundle(FunctionalDoublesSurface.from(a), FunctionalDoublesSurface.from(b), FunctionalDoublesSurface.from(c), FunctionalDoublesSurface.from(alpha),
        FunctionalDoublesSurface.from(beta), lambda2, initialCondition);

  }

  //TODO handle with a central calculator
  private static double getLocalVolFirstDiv(final LocalVolatilitySurfaceStrike localVol, final double t, final double s) {
    final double eps = 1e-4;
    final double up = localVol.getVolatility(t, s + eps);
    final double down = localVol.getVolatility(t, s - eps);
    return (up - down) / 2 / eps;
  }

  private static double getLocalVolSecondDiv(final LocalVolatilitySurfaceStrike localVol, final double t, final double s) {
    final double eps = 1e-4;
    final double up = localVol.getVolatility(t, s + eps);
    final double mid = localVol.getVolatility(t, s);
    final double down = localVol.getVolatility(t, s - eps);
    return (up + down - 2 * mid) / eps / eps;
  }

}
