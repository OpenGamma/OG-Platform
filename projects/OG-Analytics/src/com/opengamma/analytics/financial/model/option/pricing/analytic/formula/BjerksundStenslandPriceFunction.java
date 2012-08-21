/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.BivariateNormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class BjerksundStenslandPriceFunction implements OptionPriceFunction<BlackFunctionData> {
  private static final ProbabilityDistribution<double[]> BIVARIATE_NORMAL = new BivariateNormalDistribution();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Override
  public Function1D<BlackFunctionData, Double> getPriceFunction(final EuropeanVanillaOption option) {

    return new Function1D<BlackFunctionData, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final BlackFunctionData data) {
        ArgumentChecker.notNull(data, "data");
        final double f = data.getForward();
        final double k = option.getStrike();
        final double t = option.getTimeToExpiry();
        final double sigma = data.getBlackVolatility();
        final double df = data.getDiscountFactor();
        if (!option.isCall()) {
          if (f == 0) {
            return k;
          }
          return -99999.;
          //TODO
          //          r -= b;
          //          b *= -1;
          //          final double temp = s;
          //          s = k;
          //          k = temp;
          //          final YieldAndDiscountCurve curve = data.getInterestRateCurve().withParallelShift(-b);
          //          newData = data.withInterestRateCurve(curve).withSpot(s);
        }
        //        if (b >= r) {
        //          final OptionDefinition european = new EuropeanVanillaOptionDefinition(k, definition.getExpiry(), definition.isCall());
        //          final Function1D<StandardOptionDataBundle, Double> bsm = BSM.getPricingFunction(european);
        //          return bsm.evaluate(newData);
        //        }
        final double r = -Math.log(df) / t;
        return getCallPrice(f, k, sigma, t, r, 0);
      }
    };
  }

  private double getCallPrice(final double f, final double k, final double sigma, final double t, final double r, final double b) {
    final double sigmaSq = sigma * sigma;
    final double y = 0.5 - b / sigmaSq;
    final double beta = y + Math.sqrt(y * y + 2 * r / sigmaSq);
    final double b0 = Math.max(k, r * k / (r - b));
    final double bInfinity = beta * k / (beta - 1);
    final double t1 = 0.5 * (Math.sqrt(5) - 1) * t;
    final double h1 = getH(b, t1, sigma, k, b0, bInfinity);
    final double h2 = getH(b, t, sigma, k, b0, bInfinity);
    final double x1 = getX(b0, bInfinity, h1);
    final double x2 = getX(b0, bInfinity, h2);
    if (f >= x2) {
      return f - k;
    }
    final double alpha1 = getAlpha(x1, beta, k);
    final double alpha2 = getAlpha(x2, beta, k);
    return alpha2 * Math.pow(f, beta) - alpha2 * getPhi(f, t1, beta, x2, x2, r, b, sigma) + getPhi(f, t1, 1, x2, x2, r, b, sigma) - getPhi(f, t1, 1, x1, x2, r, b, sigma)
        - k * getPhi(f, t1, 0, x2, x2, r, b, sigma) + k * getPhi(f, t1, 0, x1, x2, r, b, sigma) + alpha1 * getPhi(f, t1, beta, x1, x2, r, b, sigma) - alpha1
        * getPsi(f, t1, t, beta, x1, x2, x1, r, b, sigma) + getPsi(f, t1, t, 1, x1, x2, x1, r, b, sigma) - getPsi(f, t1, t, 1, k, x2, x1, r, b, sigma) - k
        * getPsi(f, t1, t, 0, x1, x2, x1, r, b, sigma) + k * getPsi(f, t1, t, 0, k, x2, x1, r, b, sigma);
  }

  private double getH(final double b, final double t, final double sigma, final double k, final double b0, final double bInfinity) {
    return -(b * t + 2 * sigma * Math.sqrt(t)) * k * k / (b0 * (bInfinity - b0));
  }

  private double getX(final double b0, final double bInfinity, final double h) {
    return b0 + (bInfinity - b0) * (1 - Math.exp(h));
  }

  private double getAlpha(final double i, final double beta, final double k) {
    return Math.pow(i, -beta) * (i - k);
  }

  private double getPhi(final double s, final double t, final double gamma, final double h, final double x, final double r, final double b, final double sigma) {
    final double sigmaSq = sigma * sigma;
    final double denom = getDenom(t, sigma);
    final double lambda = getLambda(r, gamma, b, sigmaSq);
    final double kappa = getKappa(b, gamma, sigmaSq);
    final double y = getY(t, b, sigmaSq, gamma, denom);
    final double d1 = getD(s / h, denom, y);
    final double d2 = getD(x * x / (s * h), denom, y);
    return Math.exp(lambda * t) * Math.pow(s, gamma) * (NORMAL.getCDF(d1) - Math.pow(x / s, kappa) * NORMAL.getCDF(d2));
  }

  private double getPsi(final double s, final double t1, final double t2, final double gamma, final double h, final double x2, final double x1, final double r,
      final double b, final double sigma) {
    final double sigmaSq = sigma * sigma;
    final double denom1 = getDenom(t1, sigma);
    final double denom2 = getDenom(t2, sigma);
    final double y1 = getY(t1, b, sigmaSq, gamma, denom1);
    final double y2 = getY(t2, b, sigmaSq, gamma, denom2);
    final double d1 = getD(s / x1, denom1, y1);
    final double d2 = getD(x2 * x2 / (s * x1), denom1, y1);
    final double d3 = d1 + 2 * y1;
    final double d4 = d2 + 2 * y1;
    final double e1 = getD(s / h, denom2, y2);
    final double e2 = getD(x2 * x2 / (s * h), denom2, y2);
    final double e3 = getD(x1 * x1 / (s * h), denom2, y2);
    final double e4 = getD(s * x1 * x1 / (h * x2 * x2), denom2, y2);
    final double lambda = getLambda(r, gamma, b, sigmaSq);
    final double kappa = getKappa(b, gamma, sigmaSq);
    final double rho = Math.sqrt(t1 / t2);
    return Math.exp(lambda * t2)
        * Math.pow(s, gamma)
        * (BIVARIATE_NORMAL.getCDF(new double[] {d1, e1, rho}) - Math.pow(x2 / s, kappa) * BIVARIATE_NORMAL.getCDF(new double[] {d2, e2, rho}) - Math.pow(x1 / s, kappa)
            * BIVARIATE_NORMAL.getCDF(new double[] {d3, e3, -rho}) + Math.pow(x1 / x2, kappa) * BIVARIATE_NORMAL.getCDF(new double[] {d4, e4, -rho}));
  }

  private double getLambda(final double r, final double gamma, final double b, final double sigmaSq) {
    return -r + gamma * b + 0.5 * gamma * (gamma - 1) * sigmaSq;
  }

  private double getKappa(final double b, final double gamma, final double sigmaSq) {
    return 2 * b / sigmaSq + 2 * gamma - 1;
  }

  private double getY(final double t, final double b, final double sigmaSq, final double gamma, final double denom) {
    return t * (b + sigmaSq * (gamma - 0.5)) / denom;
  }

  private double getDenom(final double t, final double sigma) {
    return sigma * Math.sqrt(t);
  }

  private double getD(final double x, final double denom, final double y) {
    return -(Math.log(x) / denom + y);
  }
}
