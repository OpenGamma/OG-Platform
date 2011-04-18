/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class SABRHaganVolatilityFunction implements VolatilityFunctionProvider<SABRFormulaData> {
  private static final double EPS = 1e-15;

  @Override
  public Function1D<SABRFormulaData, Double> getVolatilityFunction(final EuropeanVanillaOption option) {
    Validate.notNull(option, "option");
    final double k = Math.max(option.getStrike(), 0.000001); // Floored at 0.01bp
    // TODO: Improve treatment around strike/k=0?
    final double t = option.getTimeToExpiry();
    return new Function1D<SABRFormulaData, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public final Double evaluate(final SABRFormulaData data) {
        Validate.notNull(data, "data");
        final double alpha = data.getAlpha();
        final double beta = data.getBeta();
        final double rho = data.getRho();
        final double nu = data.getNu();
        final double f = data.getForward();
        double vol, z, zOverChi;
        final double beta1 = 1 - beta;
        if (CompareUtils.closeEquals(f, k, EPS)) {
          final double f1 = Math.pow(f, beta1);
          vol = alpha * (1 + t * (beta1 * beta1 * alpha * alpha / 24 / f1 / f1 + rho * alpha * beta * nu / 4 / f1 + nu * nu * (2 - 3 * rho * rho) / 24)) / f1;

        } else {
          if (CompareUtils.closeEquals(beta, 0, EPS)) {
            final double ln = Math.log(f / k);
            z = nu * Math.sqrt(f * k) * ln / alpha;
            zOverChi = getZOverChi(rho, z);
            vol = alpha * ln * zOverChi * (1 + t * (alpha * alpha / f / k + nu * nu * (2 - 3 * rho * rho)) / 24) / (f - k);
          } else if (CompareUtils.closeEquals(beta, 1, EPS)) {
            final double ln = Math.log(f / k);
            z = nu * ln / alpha;
            zOverChi = getZOverChi(rho, z);
            vol = alpha * zOverChi * (1 + t * (rho * alpha * nu / 4 + nu * nu * (2 - 3 * rho * rho) / 24));
          } else {
            final double ln = Math.log(f / k);
            final double f1 = Math.pow(f * k, beta1);
            final double f1Sqrt = Math.sqrt(f1);
            final double lnBetaSq = Math.pow(beta1 * ln, 2);
            z = nu * f1Sqrt * ln / alpha;
            zOverChi = getZOverChi(rho, z);
            final double first = alpha / (f1Sqrt * (1 + lnBetaSq / 24 + lnBetaSq * lnBetaSq / 1920));
            final double second = zOverChi;
            final double third = 1 + t * (beta1 * beta1 * alpha * alpha / 24 / f1 + rho * nu * beta * alpha / 4 / f1Sqrt + nu * nu * (2 - 3 * rho * rho) / 24);
            vol = first * second * third;
          }
        }

        return vol;
      }
    };
  }

  /**
   * Return the Black implied volatility in the SABR model and its derivatives.
   * @param option The option.
   * @param data The Black data.
   * @return An array with [0] the volatility, [1] Derivative w.r.t the forward, [2] the derivative w.r.t the strike, [3] the derivative w.r.t. to alpha,
   * [4] the derivative w.r.t. to rho, [5] the derivative w.r.t. to nu
   */
  public double[] getVolatilityAdjoint(final EuropeanVanillaOption option, final SABRFormulaData data) {
    /**
     * The array storing the price and derivatives.
     */
    double[] volatilityAdjoint = new double[6];

    final double strike = Math.max(option.getStrike(), 0.000001);
    final double timeToExpiry = option.getTimeToExpiry();
    final double alpha = data.getAlpha();
    final double beta = data.getBeta();
    final double rho = data.getRho();
    final double nu = data.getNu();
    final double forward = data.getForward();

    // Implementation note: Forward sweep.
    double sfK = Math.pow(forward * strike, (1 - beta) / 2);
    double lnrfK = Math.log(forward / strike);
    double z = nu / alpha * sfK * lnrfK;
    double rzxz;
    double xz = 0;
    if (Math.abs(forward - strike) < 1E-7) {
      rzxz = 1 - rho * z / 2; // order 1
    } else {
      xz = Math.log((Math.sqrt(1 - 2 * rho * z + z * z) + z - rho) / (1 - rho));
      rzxz = z / xz;
    }
    double sf1 = sfK * (1 + (1 - beta) * (1 - beta) / 24 * (lnrfK * lnrfK) + Math.pow(1 - beta, 4) / 1920 * Math.pow(lnrfK, 4));
    double sf2 = (1 + (Math.pow((1 - beta) * alpha / sfK, 2) / 24 + (rho * beta * nu * alpha) / (4 * sfK) + (2 - 3 * rho * rho) * nu * nu / 24) * timeToExpiry);
    volatilityAdjoint[0] = alpha / sf1 * rzxz * sf2;

    // Implementation note: Backward sweep.
    double vBar = 1;
    double sf2Bar = alpha / sf1 * rzxz * vBar;
    double sf1Bar = -alpha / (sf1 * sf1) * rzxz * sf2 * vBar;
    double rzxzBar = alpha / sf1 * sf2 * vBar;
    double zBar;
    double xzBar = 0;
    if (Math.abs(forward - strike) < 1E-7) {
      zBar = -rho / 2 * rzxzBar;
    } else {
      xzBar = -z / (xz * xz) * rzxzBar;
      zBar = 1 / xz * rzxzBar + 1 / ((Math.sqrt(1 - 2 * rho * z + z * z) + z - rho)) * (0.5 * Math.pow(1 - 2 * rho * z + z * z, -0.5) * (-2 * rho + 2 * z) + 1) * xzBar;
    }
    double lnrfKBar = sfK * ((1 - beta) * (1 - beta) / 12 * lnrfK + Math.pow(1 - beta, 4) / 1920 * 4 * Math.pow(lnrfK, 3)) * sf1Bar + nu / alpha * sfK * zBar;
    double sfKBar = nu / alpha * lnrfK * zBar + (1 + (1 - beta) * (1 - beta) / 24 * lnrfK * lnrfK + Math.pow(1 - beta, 4) / 1920 * Math.pow(lnrfK, 4)) * sf1Bar
        + (-Math.pow((1 - beta) * alpha, 2) / Math.pow(sfK, 3) / 12 - (rho * beta * nu * alpha) / 4 / (sfK * sfK)) * timeToExpiry * sf2Bar;
    double strikeBar = -1 / strike * lnrfKBar + (1 - beta) * sfK / (2 * strike) * sfKBar;
    double forwardBar = 1 / forward * lnrfKBar + (1 - beta) * sfK / (2 * forward) * sfKBar;
    double nuBar = 1 / alpha * sfK * lnrfK * zBar + ((rho * beta * alpha) / (4 * sfK) + (2 - 3 * rho * rho) * nu / 12) * timeToExpiry * sf2Bar;
    double rhoBar;
    if (Math.abs(forward - strike) < 1E-7) {
      rhoBar = -z / 2 * rzxzBar;
    } else {
      rhoBar = (1 / (Math.sqrt(1 - 2 * rho * z + z * z) + z - rho) * (-Math.pow(1 - 2 * rho * z + z * z, -0.5) * z - 1) + 1 / (1 - rho)) * xzBar;
    }
    rhoBar += ((beta * nu * alpha) / (4 * sfK) - rho * nu * nu / 4) * timeToExpiry * sf2Bar;

    double alphaBar = -nu / (alpha * alpha) * sfK * lnrfK * zBar + (((1 - beta) * alpha / sfK) * ((1 - beta) / sfK) / 12 + (rho * beta * nu) / (4 * sfK)) * timeToExpiry * sf2Bar + 1 / sf1 * rzxz
        * sf2 * vBar;
    volatilityAdjoint[1] = forwardBar;
    volatilityAdjoint[2] = strikeBar;
    volatilityAdjoint[3] = alphaBar;
    volatilityAdjoint[4] = rhoBar;
    volatilityAdjoint[5] = nuBar;

    return volatilityAdjoint;
  }

  private double getZOverChi(final double rho, final double z) {

    if (CompareUtils.closeEquals(z, 0.0, EPS)) {
      return 1.0;
    }

    final double rhoStar = 1 - rho;
    if (CompareUtils.closeEquals(rhoStar, 0.0, 1e-8)) {
      if (z >= 1.0) {
        return 0.0;
      }
      return -z / Math.log(1 - z);
    }
    // Implementation comment: To avoid numerical instability (0/0) around ATM the first order approximation is used.
    if (CompareUtils.closeEquals(z, 0.0, 1E-7)) {
      return 1.0 - rho * z / 2.0;
    }
    final double chi = Math.log((Math.sqrt(1 - 2 * rho * z + z * z) + z - rho)) - Math.log(rhoStar);
    return z / chi;
  }
}
