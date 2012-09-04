/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.smile.function.MultiHorizonMixedLogNormalModelData;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility to produce volatility surfaces (implied and local) via a mixed log-normal model. This guarantees an arbitrage free implied volatility surface and a corresponding 
 * local volatility surface. It's use is primarily in testing 
 */
public class MixedLogNormalVolatilitySurface {

  private static final double ROOT_2_PI = Math.sqrt(2 * Math.PI);
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  /**
   * Gets the implied volatility surface from a mixed log-normal model
   * @param fwdCurve The forward curve 
   * @param data parameters of a Mixed Log-Normal Model
   * @return implied volatility surface
   */
  public static BlackVolatilitySurfaceStrike getImpliedVolatilitySurface(final ForwardCurve fwdCurve, final MultiHorizonMixedLogNormalModelData data) {
    final double minT = 1e-6;
    ArgumentChecker.notNull(fwdCurve, "null fwdCurve");
    ArgumentChecker.notNull(data, "null data");

    final double[] w = data.getWeights();
    final double[] sigma = data.getVolatilities();
    final double[] mu = data.getMus();
    final int n = w.length;
    double sum = 0;
    for (int i = 0; i < n; i++) {
      sum += w[i] * sigma[i];
    }
    final double tZeroLimit = sum;

    final Function<Double, Double> surf = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... x) {
        double t = x[0];
        final double k = x[1];
        final double fwd = fwdCurve.getForward(t);
        if (t < minT) {
          if (k == fwd) {
            return tZeroLimit;
          }
          t = minT;
        }
        double expOmega = 0.0;
        for (int i = 0; i < n; i++) {
          expOmega += w[i] * Math.exp(t * mu[i]);
        }

        final boolean isCall = k >= fwd;
        final double kStar = k / fwd;
        double price = 0;
        for (int i = 0; i < n; i++) {
          double fStar = Math.exp(t * mu[i]) / expOmega;
          price += w[i] * BlackFormulaRepository.price(fStar, kStar, t, sigma[i], isCall);
        }
        if (price > 1e-250) {
          return BlackFormulaRepository.impliedVolatility(price, 1.0, kStar, t, isCall);
        }

        //if we are such an extreme strike that the price is zero to machine accuracy then the implied vol is a bit moot, although some value may be useful for extrapolation. 
        //Clearly the value found here, if put back into the Black formula will give a price of zero. 
        double largestSigma = 0.0;
        for (int i = 0; i < n; i++) {
          if (w[i] > 0.0 && sigma[i] > largestSigma) {
            largestSigma = sigma[i];
          }
        }
        return largestSigma;
      }
    };
    return new BlackVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surf));
  }

  /**
   * Gets the local volatility surface from a mixed log-normal model
   * @param fwdCurve The forward curve 
   * @param data parameters of a Mixed Log-Normal Model
   * @return local volatility surface
   */
  public static LocalVolatilitySurfaceStrike getLocalVolatilitySurface(final ForwardCurve fwdCurve, final MultiHorizonMixedLogNormalModelData data) {
    final double maxExp = 100;
    final double t0 = 1e-4;
    final double rootT0 = Math.sqrt(t0);
    final double minT = 1e-12;

    ArgumentChecker.notNull(fwdCurve, "null fwdCurve");
    ArgumentChecker.notNull(data, "null data");

    final double[] w = data.getWeights();
    final double[] sigma = data.getVolatilities();
    final double[] mu = data.getMus();
    final int n = w.length;

    //    double sum1 = 0;
    //    double sum2 = 0;
    //    for (int i = 0; i < n; i++) {
    //      sum1 += w[i] * sigma[i];
    //      sum2 += w[i] / sigma[i];
    //    }
    //    final double tZeroLimit = Math.sqrt(sum1 / sum2);

    final Function<Double, Double> surf = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        double t = Math.max(minT, tk[0]);
        final double k = tk[1];
        final double fwd = fwdCurve.getForward(t);

        final double rootT = Math.sqrt(t);
        final boolean isCall = k >= fwd;
        final double x = k / fwd;

        if (t < t0) {
          final double k2 = fwd * Math.pow(x, rootT0 / rootT);
          return evaluate(t0, k2);
        }

        final double maxX = Math.exp(rootT * maxExp);
        if (x > maxX) {
          return evaluate(t, maxX * fwd);
        }
        final double minX = 1 / maxX;
        if (x < minX) {
          return evaluate(t, minX * fwd);
        }

        double expOmega = 0.0;
        double muStar = 0.0;

        for (int i = 0; i < n; i++) {
          double temp = w[i] * Math.exp(t * mu[i]);
          expOmega += temp;
          muStar += temp * mu[i];
        }
        muStar /= expOmega;

        double[] d1 = new double[n];
        double[] d1Sqr = new double[n];
        double[] fStar = new double[n];
        double d1SqrMin = Double.POSITIVE_INFINITY;

        for (int i = 0; i < n; i++) {
          fStar[i] = Math.exp(t * mu[i]) / expOmega;
          d1[i] = (Math.log(fStar[i] / x) + 0.5 * sigma[i] * sigma[i] * t) / sigma[i] / rootT;
          d1Sqr[i] = d1[i] * d1[i];
          if (d1Sqr[i] < d1SqrMin) {
            d1SqrMin = d1Sqr[i];
          }

        }

        double den = 0;
        double num = 0;
        final boolean useApprox = d1SqrMin > maxExp;
        for (int i = 0; i < n; i++) {
          double regPhi = Math.exp(0.5 * (d1SqrMin - d1Sqr[i]));
          double deltaStar = 0;
          if (useApprox) {
            deltaStar = (isCall ? 1.0 : -1.0) * 2 * rootT * regPhi / Math.sqrt(1 + d1Sqr[i]);
          } else {
            final double invPhiMin = Math.exp(0.5 * d1SqrMin) * ROOT_2_PI;
            double delta = isCall ? NORMAL.getCDF(d1[i]) : -NORMAL.getCDF(-d1[i]);
            deltaStar = 2 * rootT * invPhiMin * delta;
          }

          den += w[i] * fStar[i] * regPhi / sigma[i];
          num += w[i] * fStar[i] * (regPhi * sigma[i] + deltaStar * (mu[i] - muStar));
        }

        double res = Math.sqrt(num / den);
        if (Doubles.isFinite(res)) {
          return res;
        }
        return 0.0;
        //        throw new MathException("Local Volatility failure: " + res);
      }
    };
    return new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surf));
  }
}
